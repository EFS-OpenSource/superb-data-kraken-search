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
import com.efs.sdk.search.model.search.Operator;
import com.efs.sdk.search.model.search.Query;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.efs.sdk.search.commons.SearchException.SEARCH_ERROR.*;
import static com.efs.sdk.search.model.search.DataType.STRING;
import static com.efs.sdk.search.model.search.Operator.*;
import static java.lang.String.format;
import static java.lang.String.join;
import static java.util.stream.Collectors.joining;

@Component
public class QueryBuilder {

    public static final int MAX_QUERY_SIZE = 10_000;

    /**
     * property for fulltext-search
     */
    private static final String PROP_ALL_FIELDS = "_all_fields";

    private final ObjectMapper objectMapper;

    public QueryBuilder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String buildSearch(Query query) throws SearchException {
        try {
            Map<String, Object> queryMap = getQueryMap(query);
            return objectMapper.writeValueAsString(queryMap);
        } catch (JsonProcessingException e) {
            throw new SearchException(ERROR_CREATING_QUERY);
        }
    }

    private Map<String, Object> getQueryMap(Query query) throws SearchException {
        Map<String, Object> queryMap = new HashMap<>();
        queryMap.put("from", buildFromParameter(query));
        queryMap.put("size", query.getSize());
        queryMap.put("query", buildQueryString(query));
        queryMap.put("_source", query.getResultProperties());
        return queryMap;
    }

    /**
     * Corrects the 'from'-parameter for es-query considering the es-maximum
     *
     * @param query the query
     * @return corrected 'from' parameter
     */
    private int buildFromParameter(Query query) {
        return Math.min(query.getPage() * query.getSize(), MAX_QUERY_SIZE - query.getSize());
    }

    private Map<String, Map<String, String>> buildQueryString(Query query) throws SearchException {
        Map<String, String> queryStringMap = new HashMap<>();
        String queryString = parseFilter(query);
        queryStringMap.put("query", queryString);
        queryStringMap.put("analyze_wildcard", "true");

        return Collections.singletonMap("query_string", queryStringMap);
    }

    private String parseFilter(Query query) throws SearchException {
        List<String> queryParts = new ArrayList<>();
        Map<String, List<Filter>> equalFilter = new HashMap<>();
        Map<String, List<Filter>> notFilter = new HashMap<>();

        List<Filter> filters = escapeFilters(query.getFilter());
        for (Filter filter : filters) {
            validateFilter(filter);
            switch (filter.getOperator()) {
                case EQ -> equalFilter.computeIfAbsent(filter.getProperty(), k -> new ArrayList<>()).add(filter);
                case LIKE -> {
                    filter.setValue(format("*%s*", filter.getValue()));
                    equalFilter.computeIfAbsent(filter.getProperty(), k -> new ArrayList<>()).add(filter);
                }
                case NOT -> notFilter.computeIfAbsent(filter.getProperty(), k -> new ArrayList<>()).add(filter);
                case GT -> queryParts.add(getBoundFilter(filter.getProperty(), filter.getValue(), "*", false));
                case GTE -> queryParts.add(getBoundFilter(filter.getProperty(), filter.getValue(), "*", true));
                case LT -> queryParts.add(getBoundFilter(filter.getProperty(), "*", filter.getValue(), false));
                case LTE -> queryParts.add(getBoundFilter(filter.getProperty(), "*", filter.getValue(), true));
                case BETWEEN -> {
                    // convert bounds to single value
                    filter.setValue(format("[ %s TO %s ]", filter.getLowerBound(), filter.getUpperBound()));
                    equalFilter.computeIfAbsent(filter.getProperty(), k -> new ArrayList<>()).add(filter);
                }
                default -> throw new SearchException(UNKNOWN_OPERATOR, filter.getOperator().toString());
            }
        }
        enrichQueryParts(equalFilter, queryParts);
        enrichQueryParts(notFilter, queryParts);

        if (!queryParts.isEmpty()) {
            return join(format(" %s ", AND), queryParts);
        }
        return "*";
    }

    private void validateFilter(Filter filter) throws SearchException {
        Operator operator = filter.getOperator();
        if (BETWEEN == operator) {
            if (filter.getUpperBound() == null) {
                throw new SearchException(INVALID_BETWEEN_FILTER_MISSING_UPPERBOUND, filter.getProperty());
            }
            if (filter.getLowerBound() == null) {
                throw new SearchException(INVALID_BETWEEN_FILTER_MISSING_LOWERBOUND, filter.getProperty());
            }
        } else if (filter.getValue() == null) {
            throw new SearchException(INVALID_FILTER_MISSING_VALUE, filter.getProperty());
        }
    }

    private void enrichQueryParts(Map<String, List<Filter>> filter, List<String> queryParts) {
        if (!filter.isEmpty()) {
            queryParts.add(filter.values().stream().map(this::buildSearchParameter).collect(joining(format(" %s ", AND))));
        }
    }

    private String buildSearchParameter(List<Filter> filters) {
        String value = filters.get(0).getValue();
        String key = filters.get(0).getProperty();

        if (PROP_ALL_FIELDS.equalsIgnoreCase(key)) {
            return buildAllFieldsParameter(filters);
        }

        if (filters.size() > 1) {
            String multiValue = filters.stream().map(Filter::getValue).collect(joining(format(" %s ", OR)));
            value = format("( %s )", multiValue);
        }

        boolean notOperatorPresent = "not".equalsIgnoreCase(filters.get(0).getOperator().toString());
        String notOperator = notOperatorPresent ? "NOT " : "";

        return format("%s%s:%s", notOperator, key, value);
    }

    private String buildAllFieldsParameter(List<Filter> filters) {
        if (filters.size() > 1) {
            return format("(%s)",
                    filters.stream().map(searchFilter -> format("( %s )", createAllFieldsValue(searchFilter.getValue()))).collect(joining(format(" %s ", OR))));
        } else {
            return createAllFieldsValue(filters.get(0).getValue());
        }
    }

    private String createAllFieldsValue(String value) {
        String trimmedAllFieldsValue = value == null ? "" : value.trim();
        if (!trimmedAllFieldsValue.isEmpty()) {
            String[] splitAllFieldsValues = trimmedAllFieldsValue.split("\\s+");
            return join(format(" %s ", AND), splitAllFieldsValues);
        }
        return "";
    }

    private String getBoundFilter(String property, String lowerBound, String upperBound, boolean inclusive) {
        if (inclusive) {
            return format("%s:[ %s TO %s ]", property, lowerBound, upperBound);
        }
        return format("%s:{ %s TO %s }", property, lowerBound, upperBound);
    }

    private List<Filter> escapeFilters(List<Filter> filter) {
        List<Filter> escapedFilters = new ArrayList<>(filter);
        escapedFilters.stream().filter(searchFilter -> STRING.equals(searchFilter.getDataType())).forEach(searchFilter -> searchFilter.setValue(searchFilter.getValue().replaceAll("^-", "\\\\-").replace("/", "\\/")));
        return escapedFilters;
    }
}
