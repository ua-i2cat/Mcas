package net.i2cat.mcas.config.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

import net.i2cat.mcas.exceptions.MCASException;

public class DAO<T> {
	
	protected static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("tConfig");
    protected static final EntityManager em = emf.createEntityManager();
    private Class<T> type;
    
    public DAO(Class<T> arg){
    	type = arg;
    }
    
    protected EntityManager getEntityManager(){
    	return em;
    }
    
    public List<T> listAll() throws MCASException{
    	try {
			TypedQuery<T> query = getEntityManager().createQuery("select t from " + type.getName() + " t", type);
			return query.getResultList();
		} catch (Exception e){
			e.printStackTrace();
			throw new MCASException();
		}
    }
    
	public T findByName(String name) throws MCASException{
		try {
			TypedQuery<T> query = getEntityManager().createQuery("select t from " + type.getName() + " t where t.name = :name", type);
			query.setParameter("name", name);
			return query.getSingleResult();
		} catch (Exception e){
			e.printStackTrace();
			throw new MCASException();
		}
	}
    
    public T findById(Object id) throws MCASException{
		try {
			return em.find(type, id);
		} catch (Exception e){
			e.printStackTrace();
			throw new MCASException();
		}
	}
    
    public void save(Object o){
    	em.getTransaction().begin();
    	try {
    		em.persist(o);
    		em.getTransaction().commit();
    	} catch (Exception e){
    		e.printStackTrace();
    		em.getTransaction().rollback();
    	}
    }
    
    public void delete(Object o){
    	em.getTransaction().begin();
    	em.remove(o);
    	em.getTransaction().commit();
    }
    
    public void deleteById(Object id) throws MCASException{
    	em.getTransaction().begin();
    	em.remove(findById(id));
    	em.getTransaction().commit();
    }
}
