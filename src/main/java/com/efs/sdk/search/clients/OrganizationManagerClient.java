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
package com.efs.sdk.search.clients;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
@Slf4j
public class OrganizationManagerClient {

    private final RestTemplate restTemplate;
    private final String allSpacesEndpoint;

    public OrganizationManagerClient(RestTemplate restTemplate, @Value("${search.organizationmanager-endpoints.spaces}") String allSpacesEndpoint) {
        this.restTemplate = restTemplate;
        this.allSpacesEndpoint = allSpacesEndpoint;
    }


    /**
     * Get spaces (by permissions)
     * <p>
     * Lists all spaces of given organization
     * (only allowed, if user has access to the organization or if space and organization are public).
     * If permissions is set, list only the spaces the user has requested permissions to.
     *
     * @param token the (user) token that is used to make the request
     * @return the spaces
     * @throws RestClientException on 4xx client error or 5xx server error
     */
    public List<String> getAllSpaces(String token) throws RestClientException {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Authorization", "Bearer " + token);

        ParameterizedTypeReference<List<String>> responseType = new ParameterizedTypeReference<>() {
        };
        ResponseEntity<List<String>> response = restTemplate.exchange(allSpacesEndpoint, HttpMethod.GET, new HttpEntity<>(headers), responseType);
        return response.getBody();
    }

}
