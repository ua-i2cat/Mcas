package cat.i2cat.mcaslite.junit;

import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import cat.i2cat.mcaslite.config.model.TranscoRequest;
import cat.i2cat.mcaslite.exceptions.MCASException;
import cat.i2cat.mcaslite.management.TranscoHandler;
import static org.powermock.api.easymock.PowerMock.*;


@RunWith(PowerMockRunner.class)
@PrepareForTest(TranscoHandler.class)
public class TranscoHandlerTest {
	
	@BeforeClass
	public static void setup() throws Exception{
//		//mockStatic(DefaultsUtils.class);
//		//expect(DefaultsUtils.feedDefaultsNeeded()).andReturn(true);
//		@SuppressWarnings("unchecked")
//		DAO<ApplicationConfig> applicationDaoMock = createMock(DAO.class);
//		expectNew(DAO.class).andReturn(applicationDaoMock);
//		//expect(applicationDaoMock.findByName("DEFAULT")).andReturn(DefaultsUtils.applicationGetDefaults());
//		replay();
	}
	
	@AfterClass
	public static void tearDown(){
		verifyAll();
	}
	
	@Test
	public void testPutEmptyQueue() throws MCASException{
		assertTrue((new TranscoHandler()).putRequest(new TranscoRequest()));
	}
}
