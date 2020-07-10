package com.testAppManager.test01.ui.views.admin.product;

import javax.inject.Inject;

import com.vaadin.cdi.ViewScoped;
import com.testAppManager.test01.backend.data.entity.Product;
import com.testAppManager.test01.backend.service.ProductService;
import com.testAppManager.test01.ui.navigation.NavigationManager;
import com.testAppManager.test01.ui.views.admin.AbstractCrudPresenter;

@ViewScoped
public class ProductAdminPresenter extends AbstractCrudPresenter<Product, ProductService, ProductAdminView> {

	@Inject
	public ProductAdminPresenter(ProductAdminDataProvider productAdminDataProvider, NavigationManager navigationManager,
			ProductService service) {
		super(navigationManager, service, Product.class, productAdminDataProvider);
	}
}
