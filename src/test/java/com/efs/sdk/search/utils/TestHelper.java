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
package com.efs.sdk.search.utils;

import org.springframework.security.oauth2.jwt.Jwt;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;

public class TestHelper {

    public static final String ESQUERY_PATH = "esqueries";
    public static final String QUERY_PATH = "queries";
    public static final String RESULT_PATH = "results";
    public static final String ESRESULT_PATH = "esresults";

    public static String getInputContent(String prefix, String filepath) throws IOException {
        return getInputContent(TestHelper.class, prefix, filepath);
    }

    public static String getInputContent(Class<?> clazz, String prefix, String filepath) throws IOException {
        return getInputContent(clazz, format("/%s/%s", prefix, filepath));
    }

    public static String getInputContent(Class<?> clazz, String path) throws IOException {
        try (InputStream is = getInputStream(clazz, path.startsWith("/") ? path : "/" + path)) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static InputStream getInputStream(Class<?> clazz, String filePath) throws IOException {
        InputStream in = clazz.getResourceAsStream(filePath);
        if (in == null) {
            throw new IOException(
                    format("Could not load file '%s'", filePath));
        }
        return in;
    }

    public static Jwt getJwt() {
        List<String> roles = new ArrayList<>();
        roles.add("offline_access");
        roles.add("uma_authorization");
        Jwt.Builder jwtBuilder = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("sub", "user")
                .claim("scope", "openid client_role_react-web-app email profile")
                .claim("roles", roles);

        return jwtBuilder.build();
    }

    public static String getAccessToken(Jwt jwt) {
        return jwt.getTokenValue();
    }

    public static String getAccessToken() {
        Jwt jwt = getJwt();
        return getAccessToken(jwt);
    }
}

