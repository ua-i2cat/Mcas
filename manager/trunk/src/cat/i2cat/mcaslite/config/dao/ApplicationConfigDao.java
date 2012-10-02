package cat.i2cat.mcaslite.config.dao;

import cat.i2cat.mcaslite.config.model.ApplicationConfig;
import cat.i2cat.mcaslite.exceptions.MCASException;

public class ApplicationConfigDao extends DAO {
	
	public static ApplicationConfig findById(Integer id) throws MCASException{
		try {
			return getEntityManager().find(ApplicationConfig.class, id);
		} catch (Exception e){
			e.printStackTrace();
			throw new MCASException();
		}
	}
	
	public static void save(ApplicationConfig config) throws MCASException {
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

	public static void delete(ApplicationConfig config) throws MCASException {
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
			getEntityManager().remove(findById(id));
			commit();
		} catch (Exception e) {
			rollback();
			e.printStackTrace();
			throw new MCASException();
		}
	}

}
