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
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import static org.python.indexer.Util.readFile;

/**
 * Created by lucas on 6/12/16.
 */


public class CepInterface {
  public static String notificationPath = "http://172.18.0.1:8080";
  public static String eventPostApi = "/event/REST/";
  public static Map<String, String> queryUuidByName;
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

      // Separate header and footer via regex.
      Pattern pattern = Pattern.compile("(.*?)(\n\n|\r\n\r\n)(.*)", Pattern.DOTALL);
      Matcher matcher = pattern.matcher(result);
      if (!matcher.matches()) {
        // Assume that there is no main body.
        body = "";
        header = result;
        ProcessEngineLogger.CEP_LOGGER.debug("Unicorn Response - response without body. Header is:\n" + header + "\n");
      } else {
        if (matcher.group(1).equals("")) {
          // This should not occur.
          // Groups are numbered ltr starting with 1, number 0 is for the whole matched string.
          throw new RuntimeException("Unicorn response: Can't split header from body. Response: " + result);
        } else {
          header = matcher.group(1);
          body = matcher.group(3);
        }
      }

      pattern = Pattern.compile("HTTP/(\\d)*\\.(\\d)* (\\d*) .*", Pattern.DOTALL);
      matcher = pattern.matcher(header);
      if (!matcher.matches()) {
        throw new RuntimeException("Unicorn response: Could not parse HTTP status code, header: " + header);
      }
      String status = matcher.group(3);
      if (!status.startsWith("2")) {
        throw new RuntimeException("Unicorn response HTTP Error " + matcher.group(3) + ": " + result);
      }

      in.close();
      out.close();

      Scanner scanner = new Scanner(result);
      while(scanner.hasNextLine()) {
        ProcessEngineLogger.CEP_LOGGER.debug(scanner.nextLine());
      }

    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return body;
  }

  public static void registerQuery(String queryName, String queryCode) {
    ProcessEngineLogger.CEP_LOGGER.registeringQuery(queryName);

    //String queryJSON = queryToJSON(queryCode, notificationPath + "/engine-rest/event-service/REST/" + queryName);
    String queryJSON = queryToJSON("SELECT * FROM foobar", notificationPath + "/engine-rest/event-service/REST/" + queryName);
    String uuid = unicorn("POST", "EventQuery/REST/", queryJSON);
    // TODO: Move this to a proper position
    if (queryUuidByName == null) {
      queryUuidByName = new HashMap<String, String>();
    }
    queryUuidByName.put(queryName, uuid);
  }

  public static void unregisterQuery(String queryName) {
    ProcessEngineLogger.CEP_LOGGER.unregisteringQuery(queryName);

    String uuid = queryUuidByName.get(queryName);
    if (uuid == null) {
      throw new RuntimeException("Unknown query name " + queryName);
    }

    unicorn("DELETE", "EventQuery/REST/" + uuid, "");
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

  public static void sendEvent(String processInstanceId, String activityId, String processName)
  {
    String eventXmlString = genericEventXmlString(processInstanceId, activityId, processName);
    String eventId = unicorn("POST", "Event/", eventXmlString); // Maybe this id will be useful at some point.
    ProcessEngineLogger.CEP_LOGGER.creatingEvent(processInstanceId);
  }

  public static void registerGenericCamundaEventType()
  {
    String xsd;
    try {
      xsd = readFile("camundaGenericEventType.xsd");
    } catch (Exception e)
    {
      throw new RuntimeException(e);
    }

    String schemaName = "CamundaGenericEvent";
    String timestampName = "timestamp";

    EventTypeJson json = new EventTypeJson(xsd, schemaName, timestampName);
    Gson gson = new Gson();
    String jsonString = gson.toJson(json);

    unicorn("POST", "EventType/", jsonString);
  }

  public static String genericEventXmlString(String processInstanceId, String activityId, String processName)
  {
    // This could probably be done better, but at least it's verbose.
    String xml = "";
    xml += "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
    xml += "<CamundaGenericEvent xmlns:xsi=\"http://www.w3.org/2001/XML-Schema-instance\" xsi:noNamespaceSchemaLocation=\"CamundaGenericEvent.xsd\">";
    xml += "  <processInstanceId>" + processInstanceId + "</processInstanceId>";
    xml += "  <activityId>" + activityId+ "</activityId>";
    xml += "  <processName>" + processName + "</processName>";
    xml += "</CamundaGenericEvent>";

    return xml;
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

  private static class EventTypeJson
  {
    private String xsd;
    private String schemaName;
    private String timestampName;

    public EventTypeJson(String xsd, String schemaName, String timestampName)
    {
      this.xsd = xsd;
      this.schemaName = schemaName;
      this.timestampName = timestampName;
    }
  }
}
