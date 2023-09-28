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

import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class IndexClearer {

    /**
     * clear indexes - skip 'special' indices (starting with '.', 'security-auditlog-' and securityinfo)
     *
     * @param indices The list of indices
     * @return cleared indices
     */
    public Set<String> clearIndexes(Set<String> indices) {
        return indices.stream().filter(i -> !i.startsWith(".") && !i.startsWith("security-auditlog-") && !i.equalsIgnoreCase("securityinfo")).collect(Collectors.toSet());
    }
}
