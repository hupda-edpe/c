package org.camunda.bpm.engine.rest.impl;

/**
 * Created by Matthias on 12.06.2016.
 */

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.camunda.bpm.engine.impl.cep.CepInterface;
import org.camunda.bpm.engine.rest.CepEventRestService;
import org.camunda.bpm.engine.variable.impl.value.PrimitiveTypeValueImpl;
import org.camunda.bpm.engine.variable.value.TypedValue;

import java.util.HashMap;
import java.util.Map;

// import org.camunda.bpm.engine.impl.cep.CepInterface;

public class CepEventRestServiceImpl extends AbstractRestProcessEngineAware implements CepEventRestService {
  public CepEventRestServiceImpl(String engineName, ObjectMapper objectMapper) {
    super(engineName, objectMapper);
  }

  private void dump(HashMap<String, Object> variables, JsonObject object, String prefix) {
    for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
      String name = entry.getKey();
      JsonElement element = entry.getValue();
      if (element.isJsonPrimitive()) {
        JsonPrimitive jsonValue = element.getAsJsonPrimitive();
        TypedValue javaValue;

        if (jsonValue.isBoolean()) {
          javaValue = new PrimitiveTypeValueImpl.BooleanValueImpl(jsonValue.getAsBoolean());
        } else if (jsonValue.isNumber()) {
          javaValue = new PrimitiveTypeValueImpl.DoubleValueImpl(jsonValue.getAsDouble());
        } else if (jsonValue.isString()) {
          javaValue = new PrimitiveTypeValueImpl.StringValueImpl(jsonValue.getAsString());
        } else {
          throw new RuntimeException("unknown JSON primitive type");
        }
        variables.put(prefix + name, javaValue);
      } else if (element.isJsonObject()) {
        dump(variables, element.getAsJsonObject(), prefix + name + "_");
      } else {
        throw new RuntimeException("unknown JSON type");
      }
    }
  }

  @Override
  public Response receiveEvent(String queryName, String data) {
    HashMap<String, Object> variables = new HashMap<String, Object>();
    JsonObject json = new JsonParser().parse(data).getAsJsonObject();
    dump(variables, json, "");
    ProcessEngineLogger.CEP_LOGGER.receivedEvent(queryName, variables);
    getProcessEngine().getRuntimeService().cepEventReceived(queryName, variables);
    return Response.status(200).entity("Well done!").build();
  }
}
