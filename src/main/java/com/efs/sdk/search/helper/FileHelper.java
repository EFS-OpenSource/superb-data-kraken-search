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

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static java.lang.String.format;

@Component
public class FileHelper {

    public String getInputContent(String filepath) throws IOException {
        try (InputStream is = getInputStream(filepath)) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private InputStream getInputStream(String filePath) throws IOException {
        InputStream in = getClass().getResourceAsStream("/" + filePath);
        if (in == null) {
            throw new IOException(
                    format("Could not load file '%s'", filePath));
        }
        return in;
    }
}
