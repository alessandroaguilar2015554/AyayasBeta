package com.testAppManager.test01.ui.views.admin.product;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.vaadin.data.provider.Query;
import com.vaadin.data.provider.QuerySortOrder;
import com.vaadin.shared.data.sort.SortDirection;
import com.testAppManager.test01.backend.data.entity.Product;
import com.testAppManager.test01.backend.service.ProductService;
import com.testAppManager.test01.ui.components.FilterablePageableDataProvider;

@Dependent
public class ProductAdminDataProvider extends FilterablePageableDataProvider<Product, Object> {

	private final ProductService productService;

	@Inject
	public ProductAdminDataProvider(ProductService productService) {
		this.productService = productService;
	}

	@Override
	protected Stream<Product> fetchFromBackEnd(Query<Product, Object> query, List<QuerySortOrder> sortOrders) {
		return productService.findAnyMatching(getOptionalFilter(), query.getOffset(), query.getLimit(), sortOrders);
	}

	@Override
	protected int sizeInBackEnd(Query<Product, Object> query) {
		return (int) productService.countAnyMatching(getOptionalFilter());
	}

	@Override
	protected List<QuerySortOrder> getDefaultSortOrders() {
		List<QuerySortOrder> sortOrders = new ArrayList<>();
		sortOrders.add(new QuerySortOrder("name", SortDirection.ASCENDING));
		return sortOrders;
	}

}