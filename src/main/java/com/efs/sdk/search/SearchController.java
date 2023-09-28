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
package com.efs.sdk.search;

import com.efs.sdk.search.commons.SearchException;
import com.efs.sdk.search.helper.AuthHelper;
import com.efs.sdk.search.model.search.Criteria;
import com.efs.sdk.search.model.search.Query;
import com.efs.sdk.search.model.search.Result;
import com.efs.sdk.search.services.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

import static java.lang.String.format;


@RequestMapping(value = SearchController.ENDPOINT)
@RestController
@Tag(name = SearchController.ENDPOINT)
public class SearchController {

    static final String VERSION = "v1.0";
    static final String ENDPOINT = "/" + VERSION;

    private final AuthHelper authHelper;
    private final SearchService searchService;

    public SearchController(AuthHelper authHelper, SearchService searchService) {
        this.authHelper = authHelper;
        this.searchService = searchService;
    }

    @Operation(summary = "Performs the search for a measurement")
    @PostMapping
    @ApiResponse(responseCode = "200", description = "Successfully searched.", useReturnTypeSchema = true)
    @ApiResponse(responseCode = "401", description = "User is not authorized")
    @ApiResponse(responseCode = "422", description = "Either the `Query` could not be transformed into an OpenSearch-query or the return-value could not be " +
            "transformed into a `Result`")
    public ResponseEntity<Result> search(@Parameter(hidden = true) JwtAuthenticationToken jwt, @Parameter(description = "Search query defined as JSON",
            required = true) @RequestBody Query query) throws SearchException {
        String token = authHelper.getAccessToken(jwt);
        return ResponseEntity.ok(searchService.executeSearch(query, token));
    }

    @Operation(summary = "Returns OpenSearch-indices the user has access to. OpenSearch-internal indexes are skipped.")
    @GetMapping(path = "/index")
    @ApiResponse(responseCode = "200", description = "Successfully looked up indices.", useReturnTypeSchema = true)
    @ApiResponse(responseCode = "401", description = "User is not authorized")
    @ApiResponse(responseCode = "422", description = "The return-value could not be transformed into a result")
    public ResponseEntity<Set<String>> getIndexes(@Parameter(hidden = true) JwtAuthenticationToken jwt, @Parameter(description = "index-filter", example =
            ".*") @RequestParam(defaultValue = ".*") String filter) throws SearchException {
        String token = authHelper.getAccessToken(jwt);
        return ResponseEntity.ok(searchService.getIndexes(token, filter == null ? ".*" : filter));
    }

    @Operation(summary = """
            Returns a list of all possible filter-criteria (including datatype and supported operators).

            Disabled properties are skipped.
            """)
    @GetMapping(path = "/criteria")
    @ApiResponse(responseCode = "200", description = "Successfully looked up all possible criteria.", useReturnTypeSchema = true)
    @ApiResponse(responseCode = "401", description = "User is not authorized")
    @ApiResponse(responseCode = "422", description = "The return-value could not be transformed into a result")
    public ResponseEntity<List<Criteria>> getCriteria(@Parameter(hidden = true) JwtAuthenticationToken jwt, @Parameter(description = "name of the index",
            required = true) String index) throws SearchException {
        String token = authHelper.getAccessToken(jwt);
        String indexRegex = ".*";
        if (index != null && !index.isEmpty()) {
            indexRegex = format(".*%s.*", index);
        }
        return ResponseEntity.ok(searchService.getCriteria(token, indexRegex));
    }

    @Operation(summary = "Gets possible search-result-properties")
    @GetMapping(path = "/resultproperties")
    @ApiResponse(responseCode = "200", description = "Successfully looked up all possible criteria.", useReturnTypeSchema = true)
    @ApiResponse(responseCode = "401", description = "User is not authorized")
    @ApiResponse(responseCode = "422", description = "The return-value could not be transformed into a result")
    public ResponseEntity<Set<String>> getResultProperties(@Parameter(hidden = true) JwtAuthenticationToken jwt, @Parameter(description = "name of the index"
            , required = true) String index) throws SearchException {
        String token = authHelper.getAccessToken(jwt);
        String indexRegex = ".*";
        if (index != null && !index.isEmpty()) {
            indexRegex = format(".*%s.*", index);
        }
        return ResponseEntity.ok(searchService.getResultProperties(token, indexRegex));
    }
}
