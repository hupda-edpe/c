package org.camunda.bpm.engine.impl.cep;

import org.camunda.bpm.engine.impl.ProcessEngineLogger;

public class CepLogger extends ProcessEngineLogger {
  public void receivedEvent(String name) {
    logInfo("001", "Received event {}", name);
  }
  public void debug(String string) {
    logInfo("002", string);
  }
  public void welcome() {
    logInfo("003", "Welcome sent");
  }
  public void registeringQuery(String name) {
    logInfo("004", "Registering query {}", name);
  }
  public void unregisteringQuery(String name) {
    logInfo("005", "Unregistering query {}, NOT IMPLEMENTED", name);
  }
  public void creatingEvent() {
    logInfo("006", "Creating event, NOT IMPLEMENTED");
  }
  public void registerQueryFailed(String name, String code, int status) {
    logInfo("007", "Query registering failed: {} {} {}", status, name, code);
  }
}