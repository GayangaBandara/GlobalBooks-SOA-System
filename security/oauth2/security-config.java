// SecurityConfig.java - Spring Security OAuth2 Configuration
package com.globalbooks.orders.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/v1/orders/health").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/api/v1/orders/**").authenticated()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            );
        
        // Allow H2 console in development
        http.headers().frameOptions().disable();
        
        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            // Extract authorities from JWT claims
            return jwt.getClaimAsStringList("authorities").stream()
                .map(authority -> new SimpleGrantedAuthority(authority))
                .collect(Collectors.toList());
        });
        return converter;
    }
}

// ServerPasswordCallback.java - WS-Security Password Callback
package com.globalbooks.security;

import org.apache.ws.security.WSPasswordCallback;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ServerPasswordCallback implements CallbackHandler {
    
    private static final Map<String, String> passwords = new HashMap<>();
    
    static {
        passwords.put("catalog-service", "catalog123");
        passwords.put("admin", "admin123");
        passwords.put("client", "client123");
    }

    @Override
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        for (Callback callback : callbacks) {
            if (callback instanceof WSPasswordCallback) {
                WSPasswordCallback pc = (WSPasswordCallback) callback;
                String password = passwords.get(pc.getIdentifier());
                if (password != null) {
                    pc.setPassword(password);
                } else {
                    throw new UnsupportedCallbackException(callback, 
                        "Unknown identifier: " + pc.getIdentifier());
                }
            } else {
                throw new UnsupportedCallbackException(callback, 
                    "Unrecognized Callback");
            }
        }
    }
}

<!-- security-policy.xml - WS-Security Policy for SOAP -->
<?xml version="1.0" encoding="UTF-8"?>
<wsp:Policy xmlns:wsp="http://schemas.xmlsoap.org/ws/2004/09/policy"
            xmlns:wsu="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd"
            xmlns:sp="http://schemas.xmlsoap.org/ws/2005/07/securitypolicy"
            wsu:Id="CatalogServicePolicy">
    
    <wsp:ExactlyOne>
        <wsp:All>
            <sp:SymmetricBinding>
                <wsp:Policy>
                    <sp:ProtectionToken>
                        <wsp:Policy>
                            <sp:UsernameToken sp:IncludeToken="http://schemas.xmlsoap.org/ws/2005/07/securitypolicy/IncludeToken/AlwaysToRecipient">
                                <wsp:Policy>
                                    <sp:WssUsernameToken10/>
                                </wsp:Policy>
                    </sp:ProtectionToken>
                    <sp:AlgorithmSuite>
                        <wsp:Policy>
                            <sp:Basic256/>
                        </wsp:Policy>
                    </sp:AlgorithmSuite>
                    <sp:Layout>
                        <wsp:Policy>
                            <sp:Strict/>
                        </wsp:Policy>
                    </sp:Layout>
                    <sp:IncludeTimestamp/>
                    <sp:OnlySignEntireHeadersAndBody/>
                </wsp:Policy>
            </sp:SymmetricBinding>
            
            <sp:Wss10>
                <wsp:Policy>
                    <sp:MustSupportRefKeyIdentifier/>
                    <sp:MustSupportRefIssuerSerial/>
                </wsp:Policy>
            </sp:Wss10>
            
            <sp:SignedParts>
                <sp:Body/>
            </sp:SignedParts>
            
        </wsp:All>
    </wsp:ExactlyOne>
</wsp:Policy>

<!-- server-crypto.properties - Cryptographic Properties -->
# Keystore Configuration
org.apache.ws.security.crypto.provider=org.apache.ws.security.components.crypto.Merlin
org.apache.ws.security.crypto.merlin.keystore.type=JKS
org.apache.ws.security.crypto.merlin.keystore.password=storepass
org.apache.ws.security.crypto.merlin.keystore.file=server-keystore.jks
org.apache.ws.security.crypto.merlin.keystore.alias=server
org.apache.ws.security.crypto.merlin.cert.provider=org.bouncycastle.jce.provider.BouncyCastleProvider

# Truststore Configuration
org.apache.ws.security.crypto.merlin.truststore.type=JKS
org.apache.ws.security.crypto.merlin.truststore.password=storepass
org.apache.ws.security.crypto.merlin.truststore.file=server-truststore.jks