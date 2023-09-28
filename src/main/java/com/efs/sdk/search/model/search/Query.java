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
package com.efs.sdk.search.model.search;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class Query {

    @Schema(description = "The number of results to return (maximum: 50.000 - default: 50).", example = "20")
    private int size = 50;
    @Schema(description = "The page of the search-results (starts with 0 - default: 0).", example = "0")
    private int page = 0;
    @JsonProperty(value = "index_name", required = true)
    @Schema(description = "The name of the index to search in", example = "test")
    private String indexName;

    @Schema(description = """
            Filters for the search.
                
            If search without appropriate property is desired (all_fields) set the property to "_all_fields".
                
            All parameter may have set a wildcard (*) at the end or beginning, but this is not set implicitly!
                
            All parts (whitespace separated) in a "all_fields"-search-filter are combined by an AND operator, multiple filter of that type are combined with an OR operator.
                
            """)
    private List<Filter> filter = new ArrayList<>();
    @Schema(description = "List of properties to return in the result.", example = """
            [
            "metadata.customer.customerInfo",
            "metadata.project.projectInfo",
            "metadata.project.projectId"
            ]""")
    private List<String> resultProperties = new ArrayList<>();
}
