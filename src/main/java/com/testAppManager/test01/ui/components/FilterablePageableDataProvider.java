package com.testAppManager.test01.ui.components;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.vaadin.data.provider.AbstractBackEndDataProvider;
import com.vaadin.data.provider.Query;
import com.vaadin.data.provider.QuerySortOrder;

public abstract class FilterablePageableDataProvider<T, F> extends AbstractBackEndDataProvider<T, F> {
	private String filter = "";

	public void setFilter(String filter) {
		if (filter == null) {
			throw new IllegalArgumentException("Filter cannot be null");
		}
		this.filter = filter;
		refreshAll();
	}

	protected Optional<String> getOptionalFilter() {
		if ("".equals(filter)) {
			return Optional.empty();
		} else {
			return Optional.of(filter);
		}
	}

	@Override
	protected Stream<T> fetchFromBackEnd(Query<T, F> query) {
		List<QuerySortOrder> sortOrders = getSortOrders(query);
		return fetchFromBackEnd(query, sortOrders);
	}

	private List<QuerySortOrder> getSortOrders(Query<T, F> q) {
		List<QuerySortOrder> sortOrders;
		if (q.getSortOrders().isEmpty()) {
			sortOrders = getDefaultSortOrders();
		} else {
			sortOrders = q.getSortOrders();
		}
		return sortOrders;
	}

	protected abstract Stream<T> fetchFromBackEnd(Query<T, F> query, List<QuerySortOrder> sortOrders);

	protected abstract List<QuerySortOrder> getDefaultSortOrders();

}
