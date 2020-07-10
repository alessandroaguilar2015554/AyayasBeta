package com.testAppManager.test01.ui;

import javax.inject.Inject;

import com.vaadin.addon.charts.ChartOptions;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.annotations.Viewport;
import com.vaadin.cdi.CDIUI;
import com.vaadin.navigator.PushStateNavigation;
import com.vaadin.navigator.ViewDisplay;
import com.vaadin.server.DefaultErrorHandler;
import com.vaadin.server.VaadinRequest;
import com.testAppManager.test01.app.HasLogger;
import com.testAppManager.test01.ui.navigation.NavigationManager;
import com.vaadin.ui.UI;

@Theme("apptheme")
@CDIUI("")
@Viewport("width=device-width,initial-scale=1.0,user-scalable=no")
@Title("Ayaya")
@PushStateNavigation
public class AppUI extends UI implements HasLogger {

	private final NavigationManager navigationManager;

	private final MainView mainView;

	@Inject
	public AppUI(NavigationManager navigationManager, MainView mainView) {
		this.navigationManager = navigationManager;
		this.mainView = mainView;
	}

	@Override
	protected void init(VaadinRequest vaadinRequest) {
		navigationManager.init(this, (ViewDisplay) mainView);
		setErrorHandler(event -> {
			Throwable t = DefaultErrorHandler.findRelevantThrowable(event.getThrowable());
			getLogger().error("Error during request", t);
		});

		// Set the theme ("globally") for all Charts
		ChartOptions.get(this).setTheme(new ChartsTheme());

		setContent(mainView);

		navigationManager.navigateToDefaultView();
	}
}
