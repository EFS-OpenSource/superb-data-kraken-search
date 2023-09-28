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

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class Result {
    @Schema(description = "The number of results to return. Maximum 1000.", example = "100")
    private int size;
    @Schema(description = "The pagination page of the search results. Starts with 0 for the first page.", example = "0")
    private int page;
    @Schema(description = "The total number of results.", example = "123456")
    private long max;
    @JsonProperty("max_results")
    @Schema(description = "The maximum number of results that can be returned. Returns max if < 1000, else 1000.", example = "100")
    private int maxResults ;
    @Schema(description = "The search result, list of result-maps.")
    private List<Map<String, Object>> hits;
    @Schema(description = "The duration of search", example = "0.52")
    private Number duration;
}