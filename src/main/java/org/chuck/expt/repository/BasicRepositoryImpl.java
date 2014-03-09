package org.chuck.expt.repository;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.chuck.expt.model.BaseEntity;

class BasicRepositoryImpl {
	//Chuck!! You can see whether the JPA second level cache has your entity like so:
	//eM.getEntityManagerFactory().getCache().contains(Class cls, Object  primaryKey)
	//This is the cache that outlives a transaction, and it's mode is set as shared-cache-mode in the 
	//JPA persistence unit. Spring doesn't always require you to have a persistence.xml, and as
	//of this writing this project doesn't have one, so I'm not yet sure how to set it.
	//We must therefore be using the default shared-cache-mode.
	//This site has some info about the available shared-cache-modes, and also showed me how
	//to programmatically access the cache: http://docs.oracle.com/javaee/6/tutorial/doc/gkjjj.html
	
	@PersistenceContext
	protected EntityManager eM;
	
	public void save(BaseEntity entity) {
		
		if(entity.isNew())
			eM.persist(entity);
		else {
			eM.merge(entity);
		}
	}
	
	//Chuck!! Neat trick! See http://stackoverflow.com/questions/16043761/java-hibernate-entity-allow-to-set-related-object-both-by-id-and-object-itself
	//For Hibernate the trick is session.load(SomeEntity.class, id);
	public <T extends BaseEntity> T stubReferenceForId(Class<T> clazz, Long id) {
		return eM.getReference(clazz, id);
	}
	
	public <T extends BaseEntity> T findById(Class<T> clazz, Long id) {
		return eM.find(clazz, id);
	}
	
	/**
	 * 
	 * Please don't call this in a loop!
	 * If you already have the entity that you want to remove, and if you are in a transaction,
	 * then you wouldn't use this. (Instead use remove(BaseEntity.)  You use this when you're outside a transaction, because
	 * JPA cannot delete a detached entity.
	 */
	public <T extends BaseEntity> void remove(Class<T> clazz, Long id) {
	  remove(findById(clazz, id));
	}
	
	public void remove(BaseEntity entity) {
		eM.remove(entity);
	}
	
	
	@SuppressWarnings("unchecked")
	public <T extends BaseEntity> List<T> findAll(Class<T> clazz) {
		Query query = eM.createQuery("select x from " + clazz.getSimpleName() + " x " );
		return query.getResultList();
	}

}
