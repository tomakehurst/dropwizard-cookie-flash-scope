package com.github.tomakehurst.dropwizard.flashscope;

import com.codahale.dropwizard.util.Duration;
import com.fasterxml.jackson.annotation.JsonProperty;

public class FlashScopeConfig {

    @JsonProperty
    private String cookieName = FlashScope.COOKIE_NAME;

    @JsonProperty
    private String cookiePath = "/";

    @JsonProperty
    private String cookieDomain = null;

    @JsonProperty
    private Duration cookieMaxAge = Duration.seconds(5);

    public String getCookieName() {
        return cookieName;
    }

    public String getCookiePath() {
        return cookiePath;
    }

    public String getCookieDomain() {
        return cookieDomain;
    }

    public Duration getCookieMaxAge() {
        return cookieMaxAge;
    }
}
