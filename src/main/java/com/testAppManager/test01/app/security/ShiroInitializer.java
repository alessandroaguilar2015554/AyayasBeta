package com.testAppManager.test01.app.security;

import java.util.ArrayList;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.annotation.WebListener;

import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.web.env.EnvironmentLoaderListener;
import org.apache.shiro.web.env.WebEnvironment;

import com.testAppManager.test01.backend.service.UserService;

@WebListener
public class ShiroInitializer extends EnvironmentLoaderListener {
	@Inject
	private UserService userService;

	@Inject
	private CredentialsMatcher credentialsMatcher;

	@Override
	protected WebEnvironment createEnvironment(ServletContext arg0) {
		WebEnvironment environment = super.createEnvironment(arg0);
		RealmSecurityManager realmSecurityManager = (RealmSecurityManager) environment.getSecurityManager();

		ShiroAppRealm appRealm = new ShiroAppRealm(credentialsMatcher, userService);

		ArrayList<Realm> realms = new ArrayList<>();
		realms.add(appRealm);
		realmSecurityManager.setRealms(realms);

		return environment;
	}
}
