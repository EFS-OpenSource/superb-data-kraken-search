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

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.message.BasicHeader;
import org.apache.http.ssl.SSLContextBuilder;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLContext;

import static java.lang.String.format;

@Component
@Profile("test")
public class ElasticSearchClientBuilderTest extends ElasticSearchClientBuilder {


    ElasticSearchClientBuilderTest(@Value("${search.elasticsearch.url}") String elasticsearchUrl) {
        super(elasticsearchUrl);
    }

    public RestClient buildRestClient(String token) {
        Header[] defaultHeaders = new Header[]{new BasicHeader("Authorization", format("Bearer %s", token))};
        try {
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, (cert, authType) -> true).build();

            return RestClient.builder(HttpHost.create(elasticsearchUrl))
                    .setDefaultHeaders(defaultHeaders)
                    .setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder.setSSLContext(sslContext).setSSLHostnameVerifier(new NoopHostnameVerifier()))
                    .build();
        } catch (Exception e) {
            // Error should never be thrown
            return null;
        }
    }
}
