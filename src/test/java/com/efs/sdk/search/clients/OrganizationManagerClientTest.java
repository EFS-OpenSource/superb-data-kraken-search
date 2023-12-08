package com.efs.sdk.search.clients;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

class OrganizationManagerClientTest {

    private OrganizationManagerClient client;
    @MockBean
    private Jwt jwt;
    @MockBean
    private RestTemplate restTemplate;
    @MockBean
    private JwtAuthenticationToken token;

    @BeforeEach
    void setup() {
        this.restTemplate = Mockito.mock(RestTemplate.class);
        this.token = Mockito.mock(JwtAuthenticationToken.class);
        this.jwt = Mockito.mock(Jwt.class);
        this.client = new OrganizationManagerClient(restTemplate, "http://idontcare.de/spaceEndpoint");
    }

    @Test
    void givenGetSpacesOk_whenGetSpaces_thenOk() {
        given(token.getToken()).willReturn(jwt);
        given(jwt.getTokenValue()).willReturn("any value");

        List<String> spaces = List.of("orga_space");

        given(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(new ParameterizedTypeReference<List<String>>() {
        }))).willReturn(ResponseEntity.ok(spaces));

        assertEquals(1, client.getAllSpaces(token.getToken().getTokenValue()).size());
    }
}
