/**************************************
Test disabled as it has become obsolete
***************************************/

/*
package net.i2cat.mcas.junit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import net.i2cat.mcas.config.model.TranscoRequestV;
import net.i2cat.mcas.config.model.TranscoRequestV.Status;
import net.i2cat.mcas.exceptions.MCASException;
import net.i2cat.mcas.management.TranscoQueue;

public class TranscoQueueTest {

	private final static int NREQ = 100;
	private final static int maxOutMedia = 4;
	private final static int maxInMedia = 4;
	private final static int maxTransco = 2;
	private final TranscoQueue queue = TranscoQueue.getInstance();
	private pThread producer = new pThread(queue, NREQ);
	private Thread p = new Thread(producer);
	
	private boolean MQBlock = true;
	private boolean TPBlock = true;
	private boolean TTBlock = true;

	@Test
	public void putTest() throws MCASException {
		TranscoRequestV req1 = new TranscoRequestV();
		TranscoRequestV req2 = new TranscoRequestV();
		boolean fail = false;
		assertTrue(queue.isEmpty());
		queue.put(req1);
		assertTrue(!queue.isEmpty());
		try {
			queue.put(req1);
		} catch (MCASException e) {
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
		TranscoRequestV req1 = new TranscoRequestV();
		queue.put(req1);
		assertTrue(!queue.isEmpty());
		TranscoRequestV req2 = queue.get(Status.CREATED);
		assertNotNull(req1);
		assertEquals(req2, req1);
		assertEquals(0, queue.indexOf(req1));
	}

	@Test
	public void updateTest() throws MCASException {
		queue.clearQueue();
		assertTrue(queue.isEmpty());
		TranscoRequestV req1 = new TranscoRequestV();
		queue.put(req1);
		assertTrue(!queue.isEmpty());
		TranscoRequestV req2 = queue.get(Status.CREATED);
		assertNotNull(req1);
		assertEquals(req2, req1);
		req2.increaseState();
		assertEquals(req2, req1);
		queue.update(req2);
		req1 = queue.get(Status.CREATED);
		assertNull(req1);
		assertEquals(0, queue.indexOf(req2));
		req1 = queue.get(Status.M_QUEUED);
		assertNotNull(req1);
		assertEquals(req2, req1);
	}

	@Test
	public void handlerTest() throws InterruptedException {
		queue.clearQueue();
		assertTrue(queue.isEmpty());
		TranscoRequestV request = null;
		p.setDaemon(true);
		p.start();
		int k = 0;
		int j = 0;
		int q = 0;
		int ntq;
		int ntt;
		Thread.sleep(5000);
		String str = "";
		while (j < NREQ && k < NREQ && q < NREQ) {
			try {
				synchronized (queue) {
					waitCondition();
					str = countStates();
					ntq = queue.count(Status.T_QUEUED);
					ntt = queue.count(Status.T_TRANSCODED);
				}
				if (! MQBlock) {
					request = queue.get(Status.M_QUEUED);
					if (ntq >= maxInMedia){
						System.out.println(MQBlock + " " + TPBlock + " " + TTBlock);
						System.out.println("M_QUEUED FAIL: \n" + str);
						Assert.fail();
					}
					assertEquals(((Integer) j).toString(), request.getSrc());
					j++;
					request.increaseState();
					queue.update(request);
					(new Thread(new mThread(queue, request))).start();
				} 
				if (! TPBlock) {
					request = queue.get(Status.T_QUEUED);
					if (ntt >= maxOutMedia){
						System.out.println(MQBlock + " " + TPBlock + " " + TTBlock);
						System.out.println("T_TRANSCODED FAIL: \n" + str);
						Assert.fail();
					}
					assertEquals(((Integer) k).toString(), request.getSrc());
					k++;
					request.increaseState();
					queue.update(request);
					(new Thread(new mThread(queue, request))).start();
				} 
				if (! TTBlock) {
					request = queue.get(Status.T_TRANSCODED);
					assertEquals(((Integer) q).toString(),
					request.getSrc());
					q++;
					request.increaseState();
					queue.update(request);
					(new Thread(new mThread(queue, request))).start();
				} 
				if (MQBlock && TPBlock && TTBlock){
					System.out.println(MQBlock + " " + TPBlock + " " + TTBlock);
					System.out.println(str);
					Assert.fail();
				}
			} catch (Exception e) {
				e.printStackTrace();
				Assert.fail();
			}
		}
	}

	private void waitCondition() throws MCASException, InterruptedException {
		MQBlock = conditionMQ();
		TPBlock = conditionTQ();
		TTBlock = conditionTT();
		if (MQBlock && TPBlock && TTBlock) {
			queue.wait();
			waitCondition();
		}
	}

	private boolean conditionMQ() throws MCASException {
		return (queue.isEmpty(Status.M_QUEUED) || queue.count(Status.M_PROCESS) >= maxInMedia || queue.count(Status.T_QUEUED) >= maxInMedia);
	}
	
	private boolean conditionTQ() throws MCASException {
		return (queue.isEmpty(Status.T_QUEUED) || queue.count(Status.T_PROCESS) >= maxTransco || queue.count(Status.T_TRANSCODED) >= maxOutMedia);
	}
	
	private boolean conditionTT() throws MCASException {
		return (queue.isEmpty(Status.T_TRANSCODED) || queue.count(Status.MOVING) >= maxOutMedia);
	}

	private String countStates() throws MCASException {
		synchronized (queue) {
			String str = "";
			str += Status.CREATED.getName() + ": " + queue.count(Status.CREATED)
					+ "\n";
			str += Status.M_QUEUED.getName() + ": "
					+ queue.count(Status.M_QUEUED) + "\n";
			str += Status.M_PROCESS.getName() + ": "
					+ queue.count(Status.M_PROCESS) + "\n";
			str += Status.T_QUEUED.getName() + ": "
					+ queue.count(Status.T_QUEUED) + "\n";
			str += Status.T_PROCESS.getName() + ": "
					+ queue.count(Status.T_PROCESS) + "\n";
			str += Status.T_TRANSCODED.getName() + ": "
					+ queue.count(Status.T_TRANSCODED) + "\n";
			str += Status.MOVING.getName() + ": " + queue.count(Status.MOVING)
					+ "\n";
			str += Status.DONE.getName() + ": " + queue.count(Status.DONE) + "\n";
			str += Status.ERROR.getName() + ": " + queue.count(Status.ERROR)
					+ "\n";
			str += Status.PARTIAL_ERROR.getName() + ": "
					+ queue.count(Status.PARTIAL_ERROR) + "\n";
			return str;
		}
	}

}

class mThread implements Runnable {

	private final TranscoQueue queue;
	private TranscoRequestV request;

	public mThread(TranscoQueue queue, TranscoRequestV request) {
		this.queue = queue;
		this.request = request;
	}

	@Override
	public void run() {
		try {
			Random r = new Random();
			request.increaseState();
			Thread.sleep((r.nextInt(100) + 1) * 100);
			synchronized (queue) {
				queue.update(request);
				queue.notifyAll();
			}
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

}

class pThread implements Runnable {

	private final TranscoQueue queue;
	private int nReq;

	public pThread(TranscoQueue queue, int nReq) {
		this.queue = queue;
		this.nReq = nReq;
	}

	@Override
	public void run() {
		Random r = new Random();
		for (int i = 0; i < nReq; i++) {
			try {
				TranscoRequestV req = new TranscoRequestV();
				req.setSrc(((Integer) i).toString());
				Thread.sleep((r.nextInt(100) + 1) * 5);
				req.increaseState();
				synchronized (queue) {
					queue.put(req);
					queue.notifyAll();
				}
			} catch (Exception e) {
				Assert.fail();
			}
		}
	}

}
*/