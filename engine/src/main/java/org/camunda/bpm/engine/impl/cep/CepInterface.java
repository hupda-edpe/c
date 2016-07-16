package org.camunda.bpm.engine.impl.cep;

import com.google.gson.Gson;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.persistence.entity.CepEventSubscriptionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.EventSubscriptionManager;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

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

  private static String readToEnd(InputStream in) throws IOException {
    byte buffer[] = new byte[2048];
    StringBuilder result = new StringBuilder();
    while (true) {
      int read = in.read(buffer);
      if (read <= 0) {
        break;
      } else {
        result.append(new String(Arrays.copyOfRange(buffer, 0, read), "UTF-8"));
      }
    }
    return result.toString();
  }

  private static String unicorn(String method, String path, String data) {
    String header = "", body = "";
    try {
      String restPath = "/Unicorn/webapi/REST/" + path;
      String sendData = "";
      if (!data.equals("")) {
        sendData = data + "\r\n";
      }
      Socket socket = new Socket("172.18.0.3", 8080);
      DataOutputStream out = new DataOutputStream(socket.getOutputStream());
      out.write((
          method + " " + restPath + " HTTP/1.1\r\n" +
          "User-Agent: camunda\r\n" +
          "Host: localhost\r\n" +
          "Connection: close\r\n" +
          "Content-Type: application/json\r\n" +
          "Content-Length: " + sendData.getBytes("UTF-8").length + "\r\n" +
          "\r\n"
          + sendData
      ).getBytes("UTF-8"));

      DataInputStream in = new DataInputStream(socket.getInputStream());

      String result = readToEnd(in);


      // Seperate header and footer via regex.
      Pattern pattern = Pattern.compile("(.*)??(\n\n|\n\r\n\r)(.*)");
      Matcher matcher = pattern.matcher(result);
      if (!matcher.matches())
      {
        // Assume that there is no main body.
        body = null;
        header = result;
        ProcessEngineLogger.CEP_LOGGER.debug("Unicorn Response - response without body. Header is:\n" + header + "\n");
      } else {
        if (matcher.group(1) == "")
        // This should not occur.
        // Groups are numbered ltr starting with 1, number 0 is for the whole matched string.
        {
          ProcessEngineLogger.CEP_LOGGER.debug("Unicorn Response - Error in splitting header and body. Response was:\n" + result + "\n");
          header = null;
          body = null;
        } else {
          header = matcher.group(1);
          body = matcher.group(3);
        }
      }
      Scanner scanner = new Scanner(header);


      pattern = Pattern.compile("HTTP/(\\d)*\\.(\\d)* (\\d*) .*");
      matcher = pattern.matcher(header);
      if (!matcher.matches()) {
        ProcessEngineLogger.CEP_LOGGER.debug("Unicorn Response - Could not detect http status code. Header is:\n" + header + "\n");
      }
      if (matcher.group(3) != "200")
      {
        // Http Error.
        ProcessEngineLogger.CEP_LOGGER.debug("Unicorn Response - Http error: " + matcher.group(3) + "\n");
      }

      in.close();
      out.close();

      // If we need more information, we should use the scanner class.

      //Scanner scanner = new Scanner(result);
      //while(scanner.hasNextLine()) {
      //  ProcessEngineLogger.CEP_LOGGER.debug(scanner.nextLine());
      //}

    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return body;
  }

  public static void registerQuery(String queryName, String queryCode) {
    ProcessEngineLogger.CEP_LOGGER.registeringQuery(queryName);

    //String queryJSON = queryToJSON(queryCode, notificationPath + "/engine-rest/event-service/REST/" + queryName);
    String queryJSON = queryToJSON("SELECT *", notificationPath + "/engine-rest/event-service/REST/" + queryName);

    unicorn("POST", "EventQuery/REST/", queryJSON);

    /*
    Response response = ClientBuilder.newClient().target(unicornUrl + "/EventQuery/REST").request().post(Entity.json(queryJSON));
    if (response.getStatus() != 200) {
      ProcessEngineLogger.CEP_LOGGER.registerQueryFailed(queryName, queryCode, response.getStatus());
    }
    */
  }

  public static void unregisterQuery(String queryName) {
    ProcessEngineLogger.CEP_LOGGER.unregisteringQuery(queryName);

    unicorn("DELETE", "EventQuery/REST/" + queryName, "");
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

  public static void sendEvent(String processInstanceId) {
    ProcessEngineLogger.CEP_LOGGER.creatingEvent(processInstanceId);
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
