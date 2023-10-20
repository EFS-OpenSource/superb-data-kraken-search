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
package com.efs.sdk.search.services;

import com.efs.sdk.search.clients.ElasticSearchRestClient;
import com.efs.sdk.search.clients.OrganizationManagerClient;
import com.efs.sdk.search.clients.ResultBuilder;
import com.efs.sdk.search.commons.SearchException;
import com.efs.sdk.search.helper.ParseHelper;
import com.efs.sdk.search.model.elasticsearch.ESFieldProperty;
import com.efs.sdk.search.model.elasticsearch.ESResponse;
import com.efs.sdk.search.model.search.Criteria;
import com.efs.sdk.search.model.search.Query;
import com.efs.sdk.search.model.search.Result;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SearchService {


    private static final String MAPPINGS = "mappings";
    private static final String PROPERTIES = "properties";
    private final ResultBuilder resultBuilder;
    private final ElasticSearchRestClient searchClient;
    private final OrganizationManagerClient organizationManagerClient;
    private final ParseHelper parseHelper;

    public SearchService(ElasticSearchRestClient searchClient, ResultBuilder resultBuilder, OrganizationManagerClient organizationManagerClient) {
        this.searchClient = searchClient;
        this.resultBuilder = resultBuilder;
        this.organizationManagerClient = organizationManagerClient;
        this.parseHelper = new ParseHelper();
    }

    public Result executeSearch(Query query, String token) throws SearchException {
        ESResponse response = searchClient.executeSearch(query, token);

        Result result = resultBuilder.buildResult(response);
        result.setPage(query.getPage());
        result.setSize(query.getSize());
        return result;
    }

    public Set<String> getIndices(String token, String indexWildcard) throws SearchException {
        List<Map<String, Object>> filteredMappingsList = getFilteredIndicesAndMappings(token, indexWildcard);
        Set<String> indices = new HashSet<>();
        for (Map<String, Object> filteredMappings : filteredMappingsList) {
            indices.addAll(filteredMappings.keySet());
        }
        return indices;
    }

    public List<Criteria> getCriteria(String token, String indexRegex) throws SearchException {

        List<Map<String, Object>> filteredMappingsList = getFilteredIndicesAndMappings(token, indexRegex);
        Set<Criteria> criteria = new HashSet<>();
        for (Map<String, Object> filteredMappings : filteredMappingsList) {
            for (Object object : filteredMappings.values()) {
                Map<String, Map<String, Map<String, ESFieldProperty>>> mapping = parseHelper.parseMappingsFromObject(object);
                if (mapping.containsKey(MAPPINGS) && mapping.get(MAPPINGS).containsKey(PROPERTIES)) {
                    parseHelper.parseProperties(criteria, "", mapping.get(MAPPINGS).get(PROPERTIES));
                }
            }
        }
        return new ArrayList<>(criteria);
    }


    public Set<String> getResultProperties(String token, String indexRegex) throws SearchException {
        List<Map<String, Object>> filteredMappingsList = getFilteredIndicesAndMappings(token, indexRegex);

        Set<String> propertyNames = new HashSet<>();
        for (Map<String, Object> filteredMappings : filteredMappingsList) {
            for (Object object : filteredMappings.values()) {
                Map<String, Map<String, Map<String, ESFieldProperty>>> mappings = parseHelper.parseMappingsFromObject(object);
                if (mappings != null && !mappings.isEmpty() && mappings.containsKey(MAPPINGS)) {
                    Map<String, Map<String, ESFieldProperty>> mapping = mappings.get(MAPPINGS);
                    if (mapping != null && !mapping.isEmpty() && mapping.containsKey(PROPERTIES)) {
                        parseHelper.parsePropertyNames(propertyNames, "", mapping.get(PROPERTIES));
                    }
                }
            }
        }

        return propertyNames;
    }

    private List<Map<String, Object>> getFilteredIndicesAndMappings(String token, String indexRegex) throws
            SearchException {

        // limit to accessible indices by getting accessible spaces first
        List<String> spaceNamesWithOrganizationPrefix = organizationManagerClient.getAllSpaces(token);
        List<String> spaceNamesWithOrganizationPrefixAsIndexWildcard = spaceNamesWithOrganizationPrefix.stream().map(s -> s + "*").toList();
        // then get mapping... but if there are many indices, GET /<index1>,<index2>..../_mapping becomes too long -> "An HTTP line is larger than 4096 bytes:", therefore splitting is necessary :(
        List<List<String>> listOfLists = parseHelper.splitListByMaxLength(spaceNamesWithOrganizationPrefixAsIndexWildcard, 4000);

        List<Map<String, Object>> returnList = new ArrayList<>();

        for (List<String> indexList : listOfLists) {
            Map<String, Object> mappings = searchClient.getMappings(token, String.join(",", indexList));
            returnList.add(parseHelper.getFilteredMapByIndexKeyWildcard(indexRegex, mappings));
        }
        return returnList;
    }

}
