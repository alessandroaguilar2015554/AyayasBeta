package com.testAppManager.test01.ui;

import com.vaadin.addon.charts.model.style.SolidColor;
import com.vaadin.addon.charts.model.style.Theme;
import com.vaadin.addon.charts.themes.ValoLightTheme;

/**
 * Theme for Vaadin Charts. See {@link ValoLightTheme} for a more complex theme.
 */
public class ChartsTheme extends Theme {

	private static final SolidColor COLOR1 = new SolidColor("#8740EC");
	private static final SolidColor COLOR2 = new SolidColor("#0FB6D5");
	private static final SolidColor COLOR3 = new SolidColor("#071658");

	public ChartsTheme() {
		setColors(COLOR1, COLOR2, COLOR3);
		getTitle().setColor(COLOR1);
		getTitle().setFontSize("inherit"); // inherit from CSS
	}
}
