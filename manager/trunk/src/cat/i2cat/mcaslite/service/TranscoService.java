package cat.i2cat.mcaslite.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jackson.map.ObjectMapper;
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
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("{profile}/{level}")
	public Response addCustomTransco(TRequest request, @PathParam("profile") String profile, @PathParam("level") String level) throws MCASException {
		request.setConfig("custom");
		try {
			request.setTConfig(RequestUtils.getCustomTranscoderConfig(profile, level));
		} catch (MCASException e){
			return Response.status(Response.Status.BAD_REQUEST).entity("Check defined configuration.").build();
		}
		return addTransco(request);
	}

	//TODO: status method
	
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
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String getOptions() {
		try {
			ObjectMapper mapper = new ObjectMapper();
			Map<String, List<String>> map = new HashMap<String, List<String>>();
			map.put("profiles", transcoH.getProfiles());
			map.put("levels", transcoH.getLevels());		
			return mapper.writeValueAsString(map);
		} catch (Exception e) {
			throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
		}
	}
}
