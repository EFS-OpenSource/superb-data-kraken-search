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

import com.efs.sdk.search.utils.TestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ActiveProfiles("test")
class FileHelperTest {

    private FileHelper fileHelper;

    @BeforeEach
    void setup() {
        this.fileHelper = new FileHelper();
    }

    @Test
    void givenFile_whenGetInputContent_thenOk() throws Exception {
        String actual = fileHelper.getInputContent("description.md");
        String expected = TestHelper.getInputContent(FileHelper.class, "description.md");
        assertEquals(expected, actual);
    }

    @Test
    void givenNoFile_whenGetInputContent_thenError() {
        assertThrows(IOException.class, () -> fileHelper.getInputContent("file-does-not-exist.md"));
    }


}
