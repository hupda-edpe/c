package org.camunda.bpm.engine.impl.cep;

import org.camunda.bpm.engine.impl.ProcessEngineLogger;

import java.util.Map;

public class CepLogger extends ProcessEngineLogger {
  public void receivedEvent(String name, Map<String, Object> variables) {
    logInfo("001", "Received event {} ({})", name, variables);
  }
  public void debug(String string) {
    logInfo("002", "{}", string);
  }
  public void welcome() {
    logInfo("003", "Welcome sent");
  }
  public void registeringQuery(String name) {
    logInfo("004", "Registering query {}", name);
  }
  public void unregisteringQuery(String name) {
    logInfo("005", "Unregistering query {}", name);
  }
  public void creatingEvent(String processInstanceId, String activityId, String processName) {
    logInfo("006", "Creating event for {} {} {}", processInstanceId, activityId, processName);
  }
}
