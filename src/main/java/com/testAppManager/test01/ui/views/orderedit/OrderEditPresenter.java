package com.testAppManager.test01.ui.views.orderedit;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.validation.ValidationException;

import com.vaadin.cdi.ViewScoped;
import com.vaadin.data.HasValue;
import com.testAppManager.test01.app.HasLogger;
import com.testAppManager.test01.app.security.ShiroAccessControl;
import com.testAppManager.test01.backend.data.OrderState;
import com.testAppManager.test01.backend.data.entity.Customer;
import com.testAppManager.test01.backend.data.entity.Order;
import com.testAppManager.test01.backend.data.entity.OrderItem;
import com.testAppManager.test01.backend.service.OrderService;
import com.testAppManager.test01.backend.service.PickupLocationService;
import com.testAppManager.test01.backend.service.UserService;
import com.testAppManager.test01.ui.navigation.NavigationManager;
import com.testAppManager.test01.ui.views.orderedit.OrderEditView.Mode;
import com.testAppManager.test01.ui.views.storefront.StorefrontView;
import com.vaadin.ui.Component.Focusable;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;

@ViewScoped
public class OrderEditPresenter implements Serializable, HasLogger {

	private OrderEditView view;

	private final OrderService orderService;
	private final UserService userService;

	private final PickupLocationService pickupLocationService;

	private final NavigationManager navigationManager;

	private final ShiroAccessControl accessControl;

	private static final List<OrderState> happyPath = Arrays.asList(OrderState.NEW, OrderState.CONFIRMED,
			OrderState.READY, OrderState.DELIVERED);

	@Inject
	public OrderEditPresenter(NavigationManager navigationManager, OrderService orderService, UserService userService,
			PickupLocationService pickupLocationService, ShiroAccessControl accessControl) {
		this.navigationManager = navigationManager;
		this.orderService = orderService;
		this.userService = userService;
		this.pickupLocationService = pickupLocationService;
		this.accessControl = accessControl;
	}

	public void onProductInfoChange(@Observes ProductInfoChangeEvent event) {
		updateTotalSum();
		view.onProductInfoChanged();
	}

	public void onOrderItemDelete(@Observes OrderItemDeletedEvent event) {
		removeOrderItem(event.getOrderItem());
		view.onProductInfoChanged();
	}

	public void onOrderItemUpdate(@Observes OrderUpdatedEvent event) {
		refresh(view.getOrder().getId());
	}

	void init(OrderEditView view) {
		this.view = view;
	}

	/**
	 * Called when the user enters the view.
	 */
	public void enterView(Long id) {
		Order order;
		if (id == null) {
			// New
			order = new Order();
			order.setState(OrderState.NEW);
			order.setItems(new ArrayList<>());
			order.setCustomer(new Customer());
			order.setDueDate(LocalDate.now().plusDays(1));
			order.setDueTime(LocalTime.of(8, 00));
			order.setPickupLocation(pickupLocationService.getDefault());
		} else {
			order = orderService.findOrder(id);
			if (order == null) {
				view.showNotFound();
				return;
			}
		}

		refreshView(order);
	}

	private void updateTotalSum() {
		int sum = view.getOrder().getItemsStream().filter(item -> item.getProduct() != null)
				.collect(Collectors.summingInt(item -> item.getProduct().getPrice() * item.getQuantity()));
		view.setSum(sum);
	}

	public void editBackCancelPressed() {
		if (view.getMode() == Mode.REPORT) {
			// Edit order
			view.setMode(Mode.EDIT);
		} else if (view.getMode() == Mode.CONFIRMATION) {
			// Back to edit
			view.setMode(Mode.EDIT);
		} else if (view.getMode() == Mode.EDIT) {
			// Cancel edit
			Long id = view.getOrder().getId();
			if (id == null) {
				navigationManager.navigateTo(StorefrontView.class);
			} else {
				enterView(id);
			}
		}
	}

	public void okPressed() {
		if (view.getMode() == Mode.REPORT) {
			// Set next state
			Order order = view.getOrder();
			Optional<OrderState> nextState = getNextHappyPathState(order.getState());
			if (!nextState.isPresent()) {
				throw new IllegalStateException(
						"The next state button should never be enabled when the state does not follow the happy path");
			}
			orderService.changeState(order, nextState.get(), accessControl.getUser());
			refresh(order.getId());
		} else if (view.getMode() == Mode.CONFIRMATION) {
			Order order = saveOrder();
			if (order != null) {
				// Navigate to edit view so URL is updated correctly
				navigationManager.updateViewParameter("" + order.getId());
				enterView(order.getId());
			}
		} else if (view.getMode() == Mode.EDIT) {
			Optional<HasValue<?>> firstErrorField = view.validate().findFirst();
			if (firstErrorField.isPresent()) {
				((Focusable) firstErrorField.get()).focus();
				return;
			}
			// New order should still show a confirmation page
			Order order = view.getOrder();
			if (order.getId() == null) {
				filterEmptyProducts();
				view.setMode(Mode.CONFIRMATION);
			} else {
				order = saveOrder();
				if (order != null) {
					refresh(order.getId());
				}
			}
		}
	}

	private void refresh(Long id) {
		Order order = orderService.findOrder(id);
		if (order == null) {
			view.showNotFound();
			return;
		}
		refreshView(order);

	}

	private void refreshView(Order order) {
		view.setOrder(order);
		updateTotalSum();
		if (order.getId() == null) {
			view.setMode(Mode.EDIT);
		} else {
			view.setMode(Mode.REPORT);
		}
	}

	private void filterEmptyProducts() {
		LinkedList<OrderItem> emptyRows = new LinkedList<>();
		for (OrderItem orderItem : view.getOrder().getItems()) {
			if (orderItem.getProduct() == null) {
				emptyRows.add(orderItem);
			}
		}
		emptyRows.forEach(this::removeOrderItem);
	}

	private Order saveOrder() {
		try {
			filterEmptyProducts();
			Order order = view.getOrder();
			return orderService.saveOrder(order, accessControl.getUser());
		} catch (ValidationException e) {
			// Should not get here if validation is setup properly
			Notification.show("Please check the contents of the fields: " + e.getMessage(), Type.ERROR_MESSAGE);
			getLogger().error("Validation error during order save", e);
			return null;
		} catch (Exception e) {
			if (PersistenceExceptionUtil.isOptimisticLockingException(e)) {
				// Somebody else probably edited the data at the same time
				Notification.show("Somebody else might have updated the data. Please refresh and try again.",
						Type.ERROR_MESSAGE);
				getLogger().debug("Optimistic locking error while saving order", e);
				return null;
			} else if (PersistenceExceptionUtil.isConstraintViolationException(e)) {
				// Should not get here if validation is setup properly
				Notification.show("Please check the contents of the fields: " + e.getMessage(), Type.ERROR_MESSAGE);
				getLogger().error("Constraint violation error during order save", e);
				return null;
			} else {
				// Something went wrong, no idea what
				Notification.show("An unexpected error occurred while saving. Please refresh and try again.",
						Type.ERROR_MESSAGE);
				getLogger().error("Unable to save order", e);
				return null;
			}
		}
	}

	public Optional<OrderState> getNextHappyPathState(OrderState current) {
		final int currentIndex = happyPath.indexOf(current);
		if (currentIndex == -1 || currentIndex == happyPath.size() - 1) {
			return Optional.empty();
		}
		return Optional.of(happyPath.get(currentIndex + 1));
	}

	private void removeOrderItem(OrderItem orderItem) {
		view.removeOrderItem(orderItem);
		updateTotalSum();
	}
}
