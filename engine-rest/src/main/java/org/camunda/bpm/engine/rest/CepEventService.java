package org.camunda.bpm.engine.rest;

import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cep.CepInterface;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by Matthias on 13.06.2016.
 */
public interface CepEventService {

    public static final String PATH = "/event-service";


    @POST
        @Path("/REST/{queryName}")
        @Consumes(MediaType.APPLICATION_JSON)
        public Response receiveEvent(@PathParam("eventName") String queryName, String data);

        @GET
        @Path("/welcome")
        @Produces(MediaType.TEXT_PLAIN)
        public Response getWelcome();
}
