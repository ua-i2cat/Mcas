package cat.i2cat.mcaslite.config.dao;

import java.util.UUID;

import cat.i2cat.mcaslite.config.model.TranscoRequest;
import cat.i2cat.mcaslite.exceptions.MCASException;

public class TranscoRequestDao extends DAO {

	public static TranscoRequest findById(UUID id) throws MCASException{
		try {
			return getEntityManager().find(TranscoRequest.class, id);
		} catch (Exception e){
			e.printStackTrace();
			throw new MCASException();
		}
	}
	
	public static void save(TranscoRequest request) throws MCASException {
		try {
			begin();
			getEntityManager().persist(request);
			commit();
		} catch (Exception e) {
			rollback();
			e.printStackTrace();
			throw new MCASException();
		}
	}

	public static void delete(TranscoRequest request) throws MCASException {
		try {
			begin();
			getEntityManager().remove(request);
			commit();
		} catch (Exception e) {
			rollback();
			e.printStackTrace();
			throw new MCASException();
		}
	}
	
	public static void deleteById(UUID id) throws MCASException {
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
