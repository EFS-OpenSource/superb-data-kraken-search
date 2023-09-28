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
import com.efs.sdk.search.model.search.Filter;
import com.efs.sdk.search.model.search.Query;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static com.efs.sdk.search.model.search.DataType.DATE;
import static com.efs.sdk.search.model.search.Operator.*;
import static com.efs.sdk.search.utils.TestHelper.*;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ActiveProfiles("test")
class QueryBuilderTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private QueryBuilder queryBuilder;

    @BeforeEach
    void setup() {
        this.queryBuilder = new QueryBuilder(objectMapper);
    }

    @Test
    void givenNoFilter_whenBuildSearch_thenOk() throws Exception {
        Query query = new Query();
        query.setFilter(emptyList());
        query.setPage(0);
        query.setSize(20);

        String expected = getInputContent(ESQUERY_PATH, "noFilterQuery.json");
        String actual = queryBuilder.buildSearch(query);
        JSONAssert.assertEquals(expected, actual, false);
    }

    @Test
    void givenSingleFilter_whenBuildSearch_thenOk() throws Exception {
        Filter filter = new Filter();
        filter.setProperty("metadata.customer.customerId");
        filter.setValue("efs");
        filter.setOperator(EQ);

        Query query = new Query();
        query.setFilter(List.of(filter));
        query.setPage(0);
        query.setSize(20);
        query.setResultProperties(List.of("metadata.customer.customerInfo",
                "metadata.project.projectInfo",
                "metadata.project.projectId"));

        String expected = getInputContent(ESQUERY_PATH, "singleFilterQuery.json");
        String actual = queryBuilder.buildSearch(query);
        JSONAssert.assertEquals(expected, actual, false);
    }

    @Test
    void givenMultiFilter_whenBuildSearch_thenOk() throws Exception {
        Filter filter1 = new Filter();
        filter1.setProperty("metadata.customer.customerId");
        filter1.setValue("efs");
        filter1.setOperator(EQ);

        Filter filter2 = new Filter();
        filter2.setProperty("metadata.customer.customerId");
        filter2.setValue("f");
        filter2.setOperator(LIKE);

        Query query = new Query();
        query.setFilter(List.of(filter1, filter2));
        query.setPage(0);
        query.setSize(20);

        String expected = getInputContent(ESQUERY_PATH, "multiFilterQuery.json");
        String actual = queryBuilder.buildSearch(query);
        JSONAssert.assertEquals(expected, actual, false);
    }

    @Test
    void givenBetweenNoUpperBoundFilter_whenBuildSearch_thenError() {
        Filter filter1 = new Filter();
        filter1.setProperty("massdata.dateCreated");
        filter1.setOperator(BETWEEN);

        Query query = new Query();
        query.setFilter(List.of(filter1));
        query.setPage(0);
        query.setSize(20);

        assertThrows(SearchException.class, () -> queryBuilder.buildSearch(query));
    }

    @Test
    void givenBetweenNoLowerBoundFilter_whenBuildSearch_thenError() {
        Filter filter1 = new Filter();
        filter1.setProperty("massdata.dateCreated");
        filter1.setOperator(BETWEEN);
        filter1.setUpperBound("1");

        Query query = new Query();
        query.setFilter(List.of(filter1));
        query.setPage(0);
        query.setSize(20);

        assertThrows(SearchException.class, () -> queryBuilder.buildSearch(query));
    }

    @Test
    void givenBetweenFilter_whenBuildSearch_thenOk() throws Exception {
        Filter filter1 = new Filter();
        filter1.setProperty("massdata.dateCreated");
        filter1.setUpperBound("2021-05-19");
        filter1.setLowerBound("2021-05-17");
        filter1.setOperator(BETWEEN);

        Query query = new Query();
        query.setFilter(List.of(filter1));
        query.setPage(0);
        query.setSize(20);

        String expected = getInputContent(ESQUERY_PATH, "boundQuery.json");
        String actual = queryBuilder.buildSearch(query);
        JSONAssert.assertEquals(expected, actual, false);
    }

    @Test
    void givenYearBetweenFilter_whenBuildSearch_thenOk() throws Exception {
        Filter filter1 = new Filter();
        filter1.setProperty("massdata.dateCreated");
        filter1.setUpperBound("2021-01-01||/y");
        filter1.setLowerBound("2021-01-01||/y");
        filter1.setDataType(DATE);
        filter1.setOperator(BETWEEN);

        Query query = new Query();
        query.setFilter(List.of(filter1));
        query.setPage(0);
        query.setSize(20);

        String expected = getInputContent(ESQUERY_PATH, "yearBetweenFilterQuery.json");
        String actual = queryBuilder.buildSearch(query);
        JSONAssert.assertEquals(expected, actual, false);
    }

    @Test
    void givenLikeFilter_whenBuildSearch_thenOk() throws Exception {

        Filter filter1 = new Filter();
        filter1.setProperty("metadata.customer.customerId");
        filter1.setValue("f");
        filter1.setOperator(LIKE);

        Query query = new Query();
        query.setFilter(List.of(filter1));
        query.setPage(0);
        query.setSize(20);

        String expected = getInputContent(ESQUERY_PATH, "likeFilterQuery.json");
        String actual = queryBuilder.buildSearch(query);
        JSONAssert.assertEquals(expected, actual, false);

    }

    @Test
    void givenGtFilter_whenBuildSearch_thenOk() throws Exception {
        Filter filter1 = new Filter();
        filter1.setProperty("metadata.projects.id");
        filter1.setValue("5");
        filter1.setOperator(GT);

        Query query = new Query();
        query.setFilter(List.of(filter1));
        query.setPage(0);
        query.setSize(20);

        String expected = getInputContent(ESQUERY_PATH, "gtFilterQuery.json");
        String actual = queryBuilder.buildSearch(query);
        JSONAssert.assertEquals(expected, actual, false);
    }

    @Test
    void givenGteFilter_whenBuildSearch_thenOk() throws Exception {
        Filter filter1 = new Filter();
        filter1.setProperty("metadata.projects.id");
        filter1.setValue("5");
        filter1.setOperator(GTE);

        Query query = new Query();
        query.setFilter(List.of(filter1));
        query.setPage(0);
        query.setSize(20);

        String expected = getInputContent(ESQUERY_PATH, "gteFilterQuery.json");
        String actual = queryBuilder.buildSearch(query);
        JSONAssert.assertEquals(expected, actual, false);
    }

    @Test
    void givenLtFilter_whenBuildSearch_thenOk() throws Exception {
        Filter filter1 = new Filter();
        filter1.setProperty("metadata.projects.id");
        filter1.setValue("5");
        filter1.setOperator(LT);

        Query query = new Query();
        query.setFilter(List.of(filter1));
        query.setPage(0);
        query.setSize(20);

        String expected = getInputContent(ESQUERY_PATH, "ltFilterQuery.json");
        String actual = queryBuilder.buildSearch(query);
        JSONAssert.assertEquals(expected, actual, false);
    }

    @Test
    void givenLteFilter_whenBuildSearch_thenOk() throws Exception {
        Filter filter1 = new Filter();
        filter1.setProperty("metadata.projects.id");
        filter1.setValue("5");
        filter1.setOperator(LTE);

        Query query = new Query();
        query.setFilter(List.of(filter1));
        query.setPage(0);
        query.setSize(20);

        String expected = getInputContent(ESQUERY_PATH, "lteFilterQuery.json");
        String actual = queryBuilder.buildSearch(query);
        JSONAssert.assertEquals(expected, actual, false);
    }

    @Test
    void givenAndOperatorFilter_whenBuildSearch_thenError() {
        Filter filter1 = new Filter();
        filter1.setProperty("metadata.projects.id");
        filter1.setValue("5");
        filter1.setOperator(AND);

        Query query = new Query();
        query.setFilter(List.of(filter1));
        query.setPage(0);
        query.setSize(20);

        assertThrows(SearchException.class, () -> queryBuilder.buildSearch(query));

        filter1.setOperator(OR);

        query.setFilter(List.of(filter1));

        assertThrows(SearchException.class, () -> queryBuilder.buildSearch(query));
    }

    @Test
    void givenNotFilter_whenBuildSearch_thenOk() throws Exception {
        Filter filter = new Filter();
        filter.setProperty("metadata.customer.customerId");
        filter.setValue("efs");
        filter.setOperator(NOT);

        Query query = new Query();
        query.setFilter(List.of(filter));
        query.setPage(0);
        query.setSize(20);

        String expected = getInputContent(ESQUERY_PATH, "notFilterQuery.json");
        String actual = queryBuilder.buildSearch(query);
        JSONAssert.assertEquals(expected, actual, false);
    }

    @Test
    void givenFilterNoValue_whenBuildSearch_thenError() {
        Filter filter = new Filter();
        filter.setProperty("metadata.customer.customerId");
        filter.setOperator(EQ);

        Query query = new Query();
        query.setFilter(List.of(filter));
        query.setPage(0);
        query.setSize(20);

        assertThrows(SearchException.class, () -> queryBuilder.buildSearch(query));
    }

    @Test
    void givenAllFieldsFilter_whenBuildSearch_thenOk() throws Exception {
        Filter filter = new Filter();
        filter.setProperty("_all_fields");
        filter.setOperator(EQ);
        filter.setValue("efs");

        Query query = new Query();
        query.setFilter(List.of(filter));
        query.setPage(0);
        query.setSize(20);

        String expected = getInputContent(ESQUERY_PATH, "allFieldsFilterQuery.json");
        String actual = queryBuilder.buildSearch(query);
        JSONAssert.assertEquals(expected, actual, false);
    }

    @Test
    void givenMultipleAllFieldsFilter_whenBuildSearch_thenOk() throws Exception {
        Filter filter = new Filter();
        filter.setProperty("_all_fields");
        filter.setOperator(EQ);
        filter.setValue("efs");

        Filter filter2 = new Filter();
        filter2.setProperty("_all_fields");
        filter2.setOperator(EQ);
        filter2.setValue("demo");

        Query query = new Query();
        query.setFilter(List.of(filter, filter2));
        query.setPage(0);
        query.setSize(20);

        String expected = getInputContent(ESQUERY_PATH, "multipleAllFieldsFilterQuery.json");
        String actual = queryBuilder.buildSearch(query);
        JSONAssert.assertEquals(expected, actual, false);
    }

    @Test
    void givenEmptyAllFieldsFilter_whenBuildSearch_thenOk() throws Exception {
        Filter filter = new Filter();
        filter.setProperty("_all_fields");
        filter.setOperator(EQ);
        filter.setValue(" ");

        Query query = new Query();
        query.setFilter(List.of(filter));
        query.setPage(0);
        query.setSize(20);

        String expected = getInputContent(ESQUERY_PATH, "emptyAllFieldsFilterQuery.json");
        String actual = queryBuilder.buildSearch(query);
        JSONAssert.assertEquals(expected, actual, false);
    }

    @Test
    void givenAndOrFilter_whenBuildSearch_thenOk() throws Exception {

        String queryStr = getInputContent(QUERY_PATH, "andOrFilterQuery.json");
        Query query = objectMapper.readValue(queryStr, Query.class);

        String actual = queryBuilder.buildSearch(query);
        String expected = getInputContent(ESQUERY_PATH, "andOrFilterQuery.json");
        JSONAssert.assertEquals(expected, actual, false);
    }
}
