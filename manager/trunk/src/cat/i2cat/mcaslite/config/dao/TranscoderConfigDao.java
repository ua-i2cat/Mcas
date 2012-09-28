package cat.i2cat.mcaslite.config.dao;

import javax.persistence.Query;

import cat.i2cat.mcaslite.config.model.TranscoderConfig;
import cat.i2cat.mcaslite.exceptions.MCASException;

public class TranscoderConfigDao extends DAO {

	public static TranscoderConfig findById(Integer id) throws MCASException{
		try {
			return getEntityManager().find(TranscoderConfig.class, id);
		} catch (Exception e){
			e.printStackTrace();
			throw new MCASException();
		}
	}
	
	public static TranscoderConfig findByName(String name) throws MCASException{
		try {
			Query query = getEntityManager().createQuery("select t from TranscoderConfig t where t.name = :name");
			query.setParameter("name", name);
			return (TranscoderConfig) query.getSingleResult();
		} catch (Exception e){
			e.printStackTrace();
			throw new MCASException();
		}
	}

	public static void save(TranscoderConfig config) throws MCASException {
		try {
			begin();
			getEntityManager().persist(config);
			commit();
		} catch (Exception e) {
			rollback();
			e.printStackTrace();
			throw new MCASException();
		}
	}

	public static void delete(TranscoderConfig config) throws MCASException {
		try {
			begin();
			getEntityManager().remove(config);
			commit();
		} catch (Exception e) {
			rollback();
			e.printStackTrace();
			throw new MCASException();
		}
	}
	
	public static void deleteById(Integer id) throws MCASException {
		try {
			begin();
			TranscoderConfig config = findById(id);
			getEntityManager().remove(config);
			commit();
		} catch (Exception e) {
			rollback();
			e.printStackTrace();
			throw new MCASException();
		}
	}
	
}
