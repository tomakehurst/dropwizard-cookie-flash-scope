package com.github.tomakehurst.dropwizard.flashscope;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import com.sun.jersey.api.core.HttpContext;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.NewCookie;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Map;

public class Flash {

    private final ObjectMapper objectMapper;
    private final HttpContext context;
    private final FlashScopeConfig config;

    private Object value;

    public Flash(ObjectMapper objectMapper, HttpContext context, FlashScopeConfig config) {
        this.objectMapper = objectMapper;
        this.context = context;
        this.config = config;
    }

    public <T> Optional<T> get(Class<T> clazz) {
        Map<String, Cookie> requestCookies = context.getRequest().getCookies();
        if (requestCookies.containsKey(config.getCookieName())) {
            Cookie cookie = requestCookies.get(config.getCookieName());
            try {
                String decodedValue = URLDecoder.decode(cookie.getValue(), "utf-8");
                T value = objectMapper.readValue(decodedValue, clazz);
                return Optional.of(value);
            } catch (UnsupportedEncodingException e) {
                throw new IllegalArgumentException("Bad flash cookie encoding", e);
            } catch (IOException e) {
                throw new IllegalArgumentException("Error deserializing flash cookie value", e);
            }
        }

        return Optional.absent();
    }

    public <T> void set(T value) {
        this.value = value;
    }

    public boolean isEmpty() {
        return value == null;
    }

    public NewCookie asCookie() {
        return createCookie(value);
    }

    private <T> NewCookie createCookie(T value) {
        try {
            String json = objectMapper.writeValueAsString(value);
            return newCookie(config.getCookieName(),
                    config.getCookiePath(),
                    config.getCookieDomain(),
                    (int) config.getCookieMaxAge().toSeconds(),
                    urlEncode(json));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize flash attributes", e);
        }
    }

    public static NewCookie expireImmediately(FlashScopeConfig config) {
        return newCookie(config.getCookieName(),
                config.getCookiePath(),
                config.getCookieDomain(),
                0,
                "{}");
    }

    private static NewCookie newCookie(String name, String path, String domain, int maxAge, String content) {
        return new NewCookie(name,
                content,
                path,
                domain,
                "",
                maxAge,
                false);
    }

    private static String urlEncode(String unencoded) {
        try {
            return URLEncoder.encode(unencoded, "utf-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("utf-8 isn't a supported encoding on this system");
        }
    }
}
