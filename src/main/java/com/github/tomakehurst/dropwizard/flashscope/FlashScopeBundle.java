package com.github.tomakehurst.dropwizard.flashscope;

import com.codahale.dropwizard.ConfiguredBundle;
import com.codahale.dropwizard.setup.Bootstrap;
import com.codahale.dropwizard.setup.Environment;

public class FlashScopeBundle<T> implements ConfiguredBundle<T> {

    @Override
    public final void run(T configuration, Environment environment) throws Exception {
        FlashScopeConfig flashScopeConfig = getFlashScopeConfig(configuration);
        environment.jersey().addProvider(new FlashScopeInjectableProvider(environment.getObjectMapper(), flashScopeConfig));
        environment.jersey().addProvider(new FlashScopeResourceMethodDispatchAdapter(flashScopeConfig, environment.getObjectMapper()));
    }

    @Override
    public final void initialize(Bootstrap<?> bootstrap) {
        // nothing
    }

    /**
     * Template method to override if you want to override any of the flash scope's cookie parameters.
     * @param configuration the service's configuration
     * @return the flash scope configuration
     */
    protected FlashScopeConfig getFlashScopeConfig(T configuration) {
        return new FlashScopeConfig();
    }
}
