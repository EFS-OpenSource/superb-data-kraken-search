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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;

@ActiveProfiles("test")
class IndexClearerTest {

    private IndexClearer indexClearer;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        this.indexClearer = new IndexClearer();
    }

    @Test
    void givenSecurityIndex_whenClearIndexes_thenOk() throws Exception {
        Set<String> indexes = Set.of(".async-search",
                ".ds-ilm-history-5-2021.04.27-000002",
                ".kibana_1",
                ".apm-custom-link",
                "test-index",
                "test-index-2",
                "securityinfo",
                ".kibana_task_manager_1",
                ".apm-agent-configuration",
                ".ds-ilm-history-5-2021.03.18-000001"
        );
        Set<String> expected = Set.of("test-index", "test-index-2");
        String expectedStr = objectMapper.writeValueAsString(expected);
        Set<String> actual = indexClearer.clearIndexes(indexes);
        String actualStr = objectMapper.writeValueAsString(actual);

        JSONAssert.assertEquals(expectedStr, actualStr, false);
    }
}
