package com.testAppManager.test01.ui.views.orderedit;

import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.vaadin.data.BeanValidationBinder;
import com.vaadin.data.BindingValidationStatus;
import com.vaadin.data.HasValue;
import com.vaadin.data.ValueContext;
import com.vaadin.data.converter.StringToIntegerConverter;
import com.testAppManager.test01.backend.data.entity.OrderItem;
import com.testAppManager.test01.backend.data.entity.Product;
import com.testAppManager.test01.ui.utils.DollarPriceConverter;
import com.vaadin.ui.Label;

@Dependent
public class ProductInfo extends ProductInfoDesign {

	private final DollarPriceConverter priceFormatter;

	private final javax.enterprise.event.Event<ProductInfoChangeEvent> productInfoChangeEvent;
	private final javax.enterprise.event.Event<OrderItemDeletedEvent> orderItemDeletedEvent;

	private BeanValidationBinder<OrderItem> binder;

	// Use Label instead of TextArea in "report mode" for a better presentation
	private Label readOnlyComment = new Label();

	private boolean reportMode = false;

	@Inject
	public ProductInfo(DollarPriceConverter priceFormatter,
			javax.enterprise.event.Event<ProductInfoChangeEvent> productInfoChangeEvent,
			javax.enterprise.event.Event<OrderItemDeletedEvent> orderItemDeleted) {
		this.priceFormatter = priceFormatter;
		this.productInfoChangeEvent = productInfoChangeEvent;
		orderItemDeletedEvent = orderItemDeleted;
	}

	@PostConstruct
	public void init() {
		binder = new BeanValidationBinder<>(OrderItem.class);
		binder.setRequiredConfigurator(null);
		binder.forField(quantity).withConverter(new StringToIntegerConverter(-1, "Please enter a number"))
				.bind("quantity");
		binder.bindInstanceFields(this);
		binder.addValueChangeListener(e -> fireProductInfoChanged());

		product.addSelectionListener(e -> {
			Optional<Product> selectedProduct = e.getFirstSelectedItem();
			int productPrice = selectedProduct.map(Product::getPrice).orElse(0);
			updatePrice(productPrice);
		});

		readOnlyComment.setWidth("100%");
		readOnlyComment.setId(comment.getId());
		readOnlyComment.setStyleName(comment.getStyleName());

		delete.addClickListener(e -> fireOrderItemDeleted());
	}

	private void updatePrice(int productPrice) {
		price.setValue(priceFormatter.convertToPresentation(productPrice, new ValueContext(Locale.US)));
	}

	private void fireProductInfoChanged() {
		productInfoChangeEvent.fire(new ProductInfoChangeEvent());
	}

	private void fireOrderItemDeleted() {
		orderItemDeletedEvent.fire(new OrderItemDeletedEvent(getItem()));
	}

	public int getSum() {
		OrderItem item = getItem();
		return item.getQuantity() * item.getProduct().getPrice();
	}

	public void setItem(OrderItem item) {
		binder.setBean(item);
	}

	public OrderItem getItem() {
		return binder.getBean();
	}

	public void setReportMode(boolean reportMode) {
		if (reportMode == this.reportMode) {
			return;
		}
		this.reportMode = reportMode;
		binder.setReadOnly(reportMode);
		delete.setVisible(!reportMode);

		// Swap the TextArea for a Label in report mode
		if (reportMode) {
			readOnlyComment.setVisible(!comment.isEmpty());
			readOnlyComment.setValue(comment.getValue());
			replaceComponent(comment, readOnlyComment);
		} else {
			replaceComponent(readOnlyComment, comment);
		}
	}

	/**
	 * Checks if no product has been selected. If no product is selected, the
	 * whole product info section is ignored when saving changes.
	 *
	 * @return <code>true</code> if no product is selected, <code>false</code>
	 *         otherwise
	 */
	public boolean isEmpty() {
		return product.isEmpty();
	}

	public Stream<HasValue<?>> validate() {
		return binder.validate().getFieldValidationErrors().stream().map(BindingValidationStatus::getField);
	}

	@Override
	public void focus() {
		product.focus();
	}
}
