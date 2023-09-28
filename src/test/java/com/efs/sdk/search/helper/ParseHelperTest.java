package com.efs.sdk.search.helper;

import com.efs.sdk.search.commons.SearchException;
import com.efs.sdk.search.model.elasticsearch.ESMappingFieldProperty;
import com.efs.sdk.search.model.search.Criteria;
import com.efs.sdk.search.model.search.DataType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;

import static com.efs.sdk.search.commons.SearchException.SEARCH_ERROR.UNABLE_EXTRACT_STRING_TO_OBJECT;
import static com.efs.sdk.search.utils.TestHelper.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@ActiveProfiles("test")
class ParseHelperTest {

    private ParseHelper parseHelper;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        this.parseHelper = new ParseHelper();
        this.objectMapper = new ObjectMapper();

    }

    private <T> T readValue(String content, Class<T> clazz) throws SearchException {
        try {
            return objectMapper.readValue(content, clazz);
        } catch (JsonProcessingException e) {
            throw new SearchException(UNABLE_EXTRACT_STRING_TO_OBJECT, content);
        }
    }

    @Test
    void givenParseProperties_whenParseProperties_thenOk() throws Exception {
        String searchResult = getInputContent(ESRESULT_PATH, "sourceData.json");
        Map<String, Object> tempProps = readValue(searchResult, Map.class);
        Map<String, ESMappingFieldProperty> properties = new ObjectMapper().convertValue(tempProps, new TypeReference<>() {
        });
        Set<Criteria> criteria = new HashSet<>();
        parseHelper.parseProperties(criteria, "", properties);
        String actual = objectMapper.writeValueAsString(criteria);
        String expected = getInputContent(RESULT_PATH, "criterias.json");
        assertFalse(criteria.isEmpty());
        JSONAssert.assertEquals(expected, actual, false);
    }

    @Test
    void givenSplitListByMaxLength_whenSplitListByMaxLength_thenOk() throws Exception {
        List<String> testList = List.of("index1", "index2", "index3", "index4", "index5", "index6", "index7", "index8");
        List<List<String>> stringList = parseHelper.splitListByMaxLength(testList, 20);
        List<String> flattenedList = stringList.stream()
                .flatMap(List::stream)
                .toList();
        JSONAssert.assertEquals(testList.toString(), flattenedList.toString(), false);
    }

    @Test
    void givenPropertyNames_whenGetPropertyNames_thenOk() throws Exception {
        String searchResult = getInputContent(ESRESULT_PATH, "sourceData.json");
        Map<String, Object> tempProps = readValue(searchResult, Map.class);
        Map<String, ESMappingFieldProperty> properties = new ObjectMapper().convertValue(tempProps, new TypeReference<>() {
        });

        Set<String> propertyNames = new HashSet<>();
        parseHelper.parsePropertyNames(propertyNames, "", properties);

        String actual = objectMapper.writeValueAsString(propertyNames);
        String expected = getInputContent(RESULT_PATH, "resultProperties.json");
        assertFalse(propertyNames.isEmpty());
        JSONAssert.assertEquals(expected, actual, false);
    }

    @Test
    void givenFloat_whenGetDataType_thenNumber() {
        ESMappingFieldProperty property = new ESMappingFieldProperty("float", new HashMap<>(), new HashMap<>(), true, true);
        DataType expected = DataType.NUMBER;
        assertEquals(expected, parseHelper.getDataType(property));
    }

    @Test
    void givenLong_whenGetDataType_thenNumber() {
        ESMappingFieldProperty property = new ESMappingFieldProperty("long", new HashMap<>(), new HashMap<>(), true, true);
        DataType expected = DataType.NUMBER;
        assertEquals(expected, parseHelper.getDataType(property));
    }

    @Test
    void givenDate_whenGetDataType_thenDate() {
        ESMappingFieldProperty property = new ESMappingFieldProperty("date", new HashMap<>(), new HashMap<>(), true, true);
        DataType expected = DataType.DATE;
        assertEquals(expected, parseHelper.getDataType(property));
    }

    @Test
    void givenBoolean_whenGetDataType_thenBoolean() {
        ESMappingFieldProperty property = new ESMappingFieldProperty("boolean", new HashMap<>(), new HashMap<>(), true, true);
        DataType expected = DataType.BOOLEAN;
        assertEquals(expected, parseHelper.getDataType(property));
    }

    @Test
    void givenNoType_whenGetDataType_thenString() {
        ESMappingFieldProperty property = new ESMappingFieldProperty("", new HashMap<>(), new HashMap<>(), true, true);
        DataType expected = DataType.STRING;
        assertEquals(expected, parseHelper.getDataType(property));
    }

    @Test
    void givenParseMappings_whenParseMappings_thenOk() throws Exception {
        String searchResult = getInputContent(ESRESULT_PATH, "mappingsResultSimple.json");
        Map<String, Object> indexMappings = new ObjectMapper().readValue(searchResult, Map.class);

        for (String index : indexMappings.keySet()) {
            Object object = indexMappings.get(index);
            Map<String, Map<String, Map<String, ESMappingFieldProperty>>> actual = this.parseHelper.parseMappingsFromObject(object);
            assertFalse(actual.isEmpty());
        }
    }


    @Test
    void givenGetFilteredMapByIndexKeyWildcard_whenGetFilteredMapByIndexKeyWildcard_thenOk() throws Exception {
        String filterBy = "*measurement*";
        String searchResult = getInputContent(ESRESULT_PATH, "mappingsResultSimple.json");
        Map<String, Object> indexMappings = new ObjectMapper().readValue(searchResult, Map.class);
        Map<String, Object> filteredIndices = parseHelper.getFilteredMapByIndexKeyWildcard(filterBy, indexMappings);
        assertThat(filteredIndices.keySet().toString(), CoreMatchers.containsString("measurement"));

    }

}