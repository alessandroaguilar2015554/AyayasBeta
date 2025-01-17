package com.testAppManager.test01.ui.views;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;

import com.testAppManager.test01.AbstractIT;
import com.testAppManager.test01.ui.views.dashboard.DashboardViewElement;

public class LoginIT extends AbstractIT {

	@Test
	public void userIsRedirectedToRequestedView() {
		openLoginView(APP_URL + "dashboard").login("barista@vaadin.com", "barista");
		Assert.assertNotNull($(DashboardViewElement.class).first());
	}

	@Test
	public void logoutWorks() {
		loginAsBarista();
		$(MenuElement.class).first().logout();
		Assert.assertEquals("Email", findElement(By.id("login-label")).getText());
	}

}
