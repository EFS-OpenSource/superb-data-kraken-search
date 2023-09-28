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

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class Filter {

    @Schema(description = "The property-name.", example = "metadata.project.projectInfo")
    private String property;
    @Schema(description = "The operator.", example = "EQ")
    private Operator operator;
    @Schema(description = "The value of the property.", example = "demo-project")
    private String value;

    @Schema(description = "The lower bound of between-filter. Only supported in case of operator BETWEEN", example = "2")
    private String lowerBound;
    @Schema(description = "The upper bound of between-filter. Only supported in case of operator BETWEEN", example = "5")
    private String upperBound;
    @Schema(description = "The data-type of the property.", example = "STRING")
    private DataType dataType;

}
