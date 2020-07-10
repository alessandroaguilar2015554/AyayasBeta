package com.testAppManager.test01.ui.views.orderedit;

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
public class ProductComboBoxDataProvider extends FilterablePageableDataProvider<Product, String> {

	private final ProductService productService;

	@Inject
	public ProductComboBoxDataProvider(ProductService productService) {
		this.productService = productService;
	}

	@Override
	protected Stream<Product> fetchFromBackEnd(Query<Product, String> query, List<QuerySortOrder> sortOrders) {
		return productService.findAnyMatching(query.getFilter(), query.getOffset(), query.getLimit(), sortOrders);
	}

	@Override
	protected int sizeInBackEnd(Query<Product, String> query) {
		return (int) productService.countAnyMatching(query.getFilter());
	}

	@Override
	protected List<QuerySortOrder> getDefaultSortOrders() {
		List<QuerySortOrder> sortOrders = new ArrayList<>();
		sortOrders.add(new QuerySortOrder("name", SortDirection.ASCENDING));
		return sortOrders;
	}

}
