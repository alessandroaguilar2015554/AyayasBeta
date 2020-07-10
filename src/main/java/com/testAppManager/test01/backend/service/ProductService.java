package com.testAppManager.test01.backend.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.deltaspike.data.api.QueryResult;

import com.vaadin.data.provider.QuerySortOrder;
import com.testAppManager.test01.backend.ProductRepository;
import com.testAppManager.test01.backend.data.entity.Product;

@Stateless
public class ProductService extends CrudService<Product> {

	private final ProductRepository productRepository;

	public ProductService() {
		// An empty constructor is required by the EJB spec even though the
		// @Inject constructor is used
		productRepository = null;
	}

	@Inject
	public ProductService(ProductRepository productRepository) {
		this.productRepository = productRepository;
	}

	@Override
	public Stream<Product> findAnyMatching(Optional<String> filter, int offset, int limit,
			List<QuerySortOrder> sortOrders) {
		QueryResult<Product> result;
		if (filter.isPresent()) {
			String repositoryFilter = "%" + filter.get() + "%";
			result = getRepository().findByNameLikeIgnoreCase(repositoryFilter);
		} else {
			result = getRepository().queryAll();
		}
		result = QueryHelper.applyLimitsAndSortOrder(result, offset, limit, sortOrders);
		return result.getResultList().stream();
	}

	@Override
	public long countAnyMatching(Optional<String> filter) {
		if (filter.isPresent()) {
			String repositoryFilter = "%" + filter.get() + "%";
			return getRepository().countByNameLikeIgnoreCase(repositoryFilter);
		} else {
			return getRepository().count();
		}
	}

	@Override
	protected ProductRepository getRepository() {
		return productRepository;
	}

}
