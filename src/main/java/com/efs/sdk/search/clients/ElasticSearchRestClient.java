/*
Copyright (C) 2023 e:fs TechHub GmbH (sdk@efs-techhub.com)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package com.efs.sdk.search.clients;

import com.efs.sdk.search.commons.SearchException;
import com.efs.sdk.search.model.elasticsearch.ESFieldProperty;
import com.efs.sdk.search.model.elasticsearch.ESHit;
import com.efs.sdk.search.model.elasticsearch.ESResponse;
import com.efs.sdk.search.model.search.Criteria;
import com.efs.sdk.search.model.search.DataType;
import com.efs.sdk.search.model.search.Query;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.efs.sdk.search.commons.SearchException.SEARCH_ERROR.*;
import static com.efs.sdk.search.model.search.DataType.*;
import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;

@Component
@Slf4j
public class ElasticSearchRestClient {

    /**
     * endpoints
     */
    static final String ENDPOINT_ALIAS = "/_alias";
    static final String ENDPOINT_SEARCH = "/_search";
    static final String ENDPOINT_SEARCH_WITH_HITS = ENDPOINT_SEARCH + "?track_total_hits=true";
    static final String MODEL_INDEX_NAME = "modelindex";

    private final ObjectMapper objectMapper;
    private final QueryBuilder queryBuilder;
    private final ElasticSearchClientBuilder clientBuilder;

    public ElasticSearchRestClient(ElasticSearchClientBuilder clientBuilder, ObjectMapper objectMapper, QueryBuilder queryBuilder) {
        this.clientBuilder = clientBuilder;
        this.objectMapper = objectMapper;
        this.queryBuilder = queryBuilder;
    }

    public Set<String> getIndexes(String token, String filter) throws SearchException {
        String responseBody = getGetResponseBody(ENDPOINT_ALIAS, "", token);
        Map<String, Object> indices = readValue(responseBody, Map.class);
        return indices.keySet().stream().filter(i -> i.matches(filter)).collect(Collectors.toSet());
    }

    public List<Criteria> getCriterias(String token, String index) throws SearchException {
        Map<String, Object> mappings = getMapping(token, index);
        Map<String, Map<String, ESFieldProperty>> properties = parseProperties(mappings);
        return new ArrayList<>(createCriteria(properties));
    }

    private Map<String, Object> getMapping(String token, String indexRegex) throws SearchException {
        Query query = new Query();
        query.setFilter(emptyList());
        query.setPage(0);
        query.setIndexName(MODEL_INDEX_NAME);
        query.setSize(10_000); //get all, 10.000 is maximum size of query - seriously who would want to have as many indices??
        ESResponse response = executeSearch(query, token);
        List<ESHit> hits = response.hits().hits();
        Pattern pattern = Pattern.compile(indexRegex);

        return hits.stream().filter(hit -> pattern.matcher(hit.id()).matches()).collect(Collectors.toMap(ESHit::id, ESHit::source));
    }

    protected String getGetResponseBody(String endpoint, String body, String token) throws SearchException {
        try {
            Response response = executeGetRequest(endpoint, body, token);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode < 200 || statusCode > 299) {
                throw new SearchException(EXTRACTION_ERROR);
            }
            return EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            throw new SearchException(UNABLE_EXTRACT_RETURN_VALUE);
        }
    }

    private Set<Criteria> createCriteria(Map<String, Map<String, ESFieldProperty>> properties) {

        Set<Criteria> criteria = new HashSet<>();
        for (Map<String, ESFieldProperty> indexProperties : properties.values()) {
            parseProperties(criteria, "", indexProperties);
        }

        return criteria;
    }

    protected DataType getDataType(ESFieldProperty property) {
        if (List.of("float", "long").contains(property.type().toLowerCase())) {
            return NUMBER;
        }
        if ("date".equalsIgnoreCase(property.type())) {
            return DATE;
        }
        if ("boolean".equalsIgnoreCase(property.type())) {
            return BOOLEAN;
        }
        return STRING;
    }

    private Map<String, Map<String, ESFieldProperty>> parseProperties(Map<String, Object> mappings) {

        Map<String, Map<String, ESFieldProperty>> properties = new HashMap<>();

        for (Map.Entry<String, Object> mapping : mappings.entrySet()) {
            Map<String, Object> propertyMap = objectMapper.convertValue(mapping.getValue(), new TypeReference<Map<String, Object>>() {
            });
            propertyMap.remove("index_name");
            Map<String, ESFieldProperty> props = objectMapper.convertValue(propertyMap, new TypeReference<Map<String, ESFieldProperty>>() {
            });
            properties.put(mapping.getKey(), props);

        }
        return properties;
    }

    public ESResponse executeSearch(Query query, String token) throws SearchException {

        String queryStr = queryBuilder.buildSearch(query);
        String responseBody = getGetResponseBody(format("%s%s", query.getIndexName(), ENDPOINT_SEARCH_WITH_HITS), queryStr, token);
        return readValue(responseBody, ESResponse.class);
    }

    protected Response executeGetRequest(String endpoint, String body, String token) throws SearchException {
        try (RestClient restClient = clientBuilder.buildRestClient(token)) {
            StringEntity entity = new StringEntity(body, APPLICATION_JSON);
            if (restClient == null) {
                throw new SearchException(UNABLE_GET_ES_CLIENT);
            }
            return restClient.performRequest(buildRequest("GET", endpoint, Collections.emptyMap(), entity));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new SearchException(SEARCH_FAILED);
        }
    }

    protected Request buildRequest(String method, String endpoint, Map<String, String> params, StringEntity entity) {
        Request request = new Request(method, endpoint);
        request.setEntity(entity);

        if (params != null && !params.isEmpty()) {
            params.forEach(request::addParameter);
        }
        return request;
    }

    public Set<String> getResultProperties(String token, String index) throws SearchException {
        Map<String, Object> mappings = getMapping(token, index);
        Map<String, Map<String, ESFieldProperty>> properties = parseProperties(mappings);
        Set<String> propertyNames = new HashSet<>();
        for (Map<String, ESFieldProperty> indexProperties : properties.values()) {
            parsePropertyNames(propertyNames, "", indexProperties);
        }

        return propertyNames;
    }

    private void parseProperties(Set<Criteria> criteria, String prefix, Map<String, ESFieldProperty> esProperties) {

        if (esProperties == null) {
            return;
        }
        for (Map.Entry<String, ESFieldProperty> propertyEntry : esProperties.entrySet()) {
            String propertyName = prefix.isBlank() ? propertyEntry.getKey() : format("%s.%s", prefix, propertyEntry.getKey());

            ESFieldProperty property = propertyEntry.getValue();
            if (property.isParent()) {
                // Recursive call of function
                parseProperties(criteria, propertyName, property.properties());
            } else {
                if (Boolean.TRUE.equals(property.enabled())) {
                    DataType criteriaType = getDataType(property);
                    criteria.add(new Criteria(propertyName, criteriaType));
                }
            }
        }
    }

    private void parsePropertyNames(Set<String> propertyNames, String prefix, Map<String, ESFieldProperty> esProperties) {
        if (esProperties == null) {
            return;
        }
        for (Map.Entry<String, ESFieldProperty> propertyEntry : esProperties.entrySet()) {
            String propertyName = prefix.isBlank() ? propertyEntry.getKey() : String.format("%s.%s", prefix, propertyEntry.getKey());

            ESFieldProperty property = propertyEntry.getValue();
            if (property.isParent()) {
                parsePropertyNames(propertyNames, propertyName, property.properties());
            } else {
                propertyNames.add(propertyName);
            }
        }
    }

    private <T> T readValue(String content, Class<T> clazz) throws SearchException {
        try {
            return objectMapper.readValue(content, clazz);
        } catch (JsonProcessingException e) {
            throw new SearchException(UNABLE_EXTRACT_STRING_TO_OBJECT, content);
        }
    }
}
