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
package com.efs.sdk.search.model.elasticsearch;

import java.util.Map;

public record ESFieldProperty(String type, Map<String, Object> fields,
                                     Map<String, ESFieldProperty> properties,
                                     Boolean enabled, Boolean index) {
    public boolean isParent() {
        return properties != null;
    }
}