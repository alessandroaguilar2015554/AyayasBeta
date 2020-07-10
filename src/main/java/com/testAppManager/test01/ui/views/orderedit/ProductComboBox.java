package com.testAppManager.test01.ui.views.orderedit;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.testAppManager.test01.backend.data.entity.Product;
import com.vaadin.ui.ComboBox;

@Dependent
public class ProductComboBox extends ComboBox<Product> {

	@Inject
	public ProductComboBox(ProductComboBoxDataProvider dataProvider) {
		setWidth("100%");
		setEmptySelectionAllowed(false);
		setPlaceholder("Product");
		setItemCaptionGenerator(Product::getName);
		setDataProvider(dataProvider);
	}

}
