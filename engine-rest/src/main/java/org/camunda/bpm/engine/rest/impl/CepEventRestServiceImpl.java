package org.camunda.bpm.engine.rest.impl;

/**
 * Created by Matthias on 12.06.2016.
 */

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.camunda.bpm.engine.impl.cep.CepInterface;
import org.camunda.bpm.engine.rest.CepEventRestService;

// import org.camunda.bpm.engine.impl.cep.CepInterface;

public class CepEventRestServiceImpl extends AbstractRestProcessEngineAware implements CepEventRestService {
    public CepEventRestServiceImpl(String engineName, ObjectMapper objectMapper) {
        super(engineName, objectMapper);
    }

    @Override
    public Response receiveEvent(String queryName, String data) {
        ProcessEngineLogger.INSTANCE.processEngineCreated("Receiving event " + data + ".");

        // String queryName = CepInterface.queryNamesByUuid.get(data);
        CepInterface.receiveEventMatch(queryName);

        // Transmit Garbage Answer to prevent deletion of query.
        return Response.status(201).entity("Well done!").build();
    }

    @Override
    public String getWelcome()
    {
        ProcessEngineLogger.INSTANCE.processEngineCreated("Sending Welcome Message!");
        CepInterface.receiveEventMatch("myQuery");

        return "Welcome!";
        // return Response.status(200).entity("Welcome!").build();
    }
}
