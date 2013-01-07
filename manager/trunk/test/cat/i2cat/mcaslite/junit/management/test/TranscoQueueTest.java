package cat.i2cat.mcaslite.junit.management.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import cat.i2cat.mcaslite.config.model.TranscoRequest;
import cat.i2cat.mcaslite.config.model.TranscoRequest.State;
import cat.i2cat.mcaslite.exceptions.MCASException;
import cat.i2cat.mcaslite.management.TranscoQueue;

@RunWith(PowerMockRunner.class)
@PrepareForTest(TranscoQueue.class)
public class TranscoQueueTest {
	
	private static TranscoQueue queue;
	private static TranscoRequest request1st;
	private static TranscoRequest request2nd;
	private static TranscoRequest request3rd;
	
	@BeforeClass
	public static void setUp(){
		queue = TranscoQueue.getInstance();
		
		request1st = TranscoRequest.getEqualRequest(UUID.randomUUID());
		request1st.setState(Status.T_PROCESS);
		
		request2nd = TranscoRequest.getEqualRequest(UUID.randomUUID());
		request2nd.setState(Status.M_PROCESS);
		
		request3rd = TranscoRequest.getEqualRequest(UUID.randomUUID());
		request3rd.setState(Status.MOVING);
	}

	@Before
	public void setup() throws MCASException{
		queue.clearQueue();
		queue.put(request1st);
		queue.put(request2nd);
		queue.put(request3rd);
		queue.put(TranscoRequest.getEqualRequest(UUID.randomUUID()));
		queue.put(TranscoRequest.getEqualRequest(UUID.randomUUID()));
	}
	
	@Test
	public void putRequestTest() throws MCASException {
		assertEquals(5, queue.size());
		queue.put(TranscoRequest.getEqualRequest(UUID.randomUUID()));
		assertEquals(6, queue.size());
	}
	
	@Test(expected = MCASException.class)
	public void putDuplicateRequestTest() throws MCASException {
		queue.put(request1st);
	}
	
	@Test
	public void indexOfTest() {
		assertEquals(1,queue.indexOf(request2nd));
	}
	
	@Test
	public void indexOfNotFoundTest() {
		assertEquals(-1,queue.indexOf(TranscoRequest.getEqualRequest(UUID.randomUUID())));
	}
	
	@Test
	public void updateTest() throws MCASException {
		TranscoRequest request = TranscoRequest.getEqualRequest(UUID.randomUUID());
		queue.put(request);
		assertEquals(Status.CREATED, queue.getRequest(request).getState());
		request.increaseState();
		queue.update(request);
		assertTrue(! Status.CREATED.equals(queue.getRequest(request).getState()));
	}
	
	@Test
	public void updateNoChangeTest() throws MCASException {
		TranscoRequest request = TranscoRequest.getEqualRequest(UUID.randomUUID());
		queue.put(request);
		assertEquals(Status.CREATED, queue.getRequest(request).getState());
		request.increaseState();
		assertTrue(Status.M_QUEUED.equals(queue.getRequest(request).getState()));
	}
	
	@Test
	public void removeTest() {
		assertTrue(queue.removeRequest(request1st));
		assertEquals(4,queue.size());
	}
	
	@Test
	public void removeNotFoundTest() {
		assertTrue(! queue.removeRequest(TranscoRequest.getEqualRequest(UUID.randomUUID())));
		assertEquals(5,queue.size());
	}
	
	@Test
	public void getTest()  {
		TranscoRequest request = queue.get(Status.T_PROCESS);
		assertEquals(request1st, request);
		assertEquals(5, queue.size());
	}
	
	@Test
	public void getNoneTest()  {
		assertNull(queue.get(Status.M_QUEUED));
	}
	
	@Test
	public void countNoneTest() throws MCASException {
		assertEquals(0,queue.count(Status.M_QUEUED));
	}
	
	@Test
	public void countTest() throws MCASException {
		assertEquals(2,queue.count(Status.CREATED));
	}
	
	@Test
	public void getStateTest() {
		assertEquals(Status.MOVING,queue.getState(request3rd));
	}
	
	@Test
	public void getStateNotFoundTest() {
		assertNull(queue.getState(TranscoRequest.getEqualRequest(UUID.randomUUID())));
	}
	
	@Test
	public void getRequestNotFoundTest() {
		assertNull(queue.getRequest(TranscoRequest.getEqualRequest(UUID.randomUUID())));
	}
	
	@Test
	public void getRequestTest() {
		assertEquals(request1st, queue.getRequest(request1st));
	}
	
	@Test
	public void clearTest(){
		assertEquals(5, queue.size());
		queue.clearQueue();
		assertEquals(0,queue.size());
	}
	
	@Test
	public void containsTest(){
		assertTrue(queue.contains(request1st));
	}
	
	@Test
	public void containsNotFoundTest(){
		assertTrue(! queue.contains(TranscoRequest.getEqualRequest(UUID.randomUUID())));
	}	
	
	@Test
	public void isEmptyTest(){
		assertTrue(! queue.isEmpty());
		queue.clearQueue();
		assertTrue(queue.isEmpty());
	}
	
	@Test
	public void sizeTest(){
		assertEquals(5,queue.size());
	}
	
	@Test
	public void isEmptyStateTest(){
		assertTrue(queue.isEmpty(Status.T_QUEUED));
		assertTrue(! queue.isEmpty(Status.MOVING));
	}
	
	@Test
	public void getListTest(){
		List<TranscoRequest> list = queue.getElements();
		assertEquals(5,list.size());
		assertTrue(list.get(0) instanceof TranscoRequest);
	}
}
