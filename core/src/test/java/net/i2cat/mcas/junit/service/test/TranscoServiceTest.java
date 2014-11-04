package net.i2cat.mcas.junit.service.test;

import java.util.UUID;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import net.i2cat.mcas.config.model.TRequest;
import net.i2cat.mcas.exceptions.MCASException;
import net.i2cat.mcas.management.TranscoHandler;
import net.i2cat.mcas.service.TranscoService;
import net.i2cat.mcas.utils.RequestUtils;

import static org.powermock.api.easymock.PowerMock.resetAll;
import static org.powermock.api.easymock.PowerMock.expectNew;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.anyObject;

@RunWith(PowerMockRunner.class)
@PrepareForTest({TranscoService.class, RequestUtils.class})
public class TranscoServiceTest {
	
	private static TRequest requestKO1; 
	private static TRequest requestOK1;
	private static TRequest requestKO2;

	@BeforeClass
	public static void setup(){
		requestKO1 = TRequest.getEqualRequest(UUID.randomUUID());
		requestKO1.setSrc("http://www.doesntExsist.fake");
		requestKO1.setDst("file:///fakeDst");
		requestOK1 = TRequest.getEqualRequest(UUID.randomUUID());
		requestOK1.setSrc("http://www.google.cat");
		requestOK1.setDst("file:///fakeDst");
		requestKO2 = TRequest.getEqualRequest(UUID.randomUUID());
		requestKO2.setSrc("http://www.google.cat");
		requestKO2.setDst("fil|///fakeDst");
	}

	@Before
	public void setUp() throws Exception{
		Thread threadMock = createMock(Thread.class);
		expectNew(Thread.class, new Class<?>[]{ Runnable.class },(TranscoHandler) anyObject()).andReturn(threadMock);
		threadMock.setName("MainManager");
		threadMock.setDaemon(true);
		threadMock.start();
	}
	
	@After
	public void tearDown(){
		verifyAll();
		resetAll();
	}
	
	@Test
	public void addTranscoOverLoadedTest() throws Exception {
		TranscoHandler transcoHMock = createMock(TranscoHandler.class);
		expectNew(TranscoHandler.class).andReturn(transcoHMock);
		expect(transcoHMock.putRequest(requestOK1)).andReturn(false).once();
		
		replayAll();
		
		TranscoService service = new TranscoService();
		Response response = service.addTransco(requestOK1);
		assertTrue(((String) response.getEntity()).equals("System overloaded, wait and retry."));
	}
	
	@Test
	public void addTranscoTestOkTest() throws Exception {
		TranscoHandler transcoHMock = createMock(TranscoHandler.class);
		expectNew(TranscoHandler.class).andReturn(transcoHMock);
		expect(transcoHMock.putRequest(requestOK1)).andReturn(true).once();
		
		replayAll();
		
		TranscoService service = new TranscoService();
		Response response = service.addTransco(requestOK1);
		assertTrue(((String) response.getEntity()).equals(requestOK1.getIdStr()));
	}
	
	@Test
	public void addTranscoUnavailableResourceTest() throws Exception {
		TranscoHandler transcoHMock = createMock(TranscoHandler.class);
		expectNew(TranscoHandler.class).andReturn(transcoHMock);
		
		replayAll();
		
		TranscoService service = new TranscoService();
		Response response = service.addTransco(requestKO1);
		assertTrue(((String) response.getEntity()).equals("Check source and destination."));
	}
	
	@Test
	public void addTranscoMalformedUriTest() throws Exception {
		TranscoHandler transcoHMock = createMock(TranscoHandler.class);
		expectNew(TranscoHandler.class).andReturn(transcoHMock);
		
		replayAll();
		
		TranscoService service = new TranscoService();
		Response response = service.addTransco(requestKO2);
		assertTrue(((String) response.getEntity()).equals("Check source and destination."));
	}
	
	@Test(expected = MCASException.class)
	public void addTranscoExceptionUriTest() throws Exception {
		TranscoHandler transcoHMock = createMock(TranscoHandler.class);
		expectNew(TranscoHandler.class).andReturn(transcoHMock);
		expect(transcoHMock.putRequest(requestOK1)).andThrow(new MCASException()).once();
		
		replayAll();
		
		TranscoService service = new TranscoService();
		service.addTransco(requestOK1);
	}
	
