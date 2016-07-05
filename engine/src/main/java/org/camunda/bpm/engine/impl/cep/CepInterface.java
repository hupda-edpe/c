package org.camunda.bpm.engine.impl.cep;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import javax.ws.rs.client.*;
import javax.ws.rs.core.Response;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
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
  public static String notificationPath = "http://172.18.0.1:8008";
  public static String eventPostApi = "/event/REST/";
  //public static String unicornUrl = "http://172.18.0.3:8080/Unicorn/REST";
  public static String unicornUrl = "http://127.0.0.1:8008/Unicorn/REST";
  public static String eventQueryApi = "/EventQuery/REST/";
  public static Map<String, String> queryNamesByUuid;
  public static void initialize() {
    try {
      InetAddress IP = InetAddress.getLocalHost();
      notificationPath = IP.getHostAddress() + eventPostApi;
    } catch (UnknownHostException e) {
      // e.printStackTrace();
    }

  }

  private static void unicorn(String method, String path, String data) {
    try {
      Socket socket = new Socket("localhost", 8008);
      DataOutputStream out = new DataOutputStream(socket.getOutputStream());
      out.write((method + " " + path + " HTTP/1.1\nHostname: 127.0.0.1\n\n" + data + "\n").getBytes("UTF-8"));
      out.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static void registerQuery(String queryName, String queryCode) {
    ProcessEngineLogger.CEP_LOGGER.registeringQuery(queryName);

    String queryJSON = queryToJSON(queryCode, notificationPath + "/engine-rest/event-service/REST/" + queryName);

    try {
      unicorn("POST", "/Unicorn/REST/EventQuery/REST/", "queryJson=" + URLEncoder.encode(queryJSON, "UTF-8"));
    } catch(UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }

    /*Response response = ClientBuilder.newClient().target(unicornUrl + "/EventQuery/REST").request().post(Entity.json(queryJSON));
    if (response.getStatus() != 200) {
      ProcessEngineLogger.CEP_LOGGER.registerQueryFailed(queryName, queryCode, response.getStatus());
    }*/
  }

  public static void unregisterQuery(String queryName) {
    ProcessEngineLogger.CEP_LOGGER.unregisteringQuery(queryName);

    unicorn("DELETE", "/Unicorn/REST/EventQuery/REST/" + queryName, "");
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
    ProcessEngineLogger.CEP_LOGGER.creatingEvent();
  }


  // The following is taken from Unicorn Source.
  public static String queryToJSON(String queryString, String notificationPath) {
    //return "{\"queryString\"=\"" + queryString + "\",\"notificationPath\"=\"" + notificationPath + "\"}";
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
