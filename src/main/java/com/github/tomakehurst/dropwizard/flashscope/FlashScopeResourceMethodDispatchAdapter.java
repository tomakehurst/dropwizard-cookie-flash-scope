package com.github.tomakehurst.dropwizard.flashscope;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.spi.container.ResourceMethodDispatchAdapter;
import com.sun.jersey.spi.container.ResourceMethodDispatchProvider;
import com.sun.jersey.spi.dispatch.RequestDispatcher;

import javax.ws.rs.ext.Provider;

import static javax.ws.rs.core.HttpHeaders.SET_COOKIE;

@Provider
public class FlashScopeResourceMethodDispatchAdapter implements ResourceMethodDispatchAdapter {

    private final FlashScopeConfig config;
    private final ObjectMapper objectMapper;

    public FlashScopeResourceMethodDispatchAdapter(FlashScopeConfig config, ObjectMapper objectMapper) {
        this.config = config;
        this.objectMapper = objectMapper;
    }

    public FlashScopeResourceMethodDispatchAdapter(ObjectMapper objectMapper) {
        this(new FlashScopeConfig(), objectMapper);
    }

    @Override
    public ResourceMethodDispatchProvider adapt(ResourceMethodDispatchProvider provider) {
        return new FlashScopeResourceMethodDispatchProvider(config, objectMapper, provider);
    }

    public static class FlashScopeResourceMethodDispatchProvider implements ResourceMethodDispatchProvider {

        private final ResourceMethodDispatchProvider provider;
        private final ObjectMapper objectMapper;
        private final FlashScopeConfig config;


        public FlashScopeResourceMethodDispatchProvider(FlashScopeConfig config, ObjectMapper objectMapper, ResourceMethodDispatchProvider provider) {
            this.config = config;
            this.objectMapper = objectMapper;
            this.provider = provider;
        }

        @Override
        public RequestDispatcher create(AbstractResourceMethod method) {
            final RequestDispatcher dispatcher = provider.create(method);
            if (dispatcher == null) {
                return null;
            }

            return new RequestDispatcher() {

                @Override
                public void dispatch(Object resource, HttpContext context) {
                    boolean incomingFlashScope = new Flash(objectMapper, context, config).get(Object.class).isPresent();

                    dispatcher.dispatch(resource, context);

                    Flash flash = (Flash) context.getProperties().get(Flash.class.getName());
                    if (flash != null && !flash.isEmpty()) {
                        context.getResponse().getHttpHeaders().add(SET_COOKIE, flash.asCookie());
                    } else if (incomingFlashScope) {
                        context.getResponse().getHttpHeaders().add(SET_COOKIE, Flash.expireImmediately(config));
                    }
                }
            };
        }
    }

}