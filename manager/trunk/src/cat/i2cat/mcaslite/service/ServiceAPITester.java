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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


import cat.i2cat.mcaslite.entities.TranscoRequest;
import cat.i2cat.mcaslite.utils.RequestValidator;

import com.sun.jersey.spi.resource.Singleton;

@Singleton
@Path("/test/rest")
public class ServiceAPITester {
	
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addTransco(TranscoRequest request) {
		try {
			if (!RequestValidator.isValidSrcUri(new URI(request.getSrc())) || !RequestValidator.isValidDestination(new URI(request.getDst()))) {
				return Response.status(400).entity("Bad Request: Check source and destination.").build();
			}
		} catch (URISyntaxException e) {
			e.printStackTrace();
			Response.status(400).entity("Bad Request: Check source and destination.").build();
		}
		return Response.status(201).entity(request.getIdStr()).build();
	}
	

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String getStatus(@QueryParam("id") String idStr){
		String idTmp = "";
		try {
			UUID id = UUID.fromString(idStr);
			idTmp = id.toString();
		} catch (IllegalArgumentException e){
			e.printStackTrace();
			return "Bad Request: Check your id param.";
		}
		return idTmp;
	}

}


