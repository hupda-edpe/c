package org.camunda.bpm.engine.impl.cep;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.ws.rs.client.*;
import javax.ws.rs.core.Response;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.persistence.entity.EventSubscriptionManager;
import org.camunda.bpm.engine.impl.persistence.entity.CepEventSubscriptionEntity;

import java.util.List;

import com.google.gson.Gson;
import java.util.List;
import java.util.Map;

/**
 * Created by lucas on 6/12/16.
 */


public class CepInterface {
    public static String notificationPath = "";
    public static String eventPostApi = "/event/REST/";
    public static String unicorn_url = "http://heinrich5991.de/Unicorn/REST";
    public static String eventQueryApi = "/EventQuery/REST/";
    public static Map<String, String> queryNamesByUuid;
    public static void initialize()
    {
        try {
            InetAddress IP = InetAddress.getLocalHost();
            notificationPath = IP.getHostAddress() + eventPostApi;
        } catch (UnknownHostException e) {
            // e.printStackTrace();
        }

    }

    public static void registerQuery(String queryName, String queryCode) {
        ProcessEngineLogger.INSTANCE.processEngineCreated("Registering query " + queryName);
        /* String queryJSON = queryToJSON(queryCode, notificationPath + queryName);

        // TODO: Fix blocking waiting.
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(unicorn_url + eventQueryApi);
        ProcessEngineLogger.INSTANCE.processEngineCreated("Will send query " + queryName);

        Response response = target.request()
                .post(javax.ws.rs.client.Entity.json(queryJSON));
        String uuid = response.getEntity().toString();
        queryNamesByUuid.put(uuid, queryName);
        ProcessEngineLogger.INSTANCE.processEngineCreated("Registered query " + queryName);
        */
    }

    public static void unregisterQuery(String queryName) {
        ProcessEngineLogger.INSTANCE.processEngineCreated("Unregistering query " + queryName + ". NOT IMPLEMENTED.");
    }

  public static void receiveEventMatch(String queryName) {
    final EventSubscriptionManager eventSubscriptionManager = Context.getCommandContext().getEventSubscriptionManager();

    // trigger all event subscriptions for the signal (start and intermediate)
    List<CepEventSubscriptionEntity> catchCepEventSubscriptions = eventSubscriptionManager
        .findCepEventSubscriptionsByQueryName(queryName);
    for (CepEventSubscriptionEntity cepEventSubscriptionEntity : catchCepEventSubscriptions) {
      // TODO: check whether the subscription entity is active; implement async
      cepEventSubscriptionEntity.eventReceived(null, false);
    }
  }

    public static void sendEvent() {
        ProcessEngineLogger.INSTANCE.processEngineCreated("Creating event. NOT IMPLEMENTED.");
    }


    // The following is taken from Unicorn Source.
    public static String queryToJSON(String queryString, String notificationPath) {
        EventQueryJsonForRest json = new EventQueryJsonForRest();
        json.setQueryString(queryString);
        json.setNotificationPath(notificationPath);

        Gson gson = new Gson();
        return gson.toJson(json);
    }

    private static class EventQueryJsonForRest {
        private String notificationPath;
        private String queryString;

        public String getQueryString() {
            return queryString;
        }

        public void setQueryString(String queryString) {
            this.queryString = queryString;
        }

        public String getNotificationPath() {
            return notificationPath;
        }

        public void setNotificationPath(String notificationPath) {
            this.notificationPath = notificationPath;
        }
    }

}
