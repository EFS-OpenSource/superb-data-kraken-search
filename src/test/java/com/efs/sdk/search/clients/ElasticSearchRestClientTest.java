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
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.efs.sdk.search.clients.ElasticSearchRestClient.*;
import static com.efs.sdk.search.utils.TestHelper.*;
import static java.lang.String.format;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsMapContaining.hasKey;
import static org.hamcrest.collection.IsMapContaining.hasValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    void givenGetMappings_whenGetMappings_thenOk() throws Exception {
        String mappingsResult = getInputContent(ESRESULT_PATH, "mappingsResultSimple.json");
        HttpRequest mappingRequest = HttpRequest.request().withMethod(HttpMethod.GET.name()).withPath("index1*,index2*" + ENDPOINT_MAPPING);

        mockServer.when(mappingRequest).respond(HttpResponse.response().withBody(mappingsResult).withStatusCode(200));
        List<String> indices = Arrays.asList("index1*", "index2*");
        Map<String, Object> actual = esRestClient.getMappings("token", String.join(",", indices));
        JSONAssert.assertEquals(mappingsResult, actual.toString(), false);
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

}
