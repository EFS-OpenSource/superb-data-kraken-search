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
package com.efs.sdk.search.helper;

import com.efs.sdk.search.commons.SearchException;
import com.efs.sdk.search.model.elasticsearch.ESFieldProperty;
import com.efs.sdk.search.model.search.Criteria;
import com.efs.sdk.search.model.search.DataType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static com.efs.sdk.search.commons.SearchException.SEARCH_ERROR.EXTRACTION_ERROR_MAPPING;
import static com.efs.sdk.search.model.search.DataType.*;
import static java.lang.String.format;

@Component
public class ParseHelper {

    public static String wildcardToRegex(String pattern) {
        return "^" + pattern.replace("*", ".*").replace("?", ".") + "$";
    }

    public void parseProperties(Set<Criteria> criteria, String prefix, Map<String, ESFieldProperty> esProperties) {

        if (esProperties == null) {
            return;
        }
        for (Map.Entry<String, ESFieldProperty> propertyEntry : esProperties.entrySet()) {
            String propertyName = prefix.isBlank() ? propertyEntry.getKey() : format("%s.%s", prefix, propertyEntry.getKey());

            ESFieldProperty property = propertyEntry.getValue();
            if (property.isParent()) {
                // Recursive call of function
                parseProperties(criteria, propertyName, property.properties());
            } else {
                if (!Boolean.FALSE.equals(property.enabled()) && !Boolean.FALSE.equals(property.index())) {
                    DataType criteriaType = getDataType(property);
                    criteria.add(new Criteria(propertyName, criteriaType));
                }
            }
        }
    }

    public void parsePropertyNames(Set<String> propertyNames, String prefix, Map<String, ESFieldProperty> esProperties) {
        if (esProperties == null) {
            return;
        }
        for (Map.Entry<String, ESFieldProperty> propertyEntry : esProperties.entrySet()) {
            String propertyName = prefix.isBlank() ? propertyEntry.getKey() : String.format("%s.%s", prefix, propertyEntry.getKey());

            ESFieldProperty property = propertyEntry.getValue();
            if (property.isParent()) {
                parsePropertyNames(propertyNames, propertyName, property.properties());
            } else {
                propertyNames.add(propertyName);
            }
        }
    }

    protected DataType getDataType(ESFieldProperty property) {
        if (List.of("float", "long").contains(property.type().toLowerCase())) {
            return NUMBER;
        }
        if ("date".equalsIgnoreCase(property.type())) {
            return DATE;
        }
        if ("boolean".equalsIgnoreCase(property.type())) {
            return BOOLEAN;
        }
        return STRING;
    }

    public List<List<String>> splitListByMaxLength(List<String> indices, int maxLength) {
        List<List<String>> listOfLists = new ArrayList<>();
        List<String> indexBunch = new ArrayList<>();
        for (int i = 0; i < indices.size(); i++) {
            String index = indices.get(i);
            indexBunch.add(index);
            if (i == indices.size() - 1 || (String.join(",", indexBunch) + "," + indices.get(i + 1)).length() > maxLength) {
                listOfLists.add(indexBunch);
                indexBunch = new ArrayList<>();
            }
        }
        return listOfLists;
    }

    public Map<String, Object> getFilteredMapByIndexKeyWildcard(String indexWildCard, Map<String, Object> initialMap) {
        return initialMap.entrySet().stream().filter(o -> o.getKey().matches(wildcardToRegex(indexWildCard))).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public Map<String, Map<String, Map<String, ESFieldProperty>>> parseMappingsFromObject(Object indexMappings) throws SearchException {
        try {
            ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return mapper.convertValue(indexMappings, new TypeReference<>() {
            });

        } catch (Exception e) {
            throw new SearchException(EXTRACTION_ERROR_MAPPING, e.getMessage());
        }
    }

}
