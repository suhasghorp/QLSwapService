package com.qlservices.adapters;

import io.quarkus.jsonb.JsonbConfigCustomizer;
import javax.inject.Singleton;
import javax.json.bind.JsonbConfig;

@Singleton
public class VanillaSwapSerializerRegistrationCustomizer implements JsonbConfigCustomizer {

    public void customize(JsonbConfig config) {
        config.withAdapters(new VanillaSwapAdapter());
    }
}
