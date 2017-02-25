package com.github.zhirayrized.keycloak.events.jms;

import org.keycloak.Config;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.events.EventType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Zhirayr Kazarosyan
 */
public class KeycloakJMSEventListenerProviderFactory implements EventListenerProviderFactory {
    private final Set<EventType> excludedEvents = new HashSet<>();

    @Override
    public EventListenerProvider create(final KeycloakSession keycloakSession) {
        return new KeycloakJMSEventListenerProvider(Collections.unmodifiableSet(excludedEvents));
    }

    @Override
    public void init(final Config.Scope config) {
        String[] excludes = config.getArray("excludes");
        if (excludes != null) {
            for (String e : excludes) {
                excludedEvents.add(EventType.valueOf(e));
            }
        }
    }

    @Override
    public void postInit(KeycloakSessionFactory keycloakSessionFactory) {
    }

    @Override
    public void close() {
    }

    @Override
    public String getId() {
        return "jms";
    }
}