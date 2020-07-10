package com.testAppManager.test01.app;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;

import com.vaadin.ui.Component;
import com.vaadin.ui.declarative.Design;
import com.vaadin.ui.declarative.Design.DefaultComponentFactory;
import com.vaadin.ui.declarative.DesignContext;

@Startup
@Singleton
public class DesignInjectSupport {

	@PostConstruct
	public void init() {
		Design.setComponentFactory(new DefaultComponentFactory() {

			@Override
			public Component createComponent(String fullyQualifiedClassName, DesignContext context) {
				Class<? extends Component> componentClass = resolveComponentClass(fullyQualifiedClassName, context);

				Instance<? extends Component> managedComponent = CDI.current().select(componentClass);
				if (!managedComponent.isAmbiguous() && !managedComponent.isUnsatisfied()) {
					// Injectable, still not clear if the correct class will be
					// used (e.g. componentClass might be Grid<Foo> and
					// MyOwnGrid might be injected, if that happens to exist in
					// the project
					Component component = managedComponent.get();
					if (component.getClass() == componentClass) {
						return component;
					}
				}

				return super.createComponent(fullyQualifiedClassName, context);
			}
		});
	}
}
