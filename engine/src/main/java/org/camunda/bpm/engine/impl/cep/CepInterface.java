package org.camunda.bpm.engine.impl.cep;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.persistence.entity.EventSubscriptionManager;
import org.camunda.bpm.engine.impl.persistence.entity.CepEventSubscriptionEntity;

import java.util.List;

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
}
