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
}