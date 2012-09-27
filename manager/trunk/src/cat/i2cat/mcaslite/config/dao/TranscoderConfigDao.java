package cat.i2cat.mcaslite.config.dao;

import org.hibernate.HibernateException;

import cat.i2cat.mcaslite.config.model.TranscoderConfig;
import cat.i2cat.mcaslite.exceptions.MCASException;

public class TranscoderConfigDao extends DAO {

	public TranscoderConfig create(TranscoderConfig config) throws MCASException {
		try{
			begin();
			getSession().save(config);
			commit();
		} catch (HibernateException e) {
			rollback();
			throw new MCASException();
		}
		return null;
	}

	public TranscoderConfig get(Integer id) throws MCASException {
		try {
			return (TranscoderConfig) getSession().get(TranscoderConfig.class, id);
		} catch (HibernateException e) {
			e.printStackTrace();
			throw new MCASException();
		}
	}

	public void save(TranscoderConfig config) throws MCASException {
		try {
			begin();
			getSession().update(config);
			commit();
		} catch (HibernateException e) {
			rollback();
			e.printStackTrace();
			throw new MCASException();
		}
	}

	public void delete(TranscoderConfig config) throws MCASException {
		try {
			begin();
			getSession().delete(config);
			commit();
		} catch (HibernateException e) {
			rollback();
			e.printStackTrace();
			throw new MCASException();
		}
	}
	
	public void delete(Integer id) throws MCASException {
		try {
			begin();
			TranscoderConfig config = get(id);
			getSession().delete(config);
			commit();
		} catch (HibernateException e) {
			rollback();
			e.printStackTrace();
			throw new MCASException();
		} catch (MCASException e) {
			rollback();
			e.printStackTrace();
			throw new MCASException();
		}
	}

	
}
