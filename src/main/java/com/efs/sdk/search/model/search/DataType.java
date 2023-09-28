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

import java.util.List;

import static com.efs.sdk.search.model.search.Operator.*;

public enum DataType {
    STRING(List.of(EQ, NOT, LIKE)),
    DATE(List.of(EQ, NOT, LT, LTE, GT, GTE, BETWEEN)),
    BOOLEAN(List.of(EQ, NOT)),
    NUMBER(List.of(EQ, NOT, LT, LTE, GT, GTE, BETWEEN)),
    ALLFIELDS(List.of(EQ));

    private final List<Operator> operators;

    DataType(List<Operator> operators) {
        this.operators = operators;
    }

    public List<Operator> getOperators() {
        return operators;
    }
}
