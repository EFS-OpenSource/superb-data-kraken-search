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
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import static com.efs.sdk.search.commons.SearchException.SEARCH_ERROR.UNAUTHORIZED;

@Component
public class AuthHelper {
    public String getAccessToken(JwtAuthenticationToken authToken) throws SearchException {
        if (authToken == null) {
            throw new SearchException(UNAUTHORIZED);
        }
        Jwt jwt = authToken.getToken();
        return jwt.getTokenValue();
    }
}
