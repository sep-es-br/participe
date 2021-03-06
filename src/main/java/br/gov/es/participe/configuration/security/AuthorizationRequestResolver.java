package br.gov.es.participe.configuration.security;

import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

public class AuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private final OAuth2AuthorizationRequestResolver delegatedRequestResolver;

    public AuthorizationRequestResolver(ClientRegistrationRepository clientRegistrationRepository, String authorizeUri) {
        this.delegatedRequestResolver = new DefaultOAuth2AuthorizationRequestResolver(clientRegistrationRepository, authorizeUri);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        OAuth2AuthorizationRequest req = delegatedRequestResolver.resolve(request);
        return customizeRequest(req);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        OAuth2AuthorizationRequest req = delegatedRequestResolver.resolve(request, clientRegistrationId);
        return customizeRequest(req);
    }

    private OAuth2AuthorizationRequest customizeRequest(OAuth2AuthorizationRequest request) {
        if (request != null) {
            return OAuth2AuthorizationRequest
                    .from(request)
                    .additionalParameters(additionalParams(request, isASocialRequest(request)))
                    .build();
        }

        return null;
    }

    private boolean isASocialRequest(OAuth2AuthorizationRequest request) {
        return request.getRedirectUri().contains("facebook") || request.getRedirectUri().contains("google") ||
                request.getRedirectUri().contains("twitter");
    }

    private Map<String, Object> additionalParams(OAuth2AuthorizationRequest request, boolean isSocial) {
        Map<String, Object> params = new HashMap<>(request.getAdditionalParameters());
        if (isSocial) {
            params.put(OAuth2ParameterNames.RESPONSE_TYPE, request.getResponseType().getValue());
        } else {
            params.put(OAuth2ParameterNames.RESPONSE_TYPE, request.getResponseType().getValue() + " id_token token");
        }
        return params;
    }

}
