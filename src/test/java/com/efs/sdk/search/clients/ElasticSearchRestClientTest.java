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
import com.efs.sdk.search.model.elasticsearch.ESResponse;
import com.efs.sdk.search.model.search.Criteria;
import com.efs.sdk.search.model.search.DataType;
import com.efs.sdk.search.model.search.Query;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.entity.StringEntity;
import org.elasticsearch.client.Request;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockserver.configuration.ConfigurationProperties;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.efs.sdk.search.clients.ElasticSearchRestClient.*;
import static com.efs.sdk.search.utils.TestHelper.*;
import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsMapContaining.hasKey;
import static org.hamcrest.collection.IsMapContaining.hasValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;


@ActiveProfiles("test")
class ElasticSearchRestClientTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private ClientAndServer mockServer;

    private ElasticSearchRestClient esRestClient;

    @MockBean
    private QueryBuilder queryBuilder;
    @MockBean
    private ElasticSearchClientBuilder clientBuilder;
    @MockBean
    private ObjectMapper mockMapper;

    @BeforeEach
    void setup() throws Exception {
        this.queryBuilder = Mockito.mock(QueryBuilder.class);

        Integer port = findRandomPort();
        ConfigurationProperties.logLevel("INFO");
        mockServer = ClientAndServer.startClientAndServer(port);

        this.clientBuilder = new ElasticSearchClientBuilderTest("http://127.0.0.1:" + port);

        this.mockMapper = Mockito.spy(new ObjectMapper());
        this.esRestClient = new ElasticSearchRestClient(clientBuilder, mockMapper, queryBuilder);
    }

    @AfterEach
    void destroy() {
        mockServer.stop();
    }

    private Integer findRandomPort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    @Test
    void givenCriteria_whenGetCriteria_thenOk() throws Exception {
        Query query = new Query();
        query.setFilter(emptyList());
        query.setPage(0);
        query.setIndexName(MODEL_INDEX_NAME);
        query.setSize(10_000);

        given(queryBuilder.buildSearch(any())).willReturn(getInputContent(ESQUERY_PATH, "modelindexQuery.json"));
        String searchResult = getInputContent(ESRESULT_PATH, "modelindex.json");
        HttpRequest searchRequest = HttpRequest.request().withMethod(HttpMethod.GET.name()).withPath(format("%s%s", query.getIndexName(), ENDPOINT_SEARCH));

        mockServer.when(searchRequest)
                .respond(HttpResponse.response().withBody(searchResult).withStatusCode(200));

        List<Criteria> criteria = esRestClient.getCriterias("dummy", ".*measurements.*");
        String actual = objectMapper.writeValueAsString(criteria);
        String expected = getInputContent(RESULT_PATH, "criterias.json");
        assertFalse(criteria.isEmpty());
        JSONAssert.assertEquals(expected, actual, false);
    }

    @Test
    void givenAndOrFilterQuery_whenSearch_thenOk() throws Exception {

        String queryStr = getInputContent(QUERY_PATH, "andOrFilterQuery.json");
        Query query = objectMapper.readValue(queryStr, Query.class);

        String searchResult = getInputContent(ESRESULT_PATH, "simpleResult.json");
        HttpRequest searchRequest = HttpRequest.request().withMethod(HttpMethod.GET.name()).withPath(format("%s%s", query.getIndexName(), ENDPOINT_SEARCH));

        mockServer.when(searchRequest).respond(HttpResponse.response().withBody(searchResult).withStatusCode(200));

        given(queryBuilder.buildSearch(any())).willReturn(queryStr);

        ESResponse esResponse = esRestClient.executeSearch(query, "dummy");
        String actual = objectMapper.writeValueAsString(esResponse);
        String expected = getInputContent(ESRESULT_PATH, "simpleResult.json");
        JSONAssert.assertEquals(expected, actual, false);
    }

    @Test
    void givenNoFilter_whenGetIndexes_thenOk() throws Exception {
        String filter = ".*";
        String indexResult = getInputContent(ESRESULT_PATH, "aliases.json");

        HttpRequest aliasRequest = HttpRequest.request().withMethod(HttpMethod.GET.name()).withPath(ENDPOINT_ALIAS);
        mockServer.when(aliasRequest)
                .respond(HttpResponse.response().withBody(indexResult).withStatusCode(200));

        Set<String> actualSet = esRestClient.getIndexes("dummy", filter);
        String actual = objectMapper.writeValueAsString(actualSet);
        String expectedStr = getInputContent(RESULT_PATH, "aliasesNoFilter.json");
        Set<String> expectedSet = objectMapper.readValue(expectedStr, Set.class);
        String expected = objectMapper.writeValueAsString(expectedSet);
        JSONAssert.assertEquals(expected, actual, false);
    }

    @Test
    void givenFilter_whenGetIndexes_thenOk() throws Exception {
        String filter = ".*-2";
        String indexResult = getInputContent(ESRESULT_PATH, "aliases.json");

        HttpRequest aliasRequest = HttpRequest.request().withMethod(HttpMethod.GET.name()).withPath(ENDPOINT_ALIAS);
        mockServer.when(aliasRequest)
                .respond(HttpResponse.response().withBody(indexResult).withStatusCode(200));

        Set<String> actualSet = esRestClient.getIndexes("dummy", filter);
        String actual = objectMapper.writeValueAsString(actualSet);
        String expectedStr = getInputContent(RESULT_PATH, "aliasesFilter.json");
        Set<String> expectedSet = objectMapper.readValue(expectedStr, Set.class);
        String expected = objectMapper.writeValueAsString(expectedSet);
        JSONAssert.assertEquals(expected, actual, false);
    }

    @Test
    void givenResultProperties_whenGetResultProperties_thenOk() throws Exception {

        Query query = new Query();
        query.setFilter(emptyList());
        query.setPage(0);
        query.setIndexName(MODEL_INDEX_NAME);
        query.setSize(10_000);

        given(queryBuilder.buildSearch(any())).willReturn(getInputContent(ESQUERY_PATH, "modelindexQuery.json"));
        String searchResult = getInputContent(ESRESULT_PATH, "modelindex.json");
        HttpRequest searchRequest = HttpRequest.request().withMethod(HttpMethod.GET.name()).withPath(format("%s%s", query.getIndexName(), ENDPOINT_SEARCH));

        String index = ".*measurements.*";

        mockServer.when(searchRequest)
                .respond(HttpResponse.response().withBody(searchResult).withStatusCode(200));

        Set<String> resultProperties = esRestClient.getResultProperties("dummy", index);

        String actual = objectMapper.writeValueAsString(resultProperties);
        String expected = getInputContent(RESULT_PATH, "resultProperties.json");
        assertFalse(resultProperties.isEmpty());
        JSONAssert.assertEquals(expected, actual, false);
    }

    @Test
    void given400_whenGetGetResponseBody_thenError() {
        HttpRequest aliasRequest = HttpRequest.request().withMethod(HttpMethod.GET.name()).withPath(ENDPOINT_ALIAS);
        mockServer.when(aliasRequest)
                .respond(HttpResponse.response().withReasonPhrase("any reason").withStatusCode(400));

        assertThrows(SearchException.class, () -> esRestClient.getGetResponseBody(ENDPOINT_ALIAS, "", "dummy"));
    }

    @Test
    @Disabled("Misplaced or misused argument matcher detected here - what???")
    void givenNoRestClient_whenExecuteGetRequest_thenError() {
        given(clientBuilder.buildRestClient(anyString())).willReturn(null);
        assertThrows(SearchException.class, () -> esRestClient.executeGetRequest(ENDPOINT_ALIAS, "", "dummy"));
    }

    @Test
    void givenParams_whenBuildRequest_thenOk() {
        Map<String, String> params = Map.of("this is", "a test");
        StringEntity entity = new StringEntity("{}", APPLICATION_JSON);
        Request request = esRestClient.buildRequest("GET", "aliases", params, entity);
        Map<String, String> actual = request.getParameters();
        assertThat(actual.size(), is(params.size()));
        assertThat(actual, hasKey("this is"));
        assertThat(actual, hasValue("a test"));
        assertThat(actual, not(hasKey("other key")));
    }

    @Test
    void givenFloat_whenGetDataType_thenNumber() {
        ESFieldProperty property = new ESFieldProperty("float", new HashMap<>(), new HashMap<>(), true);
        DataType expected = DataType.NUMBER;
        assertEquals(expected, esRestClient.getDataType(property));
    }

    @Test
    void givenLong_whenGetDataType_thenNumber() {
        ESFieldProperty property = new ESFieldProperty("long", new HashMap<>(), new HashMap<>(), true);
        DataType expected = DataType.NUMBER;
        assertEquals(expected, esRestClient.getDataType(property));
    }

    @Test
    void givenDate_whenGetDataType_thenDate() {
        ESFieldProperty property = new ESFieldProperty("date", new HashMap<>(), new HashMap<>(), true);
        DataType expected = DataType.DATE;
        assertEquals(expected, esRestClient.getDataType(property));
    }

    @Test
    void givenBoolean_whenGetDataType_thenBoolean() {
        ESFieldProperty property = new ESFieldProperty("boolean", new HashMap<>(), new HashMap<>(), true);
        DataType expected = DataType.BOOLEAN;
        assertEquals(expected, esRestClient.getDataType(property));
    }

    @Test
    void givenNoType_whenGetDataType_thenString() {
        ESFieldProperty property = new ESFieldProperty("", new HashMap<>(), new HashMap<>(), true);
        DataType expected = DataType.STRING;
        assertEquals(expected, esRestClient.getDataType(property));
    }
}
