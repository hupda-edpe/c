package org.camunda.bpm.engine.impl.cep;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;

/**
 * Created by lucas on 6/12/16.
 */
public class CepInterface {
  public static void registerQuery(String queryName, String queryCode) {
    ProcessEngineLogger.INSTANCE.processEngineCreated("Registering query " + queryName);
  }

  public static void unregisterQuery(String queryName) {
    ProcessEngineLogger.INSTANCE.processEngineCreated("Unregistering query " + queryName);
  }

  public static void createEvent() {
    ProcessEngineLogger.INSTANCE.processEngineCreated("Creating event");
  }
}
