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