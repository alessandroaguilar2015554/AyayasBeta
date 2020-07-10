package com.testAppManager.test01.ui.components;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.vaadin.data.provider.Query;
import com.vaadin.data.provider.QuerySortOrder;
import com.vaadin.shared.data.sort.SortDirection;
import com.testAppManager.test01.backend.data.entity.Order;
import com.testAppManager.test01.backend.service.OrderService;

@Dependent
public class OrdersDataProvider extends FilterablePageableDataProvider<Order, Object> {

	private final OrderService orderService;
	private LocalDate filterDate = LocalDate.now().minusDays(1);

	@Inject
	public OrdersDataProvider(OrderService orderService) {
		this.orderService = orderService;
	}

	@Override
	protected Stream<Order> fetchFromBackEnd(Query<Order, Object> query, List<QuerySortOrder> sortOrders) {
		return orderService.findAnyMatchingAfterDueDate(getOptionalFilter(), getOptionalFilterDate(), query.getOffset(),
				query.getLimit(), sortOrders);
	}

	private Optional<LocalDate> getOptionalFilterDate() {
		if (filterDate == null) {
			return Optional.empty();
		} else {
			return Optional.of(filterDate);
		}
	}

	public void setIncludePast(boolean includePast) {
		if (includePast) {
			filterDate = null;
		} else {
			filterDate = LocalDate.now().minusDays(1);
		}
	}

	@Override
	protected int sizeInBackEnd(Query<Order, Object> query) {
		return (int) orderService.countAnyMatchingAfterDueDate(getOptionalFilter(), getOptionalFilterDate());
	}

	@Override
	protected List<QuerySortOrder> getDefaultSortOrders() {
		List<QuerySortOrder> sortOrders = new ArrayList<>();
		sortOrders.add(new QuerySortOrder("dueDate", SortDirection.ASCENDING));
		sortOrders.add(new QuerySortOrder("dueTime", SortDirection.ASCENDING));
		// id included only to always get a stable sort order
		sortOrders.add(new QuerySortOrder("id", SortDirection.DESCENDING));
		return sortOrders;
	}
}
