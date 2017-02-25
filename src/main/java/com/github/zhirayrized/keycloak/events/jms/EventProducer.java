package com.github.zhirayrized.keycloak.events.jms;

import com.google.gson.Gson;
import org.jboss.logging.Logger;
import org.keycloak.events.Event;
import org.keycloak.events.admin.AdminEvent;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author Zhirayr Kazarosyan
 */
public class EventProducer {
    private static final Logger logger = Logger.getLogger(EventProducer.class);

    private final TopicConnectionFactory connectionFactory;

    private final Topic eventDestinationTopic;

    private final Topic adminEventDestinationTopic;

    public EventProducer() {
        try {
            Properties properties = new Properties();
            try(InputStream input = EventProducer.class.getClassLoader().getResourceAsStream("config.properties")) {
                properties.load(input);
            } catch (IOException e) {
                logger.warn("Could not load properties from config.properties. Falling back to default properties {}", e);
                properties.setProperty("keycloak.jms.jndi.connection_factory", "java:/jms/KeycloakBusConnectionFactory");
                properties.setProperty("keycloak.jms.jndi.topic.events", "java:/jms/topic/KeycloakEvents");
                properties.setProperty("keycloak.jms.jndi.topic.admin_events", "java:/jms/topic/KeycloakAdminEvents");
            }
            Context ctx = new InitialContext();
            this.connectionFactory = (TopicConnectionFactory) ctx.lookup(properties.getProperty("keycloak.jms.jndi.connection_factory"));
            this.eventDestinationTopic = (Topic) ctx.lookup(properties.getProperty("keycloak.jms.jndi.topic.events"));
            this.adminEventDestinationTopic = (Topic) ctx.lookup(properties.getProperty("keycloak.jms.jndi.topic.admin_events"));
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
            logger.error(e);
        }
    }

    private static class InstanceHolder {
        private final static EventProducer instance = new EventProducer();
    }
}
