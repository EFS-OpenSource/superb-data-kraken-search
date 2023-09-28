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
package com.efs.sdk.search.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.ActiveProfiles;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
class CustomJwtGrantedAuthoritiesConverterTest {

    private CustomJwtGrantedAuthoritiesConverter converter;

    @BeforeEach
    void setup() {
        this.converter = new CustomJwtGrantedAuthoritiesConverter();
    }

    @Test
    void givenRoles_whenExtractRoles_thenOk() {
        List<String> roles = List.of("offline_access", "uma_authorization");

        Collection<? extends GrantedAuthority> collection = converter.convert(getJwt(roles));
        assert collection != null;
        assertThat(collection.toString(), containsString("offline_access"));
    }

    @Test
    void givenEmptyRoles_whenExtractResourceRoles_thenOk() {
        List<String> roles = Collections.emptyList();
        Collection<? extends GrantedAuthority> collection = converter.convert(getJwt(roles));
        assertThat(collection, hasSize(0));
    }

    @Test
    void givenRoles_whenConvert_thenOk() {
        List<String> roles = List.of("offline_access", "uma_authorization");
        Jwt jwt = getJwt(roles);
        Collection<GrantedAuthority> authorities = converter.convert(jwt);
        assert authorities != null;
        for (String role : roles) {
            assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equalsIgnoreCase(role)));
        }
    }

    private Jwt getJwt(List<String> roles) {
        Jwt.Builder jwtBuilder = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("sub", "user")
                .claim("scope", "openid client_role_react-web-app email profile");
        if (!roles.isEmpty()) {
            jwtBuilder.claim("roles", roles);
        }

        return jwtBuilder.build();
    }
}