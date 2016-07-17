package org.camunda.bpm.engine.impl.cmd;

import org.camunda.bpm.engine.impl.cep.CepInterface;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;

import java.io.Serializable;
import java.util.Map;


public class CepEventReceivedCmd implements Command<Void>, Serializable {

  protected final String name;
  protected final Map<String, Object> variables;

  public CepEventReceivedCmd(String name, Map<String, Object> variables) {
    this.name = name;
    this.variables = variables;
  }

  @Override
  public Void execute(CommandContext commandContext) {
    CepInterface.receiveEventMatch(name, variables);
    return null;
  }
}