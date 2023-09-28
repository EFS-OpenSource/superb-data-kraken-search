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

import com.efs.sdk.search.model.elasticsearch.ESHit;
import com.efs.sdk.search.model.elasticsearch.ESHits;
import com.efs.sdk.search.model.elasticsearch.ESResponse;
import com.efs.sdk.search.model.search.Result;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.efs.sdk.search.clients.QueryBuilder.MAX_QUERY_SIZE;

@Component
public class ResultBuilder {

    public Result buildResult(ESResponse response) {
        Result searchResult = new Result();
        int cnt = response.hits().total().value();

        List<Map<String, Object>> hitsSources = collectHits(response);
        Number took = response.took();

        searchResult.setHits(hitsSources);
        searchResult.setMaxResults(Math.min(cnt, MAX_QUERY_SIZE));
        searchResult.setMax(cnt);
        searchResult.setDuration(took);
        return searchResult;
    }

    private List<Map<String, Object>> collectHits(ESResponse response) {
        ESHits hits = response.hits();
        if (hits == null) {
            return Collections.emptyList();
        }
        return hits.hits().stream().map(ESHit::source).toList();
    }
}
