package com.github.tomakehurst.dropwizard.flashscope;

import com.codahale.dropwizard.testing.ResourceTest;
import com.google.common.collect.ImmutableMap;
import com.sun.jersey.api.client.ClientResponse;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Map;

import static com.github.tomakehurst.dropwizard.flashscope.TestUtils.flashCookieIn;
import static com.github.tomakehurst.dropwizard.flashscope.TestUtils.hasCookieWithName;
import static com.github.tomakehurst.dropwizard.flashscope.FlashScope.COOKIE_NAME;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class FlashScopeTest extends ResourceTest {

    @Override
    protected void setUpResources() throws Exception {
        addResource(new FlashScopeTestResource());
        addProvider(new FlashScopeInjectableProvider(getObjectMapperFactory(), new FlashScopeConfig()));
        addProvider(new FlashScopeResourceMethodDispatchAdapter(getObjectMapperFactory()));
    }

    @Test
    public void setsFlashScopeContents() throws Exception {
        ClientResponse response = client()
                .resource("/flash-test")
                .post(ClientResponse.class);

        assertThat(response.getCookies(), hasCookieWithName(COOKIE_NAME));
        String decodedValue = URLDecoder.decode(flashCookieIn(response).getValue(), "utf-8");
        assertThat(decodedValue, containsString("It worked"));
    }

    @Test
    public void doesNotSetFlashCookieIfFlashOutIsEmpty() {
        ClientResponse response = client()
                .resource("/flash-empty")
                .post(ClientResponse.class);

        assertThat(response.getCookies(), not(hasCookieWithName(COOKIE_NAME)));
    }

    @Test
    public void retrievesFlashScopeContents() throws Exception {
        String message = client()
                .resource("/flash-return")
                .cookie(new NewCookie(COOKIE_NAME,
                        URLEncoder.encode("{\"actionMessage\":\"Flash aaahhh-ahhhhh\"}", "utf-8")))
                .get(String.class);

        assertThat(message, is("Flash aaahhh-ahhhhh"));
    }

    @Test
    public void canReadNestedValues() throws Exception {
        String message = client()
                .resource("/flash-nested-value")
                .cookie(new NewCookie(COOKIE_NAME,
                        URLEncoder.encode(
                                "{                                  \n" +
                                "  \"outer\": {                     \n" +
                                "    \"inner\": \"Inner value\"     \n" +
                                "  }                                \n" +
                                "}",
                                "utf-8")))
                .get(String.class);

        assertThat(message, is("Inner value"));
    }

    @Test
    public void immediatelyExpiresPreviousFlashCookie() throws Exception {
        ClientResponse response = client()
                .resource("/flash-return")
                .cookie(new NewCookie(COOKIE_NAME,
                    URLEncoder.encode("{\"actionMessage\":\"Should not see this\"}", "utf-8")))
                .get(ClientResponse.class);

        assertThat(response.getCookies(), hasCookieWithName(COOKIE_NAME));
        assertThat(flashCookieIn(response).getMaxAge(), is(0));
    }

    @Path("/")
    public static class FlashScopeTestResource {

        @Path("/flash-test")
        @POST
        public Response doSomething(@FlashScope com.github.tomakehurst.dropwizard.flashscope.Flash flash) {
            flash.set(ImmutableMap.of("actionMessage", "It worked"));
            return Response.ok().build();
        }

        @Path("/flash-empty")
        @POST
        public Response doSomethingWithNoFlashOutput(@FlashScope com.github.tomakehurst.dropwizard.flashscope.Flash flash) {
            return Response.ok().build();
        }

        @SuppressWarnings("unchecked")
        @Path("/flash-return")
        @GET
        @Produces("text/plain")
        public String getResult(@FlashScope com.github.tomakehurst.dropwizard.flashscope.Flash flash) {
            Map<String, String> contents = flash.get(Map.class).get();
            return contents.get("actionMessage");
        }

        @SuppressWarnings("unchecked")
        @Path("/flash-nested-value")
        @GET
        @Produces("text/plain")
        public String getNestedValue(@FlashScope com.github.tomakehurst.dropwizard.flashscope.Flash flash) {
            Map<String, Map<String, String>> contents = flash.get(Map.class).get();
            Map<String, String> outer = contents.get("outer");
            return outer.get("inner");
        }

    }

}
