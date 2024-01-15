package com.csctracker.securitycore.configs;

import com.csctracker.service.RequestInfo;
import org.slf4j.MDC;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.AccessTokenConverter;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.RemoteTokenServices;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;

import static com.csctracker.configs.CustomHttpTraceFilter.CORRELATION_ID_HEADER_NAME;

public class CustomRemoteToken extends RemoteTokenServices {

    private final RemoteTokenServices remoteTokenServices;

    private RestOperations restTemplate;

    private String checkTokenEndpointUrl;

    private String clientId;

    private String clientSecret;

    private AccessTokenConverter tokenConverter = new DefaultAccessTokenConverter();

    public CustomRemoteToken(String clientId, String clientSecret, String checkTokenEndpointUrl) {
        RemoteTokenServices tokenServices = new RemoteTokenServices();
        tokenServices.setClientId("accounts");
        tokenServices.setClientSecret("accounts");
        tokenServices.setCheckTokenEndpointUrl(checkTokenEndpointUrl);
        this.remoteTokenServices = tokenServices;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.checkTokenEndpointUrl = checkTokenEndpointUrl;
        restTemplate = new RestTemplate();
        ((RestTemplate) restTemplate).setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            // Ignore 400
            public void handleError(ClientHttpResponse response) throws IOException {
                if (response.getRawStatusCode() != 400) {
                    super.handleError(response);
                }
            }
        });
    }

    @Override
    @Cacheable("oauth2")
    public OAuth2Authentication loadAuthentication(String accessToken) throws AuthenticationException,
            InvalidTokenException {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<String, String>();
        formData.add("token", accessToken);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", getAuthorizationHeader(clientId, clientSecret));
        Map<String, Object> map = postForMap(checkTokenEndpointUrl, formData, headers);

        if (map.containsKey("error")) {
            if (logger.isDebugEnabled()) {
                logger.debug("check_token returned error: " + map.get("error"));
            }
            throw new InvalidTokenException(accessToken);
        }

        // gh-838
        if (map.containsKey("active") && !"true".equals(String.valueOf(map.get("active")))) {
            logger.debug("check_token returned active attribute: " + map.get("active"));
            throw new InvalidTokenException(accessToken);
        }

        return tokenConverter.extractAuthentication(map);
    }

    @Override
    public OAuth2AccessToken readAccessToken(String accessToken) {
        return remoteTokenServices.readAccessToken(accessToken);
    }


    private String getAuthorizationHeader(String clientId, String clientSecret) {

        if (clientId == null || clientSecret == null) {
            logger.warn("Null Client ID or Client Secret detected. Endpoint that requires authentication will reject request with 401 error.");
        }

        String creds = String.format("%s:%s", clientId, clientSecret);
        return "Basic " + encodeBase64Binary(creds.getBytes(StandardCharsets.UTF_8));
    }

    @SuppressWarnings(value = "unchecked")
    private Map<String, Object> postForMap(String path, MultiValueMap<String, String> formData, HttpHeaders headers) {
        if (headers.getContentType() == null) {
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        }
        headers.setAccept(Collections.singletonList(MediaType.parseMediaType(MediaType.APPLICATION_JSON_VALUE)));

        if (RequestInfo.getHeaders().containsKey(CORRELATION_ID_HEADER_NAME)) {
            headers.add(CORRELATION_ID_HEADER_NAME, RequestInfo.getHeaders().get(CORRELATION_ID_HEADER_NAME));
        }

        return restTemplate.exchange(path, HttpMethod.POST, new HttpEntity<>(formData, headers), Map.class).getBody();
    }

    private static String encodeBase64Binary(byte[] encode) {
        Base64.Encoder decoder = Base64.getEncoder();
        return new String(decoder.encode(encode));
    }

}
