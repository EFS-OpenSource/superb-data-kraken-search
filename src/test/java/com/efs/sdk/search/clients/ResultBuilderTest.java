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
import com.efs.sdk.search.model.elasticsearch.ESResponse;
import com.efs.sdk.search.model.search.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static com.efs.sdk.search.utils.TestHelper.ESRESULT_PATH;
import static com.efs.sdk.search.utils.TestHelper.getInputContent;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@ActiveProfiles("test")
class ResultBuilderTest {

    private ObjectMapper objectMapper = new ObjectMapper();
    private ResultBuilder resultBuilder;

    @BeforeEach
    void setup() {
        this.resultBuilder = new ResultBuilder();
    }

    @Test
    void givenSimpleResult_whenBuildResult_thenOk() throws Exception {

        String resultStr = getInputContent(ESRESULT_PATH, "simpleResult.json");
        ESResponse response = objectMapper.readValue(resultStr, ESResponse.class);

        Result actual = resultBuilder.buildResult(response);

        assertThat(actual.getMax(), is(4L));
        List<Map<String, Object>> hits = actual.getHits();
        String hitsStr = objectMapper.writeValueAsString(hits);
        List<Map<String, Object>> sources = response.hits().hits().stream().map(ESHit::source).collect(toList());
        String responseStr = objectMapper.writeValueAsString(sources);

        JSONAssert.assertEquals(responseStr, hitsStr, false);
    }

    @Test
    void givenEmptyResult_whenBuildResult_thenOk() throws Exception {

        String resultStr = getInputContent(ESRESULT_PATH, "emptyResult.json");
        ESResponse response = objectMapper.readValue(resultStr, ESResponse.class);

        Result actual = resultBuilder.buildResult(response);

        assertThat(actual.getMax(), is(0L));
        List<Map<String, Object>> hits = actual.getHits();
        String hitsStr = objectMapper.writeValueAsString(hits);
        List<Map<String, Object>> sources = response.hits().hits().stream().map(ESHit::source).collect(toList());
        String responseStr = objectMapper.writeValueAsString(sources);

        JSONAssert.assertEquals(responseStr, hitsStr, false);
    }
}