package com.globalbooks.catalog;

import org.apache.cxf.ws.security.wss4j.WSS4JInInterceptor;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;
import org.apache.wss4j.common.ConfigurationConstants;
import org.apache.wss4j.common.WSS4JConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class WSSecurityConfig {

    @Bean
    public WSS4JInInterceptor wss4jInInterceptor() {
        Map<String, Object> inProps = new HashMap<>();

        // UsernameToken validation
        inProps.put(ConfigurationConstants.ACTION, ConfigurationConstants.USERNAME_TOKEN);
        inProps.put(ConfigurationConstants.PASSWORD_TYPE, WSS4JConstants.PW_TEXT);
        inProps.put(ConfigurationConstants.PW_CALLBACK_CLASS, "com.globalbooks.security.ServerPasswordCallback");

        // Signature validation
        inProps.put(ConfigurationConstants.ACTION, ConfigurationConstants.USERNAME_TOKEN + " " + ConfigurationConstants.SIGNATURE);
        inProps.put(ConfigurationConstants.SIG_PROP_FILE, "server-crypto.properties");

        // Timestamp validation
        inProps.put(ConfigurationConstants.ACTION, ConfigurationConstants.USERNAME_TOKEN + " " +
                ConfigurationConstants.SIGNATURE + " " + ConfigurationConstants.TIMESTAMP);

        return new WSS4JInInterceptor(inProps);
    }

    @Bean
    public WSS4JOutInterceptor wss4jOutInterceptor() {
        Map<String, Object> outProps = new HashMap<>();

        // UsernameToken creation
        outProps.put(ConfigurationConstants.ACTION, ConfigurationConstants.USERNAME_TOKEN);
        outProps.put(ConfigurationConstants.PASSWORD_TYPE, WSS4JConstants.PW_TEXT);
        outProps.put(ConfigurationConstants.PW_CALLBACK_CLASS, "com.globalbooks.security.ServerPasswordCallback");
        outProps.put(ConfigurationConstants.USER, "catalog-service");

        // Signature creation
        outProps.put(ConfigurationConstants.ACTION, ConfigurationConstants.USERNAME_TOKEN + " " + ConfigurationConstants.SIGNATURE);
        outProps.put(ConfigurationConstants.SIG_PROP_FILE, "server-crypto.properties");
        outProps.put(ConfigurationConstants.SIG_KEY_ID, "DirectReference");

        // Timestamp creation
        outProps.put(ConfigurationConstants.ACTION, ConfigurationConstants.USERNAME_TOKEN + " " +
                ConfigurationConstants.SIGNATURE + " " + ConfigurationConstants.TIMESTAMP);

        return new WSS4JOutInterceptor(outProps);
    }
}