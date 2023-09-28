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
import com.efs.sdk.search.commons.SearchException;
import com.efs.sdk.search.model.elasticsearch.ESResponse;
import com.efs.sdk.search.model.search.Criteria;
import com.efs.sdk.search.model.search.Query;
import com.efs.sdk.search.model.search.Result;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class SearchService {

    private final ResultBuilder resultBuilder;
    private final ElasticSearchRestClient searchClient;
    private final IndexClearer indexClearer;

    public SearchService(ElasticSearchRestClient searchClient, ResultBuilder resultBuilder, IndexClearer indexClearer) {
        this.searchClient = searchClient;
        this.resultBuilder = resultBuilder;
        this.indexClearer = indexClearer;
    }

    public Result executeSearch(Query query, String token) throws SearchException {
        ESResponse response = searchClient.executeSearch(query, token);

        Result result = resultBuilder.buildResult(response);
        result.setPage(query.getPage());
        result.setSize(query.getSize());
        return result;
    }

    public Set<String> getIndexes(String token, String filter) throws SearchException {
        Set<String> indices = searchClient.getIndexes(token, filter);
        return indexClearer.clearIndexes(indices);
    }

    public List<Criteria> getCriteria(String token, String index) throws SearchException {
        return searchClient.getCriterias(token, index);
    }

    public Set<String> getResultProperties(String token, String index) throws SearchException {
        return searchClient.getResultProperties(token, index);
    }
}
