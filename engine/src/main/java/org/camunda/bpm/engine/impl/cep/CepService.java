package org.camunda.bpm.engine.impl.cep;

/**
 * Created by Matthias on 12.06.2016.
 */

import org.camunda.bpm.engine.impl.ProcessEngineLogger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
// import org.camunda.bpm.engine.impl.cep.CepInterface;

@Path("/event")
public class CepService {

    @POST
    @Path("/REST")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response receiveEvent(String data) {
        ProcessEngineLogger.INSTANCE.processEngineCreated("Receiving event " + data + ".");

        String queryName = CepInterface.queryNamesByUuid.get(data);
        CepInterface.receiveEventMatch(queryName);

        // Transmit Garbage Answer to prevent deletion of query.
        return Response.status(201).entity("Well done!").build();
    }
}
