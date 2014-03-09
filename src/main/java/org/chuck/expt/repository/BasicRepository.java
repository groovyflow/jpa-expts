package org.chuck.expt.repository;

import java.util.List;

import org.chuck.expt.model.BaseEntity;

public interface BasicRepository {
	
	void save(BaseEntity entity);
	<T extends BaseEntity> T stubReferenceForId(Class<T> clazz, Long id);
	<T extends BaseEntity> T findById(Class<T> clazz, Long id);
	<T extends BaseEntity> List<T> findAll(Class<T> clazz);
	
	<T extends BaseEntity> void remove(Class<T> clazz, Long id);
	void remove(BaseEntity entity);



}
