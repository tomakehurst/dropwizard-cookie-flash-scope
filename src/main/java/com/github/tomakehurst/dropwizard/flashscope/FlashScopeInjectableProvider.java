package com.github.tomakehurst.dropwizard.flashscope;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.model.Parameter;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.server.impl.inject.AbstractHttpContextInjectable;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;


public class FlashScopeInjectableProvider implements InjectableProvider<FlashScope, Parameter> {

    private final ObjectMapper objectMapper;
    private FlashScopeConfig config;

    public FlashScopeInjectableProvider(ObjectMapper objectMapper, FlashScopeConfig config) {
        this.objectMapper = objectMapper;
        this.config = config;
    }

    @Override
    public ComponentScope getScope() {
        return ComponentScope.PerRequest;
    }

    @Override
    public Injectable getInjectable(ComponentContext ic, FlashScope flashScope, final Parameter parameter) {
        return new AbstractHttpContextInjectable<Flash>() {
            @Override
            public Flash getValue(HttpContext context) {
                if (parameter.getParameterClass().equals(Flash.class)) {
                    Flash flash = new Flash(objectMapper, context, config);
                    context.getProperties().put(Flash.class.getName(), flash);
                    return flash;
                }

                return null;
            }
        };
    }
}
