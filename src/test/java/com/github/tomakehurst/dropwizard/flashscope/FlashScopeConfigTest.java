package com.github.tomakehurst.dropwizard.flashscope;

import com.codahale.dropwizard.testing.ResourceTest;
import com.codahale.dropwizard.util.Duration;
import com.sun.jersey.api.client.ClientResponse;
import org.junit.Test;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

import static com.github.tomakehurst.dropwizard.flashscope.TestUtils.findCookie;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class FlashScopeConfigTest extends ResourceTest {

    @Override
    protected void setUpResources() throws Exception {
        addResource(new FlashScopeTestResource());
        addProvider(new FlashScopeInjectableProvider(getObjectMapperFactory(), new TestConfig()));
        addProvider(new FlashScopeResourceMethodDispatchAdapter(new TestConfig(), getObjectMapperFactory()));
    }

    @Test
    public void cookieReflectsConfigurationValues() throws Exception {
        ClientResponse response = client()
                .resource("/flash-test")
                .post(ClientResponse.class);

        NewCookie cookie = findCookie(response, "CUSTOM_FLASH");
        assertThat(cookie.getPath(), is("/flash-test"));
        assertThat(cookie.getDomain(), is("flashtown.com"));
        assertThat(cookie.getMaxAge(), is(7));
    }

    static class TestConfig extends FlashScopeConfig {
        @Override
        public String getCookieName() {
            return "CUSTOM_FLASH";
        }

        @Override
        public String getCookiePath() {
            return "/flash-test";
        }

        @Override
        public String getCookieDomain() {
            return "flashtown.com";
        }

        @Override
        public Duration getCookieMaxAge() {
            return Duration.seconds(7);
        }
    }

    @Path("/")
    public static class FlashScopeTestResource {

        @Path("/flash-test")
        @POST
        public Response doSomething(@FlashScope com.github.tomakehurst.dropwizard.flashscope.Flash flash) {
            flash.set("actionMessage");
            return Response.ok().build();
        }
    }
}
