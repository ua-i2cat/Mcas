package cat.i2cat.mcaslite.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.sun.jersey.spi.resource.Singleton;

import cat.i2cat.mcaslite.entities.TranscoRequest;
import cat.i2cat.mcaslite.exceptions.MCASException;
import cat.i2cat.mcaslite.management.TranscoHandler;
import cat.i2cat.mcaslite.utils.RequestValidator;

@Singleton
@Path("/transco")
public class TranscoService {
	
	private TranscoHandler transcoH;
	private Thread managerTh;
	
	public TranscoService(){
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
			if (!RequestValidator.isValidSrcUri(new URI(request.getSrc())) || !RequestValidator.isValidDestination(new URI(request.getDst()))) {
				return Response.status(400).entity("Bad Request: Check source and destination.").build();
			}
		} catch (URISyntaxException e) {
			e.printStackTrace();
			Response.status(Response.Status.BAD_REQUEST).entity("Check source and destination.").build();
		}
		transcoH.putRequest(request);
		return Response.status(Response.Status.CREATED).entity(request.getIdStr()).build();
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
		}
	}
	
}
