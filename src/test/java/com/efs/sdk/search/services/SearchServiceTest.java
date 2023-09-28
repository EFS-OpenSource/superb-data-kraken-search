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
import com.efs.sdk.search.clients.ResultBuilder;
import com.efs.sdk.search.model.elasticsearch.ESResponse;
import com.efs.sdk.search.model.search.Criteria;
import com.efs.sdk.search.model.search.Query;
import com.efs.sdk.search.model.search.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.efs.sdk.search.model.search.DataType.DATE;
import static com.efs.sdk.search.model.search.DataType.STRING;
import static com.efs.sdk.search.utils.TestHelper.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ActiveProfiles("test")
class SearchServiceTest {

    @MockBean
    private ElasticSearchRestClient searchClient;
    @MockBean
    private ResultBuilder resultBuilder;
    @MockBean
    private IndexClearer indexClearer;
    private SearchService service;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        this.searchClient = Mockito.mock(ElasticSearchRestClient.class);
        this.resultBuilder = Mockito.mock(ResultBuilder.class);
        this.indexClearer = Mockito.mock(IndexClearer.class);
        this.service = new SearchService(searchClient, resultBuilder, indexClearer);
    }

    @Test
    void givenEmptyResponse_whenExecuteSearch_thenOk() throws Exception {
        ESResponse response = new ESResponse(0, false, null, null);
        Query query = new Query();

        given(searchClient.executeSearch(any(), anyString())).willReturn(response);
        given(resultBuilder.buildResult(any())).willReturn(new Result());

        Result expected = new Result();
        expected.setPage(query.getPage());
        expected.setSize(query.getSize());

        Result actual = service.executeSearch(query, getAccessToken());
        assertEquals(expected, actual);
    }

    @Test
    void givenSingleFilterQuery_whenExecuteSearch_thenOk() throws Exception {
        String queryStr = getInputContent(QUERY_PATH, "singleFilterQuery.json");
        String expectedResultStr = getInputContent(RESULT_PATH, "singleFilterResult.json");

        String token = getAccessToken();
        Result expected = objectMapper.readValue(expectedResultStr, Result.class);
        Query query = objectMapper.readValue(queryStr, Query.class);
        ESResponse response = new ESResponse(0, false, null, null);
        given(searchClient.executeSearch(any(), anyString())).willReturn(response);
        given(resultBuilder.buildResult(any())).willReturn(expected);

        Result actual = service.executeSearch(query, token);
        String actualResultStr = objectMapper.writeValueAsString(actual);

        JSONAssert.assertEquals(expectedResultStr, actualResultStr, false);
    }

    @Test
    void givenCriteria_whenGetCriteria_thenOk() throws Exception {
        List<Criteria> responses = List.of(
                new Criteria("uuid", STRING),
                new Criteria("metadata.customer.customerId", STRING),
                new Criteria("massdata.dateCreated", DATE)
        );
        given(searchClient.getCriterias(any(), any())).willReturn(responses);

        List<Criteria> actual = service.getCriteria(getAccessToken(), "test");
        assertThat(actual.size(), is(responses.size()));
        for (Criteria response : responses) {
            assertTrue(actual.stream().anyMatch(c -> response.property().equals(c.property())));
            assertTrue(actual.stream().anyMatch(c -> response.dataType().equals(c.dataType())));
        }
    }

    @Test
    void givenIndexes_whenGetIndexes_thenOk() throws Exception {
        Set<String> indexes = Set.of("index1", "index2");
        given(searchClient.getIndexes(anyString(), anyString())).willReturn(indexes);
        given(indexClearer.clearIndexes(any())).willReturn(indexes);

        String expectedStr = objectMapper.writeValueAsString(indexes);
        Set<String> actual = service.getIndexes(getAccessToken(), ".*");
        String actualStr = objectMapper.writeValueAsString(actual);
        JSONAssert.assertEquals(expectedStr, actualStr, false);
    }

    @Test
    void givenKibanaIndexes_whenGetIndexes_thenOk() throws Exception {
        String searchResult = getInputContent(ESRESULT_PATH, "aliases.json");
        Map<String, Object> indices = objectMapper.readValue(searchResult, Map.class);

        Set<String> allIndexes = indices.keySet();
        given(searchClient.getIndexes(anyString(), anyString())).willReturn(allIndexes);

        String noSpecials = getInputContent(RESULT_PATH, "aliasesNoSpecials.json");
        Set<String> noSpecialIndexes = objectMapper.readValue(noSpecials, Set.class);
        given(indexClearer.clearIndexes(any())).willReturn(noSpecialIndexes);

        Set<String> expected = Set.of("test-index", "test-index-2");
        String expectedStr = objectMapper.writeValueAsString(expected);
        Set<String> actual = service.getIndexes(getAccessToken(), ".*");
        String actualStr = objectMapper.writeValueAsString(actual);
        JSONAssert.assertEquals(expectedStr, actualStr, false);
    }

    @Test
    void givenResultProperties_whenGetResultProperties_thenOk() throws Exception {
        Set<String> resultProperties = Set.of("metadata.customer.customerInfo", "metadata.project.projectInfo");
        given(searchClient.getResultProperties(anyString(), anyString())).willReturn(resultProperties);

        String expectedStr = objectMapper.writeValueAsString(resultProperties);
        Set<String> actual = service.getResultProperties(getAccessToken(), ".*");
        String actualStr = objectMapper.writeValueAsString(actual);
        JSONAssert.assertEquals(expectedStr, actualStr, false);
    }
}
