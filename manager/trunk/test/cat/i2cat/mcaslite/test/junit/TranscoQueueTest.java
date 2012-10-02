package cat.i2cat.mcaslite.test.junit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import cat.i2cat.mcaslite.config.model.TranscoRequest;
import cat.i2cat.mcaslite.config.model.TranscoRequest.State;
import cat.i2cat.mcaslite.entities.TranscoQueue;
import cat.i2cat.mcaslite.exceptions.MCASException;

public class TranscoQueueTest {
	
	private final static int NREQ = 100;
	private final TranscoQueue queue = TranscoQueue.getInstance();
	private pThread producer = new pThread(queue, NREQ);
	private Thread p = new Thread(producer);
	
	@Test
	public void putTest() throws MCASException{
		TranscoRequest req1 = new TranscoRequest();
		TranscoRequest req2 = new TranscoRequest();
		boolean fail = false;
		assertTrue(queue.isEmpty());
		queue.put(req1);
		assertTrue(! queue.isEmpty());
		try {
			queue.put(req1);
		} catch (MCASException e){
			fail = true;
		}
		assertTrue(fail);
		assertTrue(queue.contains(req1));
		queue.put(req2);
		assertTrue(queue.contains(req1) && queue.contains(req2));
	}
	
	@Test
	public void indexOfTest() throws MCASException {
		queue.clearQueue();
		assertTrue(queue.isEmpty());
		TranscoRequest req1 = new TranscoRequest();
		queue.put(req1);
		assertTrue(! queue.isEmpty());
		TranscoRequest req2 = queue.get(State.CREATED);
		assertNotNull(req1);
		assertEquals(req2,req1);
		assertEquals(0,queue.indexOf(req1));
	}
	
	@Test
	public void updateTest() throws MCASException {
		queue.clearQueue();
		assertTrue(queue.isEmpty());
		TranscoRequest req1 = new TranscoRequest();
		queue.put(req1);
		assertTrue(! queue.isEmpty());
		TranscoRequest req2 = queue.get(State.CREATED);
		assertNotNull(req1);
		assertEquals(req2,req1);
		req2.increaseState();
		assertEquals(req2,req1);
		queue.update(req2);
		req1 = queue.get(State.CREATED);
		assertNull(req1);
		assertEquals(0,queue.indexOf(req2));
		req1 = queue.get(State.M_QUEUED);
		assertNotNull(req1);
		assertEquals(req2,req1);
	}
	
	@Test
	public void handlerTest() throws InterruptedException {
		queue.clearQueue();
		assertTrue(queue.isEmpty());
		TranscoRequest request = null;
		p.setDaemon(true);
		p.start();
		int k = 0;
		int j = 0;
		int q = 0;
		Thread.sleep(5000);
		while(j < NREQ && k < NREQ && q < NREQ){
			try {
				synchronized(queue){
					if (queue.isEmpty(State.M_QUEUED) && queue.isEmpty(State.T_QUEUED) && queue.isEmpty(State.T_TRANSCODED)){
						queue.wait();
					}
				}
				request = queue.get(State.M_QUEUED);
				if (request != null){
					assertEquals(((Integer) j).toString(), request.getSrc());
					j++;
					request.increaseState();
					queue.update(request);
					(new Thread(new mThread(queue, request))).start();
				}
				request = queue.get(State.T_QUEUED);
				if (request != null){
					assertEquals(((Integer) k).toString(), request.getSrc());
					k++;
					request.increaseState();
					queue.update(request);
					(new Thread(new mThread(queue, request))).start();
				}
				request = queue.get(State.T_TRANSCODED);
				if (request != null){
					assertEquals(((Integer) q).toString(), request.getSrc());
					q++;
					request.increaseState();
					queue.update(request);
					(new Thread(new mThread(queue, request))).start();
				}
			} catch (Exception e) {
				e.printStackTrace();
				Assert.fail();
			}
		}
	}
	
}


class mThread implements Runnable{
	
	private final TranscoQueue queue;
	private TranscoRequest request;

	public mThread(TranscoQueue queue, TranscoRequest request){
		this.queue = queue;
		this.request = request;
	}
	@Override
	public void run() {
		try {
			Random r = new Random();
			request.increaseState();
			Thread.sleep((r.nextInt(100) + 1)*10);
			synchronized(queue){
				queue.update(request);
				queue.notifyAll();
			}
		} catch (Exception e){
			e.printStackTrace();
			Assert.fail();
		}
	}
	
}

class pThread implements Runnable{
	
	private final TranscoQueue queue;
	private int nReq;

	public pThread(TranscoQueue queue, int nReq){
		this.queue = queue;
		this.nReq = nReq;
	}
	@Override
	public void run() {
		Random r = new Random();
		for(int i = 0; i < nReq; i ++){
			try {
				TranscoRequest req = new TranscoRequest();
				req.setSrc(((Integer) i).toString());
				Thread.sleep((r.nextInt(100) + 1)*10);
				req.increaseState();
				synchronized(queue){
					queue.put(req);
					queue.notifyAll();
				}
			} catch (Exception e){
				Assert.fail();
			}
		}
	}
	
}
