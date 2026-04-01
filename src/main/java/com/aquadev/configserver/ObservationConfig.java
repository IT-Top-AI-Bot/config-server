package com.aquadev.configserver;

import io.micrometer.observation.ObservationPredicate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.observation.ServerRequestObservationContext;

@Configuration
class ObservationConfig {

    @Bean
    public ObservationPredicate noActuatorTracing() {
        return (name, context) -> {
            if (context instanceof ServerRequestObservationContext serverContext) {
                var request = serverContext.getCarrier();

                if (request == null) {
                    return true;
                }

                String path = request.getRequestURI();
                return path == null || !path.startsWith("/actuator");
            }
            return true;
        };
    }
}
