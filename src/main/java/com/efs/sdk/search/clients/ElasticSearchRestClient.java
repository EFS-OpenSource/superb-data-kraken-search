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
import com.efs.sdk.search.model.elasticsearch.ESResponse;
import com.efs.sdk.search.model.search.Query;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import static com.efs.sdk.search.commons.SearchException.SEARCH_ERROR.*;
import static java.lang.String.format;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;

@Component
@Slf4j
public class ElasticSearchRestClient {

    /**
     * endpoints
     */
    static final String ENDPOINT_ALIAS = "/_alias";
    static final String ENDPOINT_SEARCH = "/_search";
    static final String ENDPOINT_MAPPING = "/_mappings";
    static final String ENDPOINT_SEARCH_WITH_HITS = ENDPOINT_SEARCH + "?track_total_hits=true";

    private final ObjectMapper objectMapper;
    private final QueryBuilder queryBuilder;
    private final ElasticSearchClientBuilder clientBuilder;

    public ElasticSearchRestClient(ElasticSearchClientBuilder clientBuilder, ObjectMapper objectMapper, QueryBuilder queryBuilder) {
        this.clientBuilder = clientBuilder;
        this.objectMapper = objectMapper;
        this.queryBuilder = queryBuilder;
    }


    public Map<String, Object> getMappings(String token, String indicesString) throws SearchException {
        String inputString = getGetResponseBody(indicesString + ENDPOINT_MAPPING, "", token);
        return readValue(inputString, Map.class);
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


    private <T> T readValue(String content, Class<T> clazz) throws SearchException {
        try {
            return objectMapper.readValue(content, clazz);
        } catch (JsonProcessingException e) {
            throw new SearchException(UNABLE_EXTRACT_STRING_TO_OBJECT, content);
        }
    }
}
