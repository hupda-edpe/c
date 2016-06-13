package org.camunda.bpm.engine.impl.cep;

/**
 * Created by Matthias on 12.06.2016.
 */

import org.camunda.bpm.engine.impl.ProcessEngineLogger;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
// import org.camunda.bpm.engine.impl.cep.CepInterface;

@Path("/event")
public class CepService {

    @POST
    @Path("/REST/{queryName}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response receiveEvent(@PathParam("eventName") String queryName, String data) {
        ProcessEngineLogger.INSTANCE.processEngineCreated("Receiving event " + data + ".");

        // String queryName = CepInterface.queryNamesByUuid.get(data);
        CepInterface.receiveEventMatch(queryName);

        // Transmit Garbage Answer to prevent deletion of query.
        return Response.status(201).entity("Well done!").build();
    }

    @GET
    @Path("/welcome")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getWelcome()
    {
        ProcessEngineLogger.INSTANCE.processEngineCreated("Sending Welcome Message!");

        return Response.status(200).entity("Welcome!").build();
    }
}
