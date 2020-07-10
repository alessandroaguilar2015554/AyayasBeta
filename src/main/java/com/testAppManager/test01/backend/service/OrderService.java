package com.testAppManager.test01.backend.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.deltaspike.data.api.QueryResult;

import com.vaadin.data.provider.QuerySortOrder;
import com.testAppManager.test01.backend.OrderRepository;
import com.testAppManager.test01.backend.data.DashboardData;
import com.testAppManager.test01.backend.data.DeliveryStats;
import com.testAppManager.test01.backend.data.OrderState;
import com.testAppManager.test01.backend.data.entity.HistoryItem;
import com.testAppManager.test01.backend.data.entity.Order;
import com.testAppManager.test01.backend.data.entity.Product;
import com.testAppManager.test01.backend.data.entity.User;

@Stateless
public class OrderService {

	private final OrderRepository orderRepository;

	private static Set<OrderState> notAvailableStates;

	static {
		notAvailableStates = new HashSet<>(Arrays.asList(OrderState.values()));
		notAvailableStates.remove(OrderState.DELIVERED);
		notAvailableStates.remove(OrderState.READY);
		notAvailableStates.remove(OrderState.CANCELLED);
	}

	public OrderService() {
		// An empty constructor is required by the EJB spec even though the
		// @Inject constructor is used
		orderRepository = null;
	}

	@Inject
	public OrderService(OrderRepository orderRepository) {
		this.orderRepository = orderRepository;
	}

	public Order findOrder(Long id) {
		return orderRepository.findById(id).orElse(null);
	}

	public Order changeState(Order order, OrderState state, User user) {
		if (order.getState() == state) {
			throw new IllegalArgumentException("Order state is already " + state);
		}
		order.setState(state);
		addHistoryItem(order, state, user);

		return orderRepository.save(order);
	}

	private void addHistoryItem(Order order, OrderState newState, User user) {
		String comment = "Order " + newState.getDisplayName();

		HistoryItem item = new HistoryItem(user, comment);
		item.setNewState(newState);
		if (order.getHistory() == null) {
			order.setHistory(new ArrayList<>());
		}
		order.getHistory().add(item);
	}

	public Order saveOrder(Order order, User user) {
		if (order.getHistory() == null) {
			String comment = "Order placed";
			order.setHistory(new ArrayList<>());
			HistoryItem item = new HistoryItem(user, comment);
			item.setNewState(OrderState.NEW);
			order.getHistory().add(item);
		}

		return orderRepository.save(order);
	}

	public Order addHistoryItem(Order order, String comment, User user) {
		HistoryItem item = new HistoryItem(user, comment);

		if (order.getHistory() == null) {
			order.setHistory(new ArrayList<>());
		}

		order.getHistory().add(item);

		return orderRepository.save(order);
	}

	public Stream<Order> findAnyMatchingAfterDueDate(Optional<String> optionalFilter,
			Optional<LocalDate> optionalFilterDate, int offset, int limit, List<QuerySortOrder> sortOrders) {
		QueryResult<Order> result;

		if (optionalFilter.isPresent()) {
			String queryFilter = "%" + optionalFilter.get() + "%";
			if (optionalFilterDate.isPresent()) {
				result = orderRepository.findByCustomerFullNameLikeAndDueDateGreaterThan(queryFilter,
						optionalFilterDate.get());
			} else {
				result = orderRepository.findByCustomerFullNameLike(queryFilter);
			}
		} else {
			if (optionalFilterDate.isPresent()) {
				result = orderRepository.findByDueDateGreaterThan(optionalFilterDate.get());
			} else {
				result = orderRepository.queryAll();
			}
		}
		result = QueryHelper.applyLimitsAndSortOrder(result, offset, limit, sortOrders);
		return result.getResultList().stream();
	}

	public long countAfterDueDateWithState(LocalDate filterDate, List<OrderState> states) {
		return orderRepository.countByDueDateAfterAndStateIn(filterDate, states);
	}

	public long countAnyMatchingAfterDueDate(Optional<String> optionalFilter, Optional<LocalDate> optionalFilterDate) {
		if (optionalFilter.isPresent()) {
			String queryFilter = "%" + optionalFilter.get() + "%";
			if (optionalFilterDate.isPresent()) {
				return orderRepository.countByCustomerFullNameContainingIgnoreCaseAndDueDateAfter(queryFilter,
						optionalFilterDate.get());
			} else {
				return orderRepository.countByCustomerFullNameContainingIgnoreCase(queryFilter);
			}
		} else {
			if (optionalFilterDate.isPresent()) {
				return orderRepository.countByDueDateAfter(optionalFilterDate.get());
			} else {
				return orderRepository.count();
			}
		}
	}

	private DeliveryStats getDeliveryStats() {
		DeliveryStats stats = new DeliveryStats();
		LocalDate today = LocalDate.now();
		stats.setDueToday((int) orderRepository.countByDueDate(today));
		stats.setDueTomorrow((int) orderRepository.countByDueDate(today.plusDays(1)));
		stats.setDeliveredToday(
				(int) orderRepository.countByDueDateAndStateIn(today, Collections.singleton(OrderState.DELIVERED)));

		stats.setNotAvailableToday((int) orderRepository.countByDueDateAndStateIn(today, notAvailableStates));
		stats.setNewOrders((int) orderRepository.countByState(OrderState.NEW));

		return stats;
	}

	public DashboardData getDashboardData(int month, int year) {
		DashboardData data = new DashboardData();
		data.setDeliveryStats(getDeliveryStats());
		data.setDeliveriesThisMonth(getDeliveriesPerDay(month, year));
		data.setDeliveriesThisYear(getDeliveriesPerMonth(year));

		Number[][] salesPerMonth = new Number[3][12];
		data.setSalesPerMonth(salesPerMonth);
		List<Object[]> sales = orderRepository.sumPerMonthLastThreeYears(OrderState.DELIVERED, year);

		for (Object[] salesData : sales) {
			// year, month, deliveries
			int y = year - ((Number) salesData[0]).intValue();
			int m = ((Number) salesData[1]).intValue() - 1;
			if (y == 0 && m == month - 1) {
				// skip current month as it contains incomplete data
				continue;
			}
			long count = ((Number) salesData[2]).longValue();
			salesPerMonth[y][m] = count;
		}

		LinkedHashMap<Product, Integer> productDeliveries = new LinkedHashMap<>();
		data.setProductDeliveries(productDeliveries);
		for (Object[] result : orderRepository.countPerProduct(OrderState.DELIVERED, year, month)) {
			int sum = ((Long) result[0]).intValue();
			Product p = (Product) result[1];
			productDeliveries.put(p, sum);
		}

		return data;
	}

	private List<Number> getDeliveriesPerDay(int month, int year) {
		int daysInMonth = YearMonth.of(year, month).lengthOfMonth();
		return flattenAndReplaceMissingWithNull(daysInMonth,
				orderRepository.countPerDay(OrderState.DELIVERED, year, month));
	}

	private List<Number> getDeliveriesPerMonth(int year) {
		return flattenAndReplaceMissingWithNull(12, orderRepository.countPerMonth(OrderState.DELIVERED, year));
	}

	private List<Number> flattenAndReplaceMissingWithNull(int length, List<Object[]> list) {
		List<Number> counts = new ArrayList<>();
		for (int i = 0; i < length; i++) {
			counts.add(null);
		}

		for (Object[] result : list) {
			Number count = (Number) result[0];
			counts.set(count.intValue() - 1, (Number) result[1]);
		}
		return counts;
	}

}
