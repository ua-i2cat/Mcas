package cat.i2cat.mcaslite.config.dao;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

public class DAO {
	
	private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("tConfig");
    private static final EntityManager em = emf.createEntityManager();
    
    
    private static EntityTransaction getTransaction(){
    	return em.getTransaction();
    }
    
    protected static void begin(){
    	getTransaction().begin();
    }
    
    protected static void commit(){
    	getTransaction().commit();
    }
    
    protected static void rollback(){
    	getTransaction().rollback();
    }
    
    protected static EntityManager getEntityManager(){
    	return em;
    }

}
