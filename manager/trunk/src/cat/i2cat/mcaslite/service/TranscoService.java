package cat.i2cat.mcaslite.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import cat.i2cat.mcaslite.config.model.TRequest;
import cat.i2cat.mcaslite.exceptions.MCASException;
import cat.i2cat.mcaslite.management.TranscoHandler;
import cat.i2cat.mcaslite.utils.DefaultsLoader;
import cat.i2cat.mcaslite.utils.RequestUtils;

import com.sun.jersey.spi.resource.Singleton;

@Singleton
@Path("/transco")
public class TranscoService {
	
	@Context
	private static ServletContext context;
	private TranscoHandler transcoH;
	private Thread managerTh;

	public TranscoService() {
		try {
			(new DefaultsLoader(Paths.get(System.getProperty("mcas.home"), "WEB-INF").toString())).tConfigFeedDefaults();
			transcoH = TranscoHandler.getInstance();
			managerTh = new Thread(transcoH);
			managerTh.setName("MainManager");
			managerTh.setDaemon(true);
			managerTh.start();
		} catch (MCASException e) {
			e.printStackTrace();
			return;
		}
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addTransco(TRequest request) throws MCASException {
		try {
			if (!RequestUtils.isValidSrcUri(new URI(request.getSrc())) || !RequestUtils.isValidDestination(new URI(request.getDst()))) {
				return Response.status(Response.Status.BAD_REQUEST).entity("Check source and destination.").build();
			}
		} catch (URISyntaxException e) {
			return Response.status(Response.Status.BAD_REQUEST).entity("Check source and destination.").build();
		}
		if (transcoH.putRequest(request)){
			return Response.status(Response.Status.CREATED).entity(request.getId()).build();
		} else {
			return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity("System overloaded, wait and retry.").build();
		}
	}
//TODO Merge the two commented methods in only one that returns in a json id, status and uris
//	@GET
//	@Produces(MediaType.TEXT_PLAIN)
//	public String getStatus(@QueryParam("id") String id){
//		try {
//			Status status = transcoH.getStatus(id);
//			if (status == null){
//				throw new WebApplicationException(Response.Status.NOT_FOUND);
//			} else {
//				return status.toString();
//			}
//		} catch (MCASException e){
//			e.printStackTrace();
//			throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
//		}
//	}
//	
//	@GET
//	@Path("uris")
//	@Produces(MediaType.APPLICATION_JSON)
//	public String getDestinationUris(@QueryParam("id") String id){
//		try {
//			TRequest request = transcoH.getRequest(id);
//			if (request == null){
//				throw new WebApplicationException(Response.Status.NOT_FOUND);
//			} else {
//				return RequestUtils.destinationJSONbuilder(request);
//			}
//		} catch (MCASException e) {
//			e.printStackTrace();
//			throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
//		}	
//	}
	
	@POST
	@Path("cancel")
	public Response cancelTransco(@QueryParam("id") String idStr, @DefaultValue("true") @QueryParam("interrupt") boolean interrupt) {
		try {
			TRequest request = transcoH.getRequest(idStr);
			if (request == null){
				return Response.status(Response.Status.NOT_FOUND).build();
			} else {
				if(transcoH.cancelRequest(request, interrupt)){
					return Response.status(Response.Status.OK).build();
				} else {
					return Response.status(Response.Status.NOT_MODIFIED).build();
				}
			}
		} catch (MCASException e) {
			e.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}
}
