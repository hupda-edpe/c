package org.camunda.bpm.engine.impl.cmd;

import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cep.CepInterface;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;

import java.io.Serializable;


public class CepEventReceivedCmd implements Command<Void>, Serializable {

  protected final String name;

  public CepEventReceivedCmd(String name) {
    this.name = name;
  }

  @Override
  public Void execute(CommandContext commandContext) {
    CepInterface.receiveEventMatch(name);
    return null;
  }
}