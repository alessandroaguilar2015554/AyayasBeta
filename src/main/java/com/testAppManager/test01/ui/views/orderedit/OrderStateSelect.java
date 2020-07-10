package com.testAppManager.test01.ui.views.orderedit;

import com.vaadin.cdi.ViewScoped;
import com.testAppManager.test01.app.HasLogger;
import com.testAppManager.test01.backend.data.OrderState;
import com.vaadin.ui.ComboBox;

@ViewScoped
public class OrderStateSelect extends ComboBox<OrderState> implements HasLogger {

	public OrderStateSelect() {
		setEmptySelectionAllowed(false);
		setTextInputAllowed(false);
		setItems(OrderState.values());
		setItemCaptionGenerator(OrderState::getDisplayName);
	}

}
