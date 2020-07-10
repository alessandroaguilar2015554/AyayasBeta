package com.testAppManager.test01.ui.views;

import javax.enterprise.context.Dependent;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;

@Dependent
public class AccessDeniedView extends AccessDeniedDesign implements View {

	@Override
	public void enter(ViewChangeEvent event) {
		// Nothing to do, just show the view
	}

}