	@Test(expected = WebApplicationException.class)
	public void getStateMalformedUUIDTest() throws Exception {
		TranscoHandler transcoHMock = createMock(TranscoHandler.class);
		expectNew(TranscoHandler.class).andReturn(transcoHMock);
		
		replayAll();
		
		TranscoService service = new TranscoService();
		service.getStatus(requestOK1.getIdStr() + "addedToFail");	
	}
	
	@Test(expected = WebApplicationException.class)
	public void getStateNotFoundTest() throws Exception {
		TranscoHandler transcoHMock = createMock(TranscoHandler.class);
		expectNew(TranscoHandler.class).andReturn(transcoHMock);
		expect(transcoHMock.getStatus(requestOK1.getId())).andReturn(null).once();
		
		replayAll();
		
		TranscoService service = new TranscoService();
		service.getStatus(requestOK1.getIdStr());
	}
	
	@Test(expected = WebApplicationException.class)
	public void getStateSearchingExceptionTest() throws Exception {
		TranscoHandler transcoHMock = createMock(TranscoHandler.class);
		expectNew(TranscoHandler.class).andReturn(transcoHMock);
		expect(transcoHMock.getStatus(requestOK1.getId())).andThrow(new MCASException()).once();
		
		replayAll();
		
		TranscoService service = new TranscoService();
		service.getStatus(requestOK1.getIdStr());
	}
	
//	@Test
//	public void getStateTest() throws Exception {
//		TranscoHandler transcoHMock = createMock(TranscoHandler.class);
//		expectNew(TranscoHandler.class).andReturn(transcoHMock);
//		expect(transcoHMock.getStatus(requestOK1.getId())).andReturn(Status.CREATED.getName()).once();
//		
//		replayAll();
//		
//		TranscoService service = new TranscoService();
//		String state = service.getStatus(requestOK1.getIdStr());
//		assertEquals(state, Status.CREATED.getName());
//	}
	
	@Test(expected = WebApplicationException.class)
	public void getUrisSearchFailTest() throws Exception {
		TranscoHandler transcoHMock = createMock(TranscoHandler.class);
		expectNew(TranscoHandler.class).andReturn(transcoHMock);
		expect(transcoHMock.getRequest(requestOK1.getId())).andThrow(new MCASException()).once();
		
		replayAll();
		
		TranscoService service = new TranscoService();
		service.getDestinationUris(requestOK1.getIdStr());
	}
	
	@Test(expected = WebApplicationException.class)
	public void getUrisMalformedUrisTest() throws Exception {
		TranscoHandler transcoHMock = createMock(TranscoHandler.class);
		expectNew(TranscoHandler.class).andReturn(transcoHMock);
		expect(transcoHMock.getRequest(requestKO2.getId())).andReturn(requestKO2);
				
		replayAll();
		
		TranscoService service = new TranscoService();
		service.getDestinationUris(requestKO2.getIdStr());
	}
	
	@Test(expected = WebApplicationException.class)
	public void getUrisNotFoundTest() throws Exception {
		TranscoHandler transcoHMock = createMock(TranscoHandler.class);
		expectNew(TranscoHandler.class).andReturn(transcoHMock);
		expect(transcoHMock.getRequest(requestOK1.getId())).andReturn(null);
				
		replayAll();
		
		TranscoService service = new TranscoService();
		service.getDestinationUris(requestOK1.getIdStr());
	}
	
	@Test
	public void getUrisTest() throws Exception {
		TranscoHandler transcoHMock = createMock(TranscoHandler.class);
		expectNew(TranscoHandler.class).andReturn(transcoHMock);
		expect(transcoHMock.getRequest(requestOK1.getId())).andReturn(requestOK1);
		
		mockStatic(RequestUtils.class);
		expect(RequestUtils.destinationJSONbuilder((TRequest) anyObject())).andReturn("fakeJson");
		
		replayAll();
		
		TranscoService service = new TranscoService();
		String uris = service.getDestinationUris(requestOK1.getIdStr());
		assertEquals("fakeJson", uris);
	}
	
	
}
