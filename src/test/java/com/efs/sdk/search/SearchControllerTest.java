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
package com.efs.sdk.search;

import com.efs.sdk.search.commons.SearchException;
import com.efs.sdk.search.helper.AuthHelper;
import com.efs.sdk.search.model.search.Criteria;
import com.efs.sdk.search.model.search.Query;
import com.efs.sdk.search.services.SearchService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Set;

import static com.efs.sdk.search.SearchController.ENDPOINT;
import static com.efs.sdk.search.commons.SearchException.SEARCH_ERROR.ERROR_CREATING_QUERY;
import static com.efs.sdk.search.commons.SearchException.SEARCH_ERROR.UNABLE_EXTRACT_RETURN_VALUE;
import static com.efs.sdk.search.model.search.DataType.DATE;
import static com.efs.sdk.search.model.search.DataType.STRING;
import static com.efs.sdk.search.utils.TestHelper.getAccessToken;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SearchController.class)
@ActiveProfiles("test")
class SearchControllerTest {

    @Autowired
    private MockMvc mvc;
    /* required for security tests to run. Do not remove! */
    @MockBean
    private JwtDecoder decoder;

    @MockBean
    private AuthHelper authHelper;

    @MockBean
    private SearchService service;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void givenNoAuthentication_whenGetIndexes_thenError() throws Exception {
        mvc.perform(get(ENDPOINT + "/index")).andExpect(status().isUnauthorized());
    }

    @Test
    void givenAuthentication_whenGetIndexes_thenOk() throws Exception {
        Set<String> indexes = Set.of("test-index", "test-index2");
        given(service.getIndexes(anyString(), anyString())).willReturn(indexes);

        mvc.perform(get(ENDPOINT + "/index").with(jwt())).andExpect(status().isOk());
    }

    @Test
    void givenNoAuthentication_whenSearch_thenError() throws Exception {
        mvc.perform(post(ENDPOINT)).andExpect(status().isForbidden());
    }

    @Test
    void givenNoQuery_whenSearch_thenError() throws Exception {
        mvc.perform(post(ENDPOINT).with(jwt())).andExpect(status().is4xxClientError());
    }

    @Test
    void givenQuery_whenSearch_thenOk() throws Exception {
        Query query = new Query();
        mvc.perform(post(ENDPOINT).with(jwt()).contentType(APPLICATION_JSON).content(objectMapper.writeValueAsString(query))).andExpect(status().isOk());
    }

    @Test
    void givenNoAuthentication_whenGetResultProperties_thenError() throws Exception {
        mvc.perform(get(ENDPOINT + "/resultproperties")).andExpect(status().isUnauthorized());
    }

    @Test
    void givenAuthentication_whenGetResultProperties_thenOk() throws Exception {
        List<String> resultProperties = List.of("prop1", "prop2");

        given(authHelper.getAccessToken(any())).willReturn(getAccessToken());
        given(service.getResultProperties(anyString(), anyString())).willReturn(Set.copyOf(resultProperties));

        mvc.perform(get(ENDPOINT + "/resultproperties").queryParam("index", "test").with(jwt())).andExpect(status().isOk()).andExpect(jsonPath("$.size()").value(2));
    }

    @Test
    void givenNoAuthentication_whenGetCriteria_thenError() throws Exception {
        mvc.perform(get(ENDPOINT + "/criteria")).andExpect(status().isUnauthorized());
    }

    @Test
    void givenNoIndex_whenGetCriteria_thenDefault() throws Exception {
        List<Criteria> criterias = List.of(new Criteria("test", DATE), new Criteria("test2", STRING));

        given(authHelper.getAccessToken(any())).willReturn(getAccessToken());
        given(service.getCriteria(anyString(), anyString())).willReturn(criterias);

        mvc.perform(get(ENDPOINT + "/criteria").with(jwt())).andExpect(status().isOk()).andExpect(jsonPath("$.size()").value(2));
    }

    @Test
    void givenAuthentication_whenGetCriteria_thenOk() throws Exception {
        List<Criteria> criterias = List.of(new Criteria("test", DATE), new Criteria("test2", STRING));

        given(authHelper.getAccessToken(any())).willReturn(getAccessToken());
        given(service.getCriteria(anyString(), anyString())).willReturn(criterias);

        mvc.perform(get(ENDPOINT + "/criteria").queryParam("index", "test").with(jwt())).andExpect(status().isOk()).andExpect(jsonPath("$.size()").value(2));
    }

    @Test
    void givenException_whenSearch_thenError() throws Exception {
        Query query = new Query();

        given(authHelper.getAccessToken(any())).willReturn("something");
        SearchException sException = new SearchException(ERROR_CREATING_QUERY);
        given(service.executeSearch(any(), anyString())).willThrow(sException);

        MvcResult result = mvc.perform(post(ENDPOINT).with(jwt()).contentType(APPLICATION_JSON).content(objectMapper.writeValueAsString(query))).andReturn();
        MockHttpServletResponse response = result.getResponse();

        assertNotNull(response);
        assertEquals(sException.getHttpStatus().value(), response.getStatus());
    }

    @Test
    void givenException_whenGetIndexes_thenError() throws Exception {
        given(authHelper.getAccessToken(any())).willReturn("something");
        SearchException sException = new SearchException(UNABLE_EXTRACT_RETURN_VALUE);
        given(service.getIndexes(anyString(), anyString())).willThrow(sException);

        MvcResult result = mvc.perform(get(ENDPOINT + "/index").with(jwt())).andReturn();
        MockHttpServletResponse response = result.getResponse();

        assertNotNull(response);
        assertEquals(sException.getHttpStatus().value(), response.getStatus());
    }

    @Test
    void givenException_whenGetCriteria_thenError() throws Exception {
        given(authHelper.getAccessToken(any())).willReturn("something");
        SearchException sException = new SearchException(UNABLE_EXTRACT_RETURN_VALUE);
        given(service.getCriteria(anyString(), anyString())).willThrow(sException);

        MvcResult result = mvc.perform(get(ENDPOINT + "/criteria").queryParam("index", "test").with(jwt())).andReturn();
        MockHttpServletResponse response = result.getResponse();

        assertNotNull(response);
        assertEquals(sException.getHttpStatus().value(), response.getStatus());
    }

    @Test
    void givenException_whenGetResultProperties_thenError() throws Exception {
        given(authHelper.getAccessToken(any())).willReturn("something");
        SearchException sException = new SearchException(UNABLE_EXTRACT_RETURN_VALUE);
        given(service.getResultProperties(anyString(), anyString())).willThrow(sException);

        MvcResult result = mvc.perform(get(ENDPOINT + "/resultproperties").queryParam("index", "test").with(jwt())).andReturn();
        MockHttpServletResponse response = result.getResponse();

        assertNotNull(response);
        assertEquals(sException.getHttpStatus().value(), response.getStatus());
    }

    @Test
    void givenNoIndex_whenGetResultProperties_thenDefault() throws Exception {
        List<Criteria> criterias = List.of(new Criteria("test", DATE), new Criteria("test2", STRING));

        given(authHelper.getAccessToken(any())).willReturn(getAccessToken());
        given(service.getCriteria(anyString(), anyString())).willReturn(criterias);

        mvc.perform(get(ENDPOINT + "/criteria").with(jwt())).andExpect(status().isOk()).andExpect(jsonPath("$.size()").value(2));
    }

}
