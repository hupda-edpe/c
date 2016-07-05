package org.camunda.bpm.engine.rest;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by Matthias on 13.06.2016.
 */
@Produces(MediaType.TEXT_PLAIN)
public interface CepEventRestService {

  public static final String PATH = "/event-service";


  @POST
  @Path("/REST/{queryName}")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response receiveEvent(@PathParam("queryName") String queryName, String data);

  @GET
  @Path("/welcome")
  @Produces(MediaType.TEXT_PLAIN)
  public String getWelcome();
}
