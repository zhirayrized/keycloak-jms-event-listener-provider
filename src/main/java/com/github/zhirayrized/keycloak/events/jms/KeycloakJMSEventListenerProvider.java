package com.github.zhirayrized.keycloak.events.jms;

import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;

import java.util.Set;

/**
 * @author Zhirayr Kazarosyan
 */
public class KeycloakJMSEventListenerProvider implements EventListenerProvider {
    private final Set<EventType> excludedEvents;

    public KeycloakJMSEventListenerProvider(final Set<EventType> excludedEvents) {
        this.excludedEvents = excludedEvents;
    }

    @Override
    public void onEvent(final Event event) {
        if (excludedEvents != null && excludedEvents.contains(event.getType())) {
            return;
        }
        EventProducer.getInstance().sendEvent(event);
    }

    @Override
    public void onEvent(final AdminEvent event, final boolean includeRepresentation) {
        EventProducer.getInstance().sendEvent(event);
    }

    @Override
    public void close() {
    }
}