package com.testAppManager.test01.ui.views.admin.user;

import java.io.Serializable;

import javax.inject.Inject;

import com.vaadin.cdi.ViewScoped;
import com.testAppManager.test01.backend.data.entity.User;
import com.testAppManager.test01.backend.service.UserService;
import com.testAppManager.test01.ui.navigation.NavigationManager;
import com.testAppManager.test01.ui.views.admin.AbstractCrudPresenter;

@ViewScoped
public class UserAdminPresenter extends AbstractCrudPresenter<User, UserService, UserAdminView>
		implements Serializable {

	@Inject
	public UserAdminPresenter(UserAdminDataProvider userAdminDataProvider, NavigationManager navigationManager,
			UserService service) {
		super(navigationManager, service, User.class, userAdminDataProvider);
	}

	public String encodePassword(String value) {
		return getService().encodePassword(value);
	}

	@Override
	protected void editItem(User item) {
		super.editItem(item);
		getView().setPasswordRequired(item.isNew());
	}
}