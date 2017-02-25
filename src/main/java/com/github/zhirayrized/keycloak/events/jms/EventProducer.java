package com.github.zhirayrized.keycloak.events.jms;

import com.google.gson.Gson;
import org.keycloak.events.Event;
import org.keycloak.events.admin.AdminEvent;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * @author Zhirayr Kazarosyan
 */
public class EventProducer {
    private final TopicConnectionFactory connectionFactory;

    private final Topic eventDestinationTopic;

    private final Topic adminEventDestinationTopic;

    public EventProducer() {
        try {
            Context ctx = new InitialContext();
            this.connectionFactory = (TopicConnectionFactory) ctx.lookup("java:/jms/KeycloakBusConnectionFactory");
            this.eventDestinationTopic = (Topic) ctx.lookup("java:/jms/topic/KeycloakEvents");
            this.adminEventDestinationTopic = (Topic) ctx.lookup("java:/jms/topic/KeycloakAdminEvents");
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

    public static EventProducer getInstance() {
        return InstanceHolder.instance;
    }

    public void sendEvent(final Event event) {
        sendEvent(event, eventDestinationTopic);
    }

    public void sendEvent(final AdminEvent event) {
        sendEvent(event, adminEventDestinationTopic);
    }

    private void sendEvent(final Object payload, final Topic topic) {
        try (Connection connection = connectionFactory.createConnection();
             Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)) {

            final Message message = session.createMessage();
            message.setStringProperty("MEDIA_TYPE", "application/json");
            message.setStringProperty("BODY", new Gson().toJson(payload));
            session.createProducer(topic).send(message);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    private static class InstanceHolder {
        private final static EventProducer instance = new EventProducer();
    }
}
