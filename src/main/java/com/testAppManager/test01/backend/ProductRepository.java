package com.testAppManager.test01.backend;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.QueryResult;
import org.apache.deltaspike.data.api.Repository;

import com.testAppManager.test01.backend.data.entity.Product;

@Repository
public interface ProductRepository extends EntityRepository<Product, Long> {

	QueryResult<Product> findByNameLikeIgnoreCase(String name);

	@Query("select count(e) from Product e WHERE lower(e.name) like lower(?1)")
	int countByNameLikeIgnoreCase(String name);

	@Query("select e from Product e")
	QueryResult<Product> queryAll();

}
