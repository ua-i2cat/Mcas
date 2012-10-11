package cat.i2cat.mcaslite.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.sun.jersey.spi.resource.Singleton;

import cat.i2cat.mcaslite.config.model.TranscoRequest;
import cat.i2cat.mcaslite.exceptions.MCASException;
import cat.i2cat.mcaslite.management.TranscoHandler;
import cat.i2cat.mcaslite.utils.RequestUtils;

@Singleton
@Path("/transco")
public class TranscoService {
	
	@Context
	private static ServletContext context;
	
	private TranscoHandler transcoH;
	private Thread managerTh;
	
	public TranscoService() throws MCASException{
		transcoH = new TranscoHandler();
		managerTh = new Thread(transcoH);
		managerTh.setName("MainManager");
		managerTh.setDaemon(true);
		managerTh.start();
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addTransco(TranscoRequest request) throws MCASException {
		try {
			if (!RequestUtils.isValidSrcUri(new URI(request.getSrc())) || !RequestUtils.isValidDestination(new URI(request.getDst()))) {
				return Response.status(Response.Status.BAD_REQUEST).entity("Check source and destination.").build();
			}
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return Response.status(Response.Status.BAD_REQUEST).entity("Check source and destination.").build();
		}
		if (transcoH.putRequest(request)){
			return Response.status(Response.Status.CREATED).entity(request.getIdStr()).build();
		} else {
			return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity("System overloaded, wait and retry.").build();
		}
	}
	

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String getStatus(@QueryParam("id") String idStr){
		try {
			String state = transcoH.getState(UUID.fromString(idStr));
			if (state == null){
				throw new WebApplicationException(Response.Status.NOT_FOUND);
			} else {
				return state;
			}
		} catch (IllegalArgumentException e){
			e.printStackTrace();
			throw new WebApplicationException(Response.Status.BAD_REQUEST);
		} catch (MCASException e){
			e.printStackTrace();
			throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
		}
	}
	
	@GET
	@Path("uris")
	@Produces(MediaType.APPLICATION_JSON)
	public String getDestinationUris(@QueryParam("id") String idStr){
		try {
			TranscoRequest request = transcoH.getRequest(UUID.fromString(idStr));
			if (request == null){
				throw new WebApplicationException(Response.Status.NOT_FOUND);
			} else {
				return RequestUtils.destinationJSONbuilder(request);
			}
		} catch (IllegalArgumentException e){
			e.printStackTrace();
			throw new WebApplicationException(Response.Status.BAD_REQUEST);
		} catch (MCASException e) {
			e.printStackTrace();
			throw new WebApplicationException(Response.Status.NO_CONTENT);
		}	
	}
}
