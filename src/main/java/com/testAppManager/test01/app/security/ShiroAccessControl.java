package com.testAppManager.test01.app.security;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import com.vaadin.cdi.access.AccessControl;
import com.testAppManager.test01.backend.data.entity.User;
import com.testAppManager.test01.backend.service.UserService;

/**
 * A simple access control implementation using Apache Shiro.
 */
@ApplicationScoped
@Alternative
public class ShiroAccessControl extends AccessControl {

	@Inject
	private UserService userService;

	public ShiroAccessControl() {
		// Normal scoped beans must have a no-arg constructor
	}

	@Override
	public boolean isUserSignedIn() {
		Subject currentUser = SecurityUtils.getSubject();
		return currentUser.isAuthenticated();
	}

	@Override
	public boolean isUserInRole(String role) {
		Subject currentUser = SecurityUtils.getSubject();
		return currentUser.hasRole(role);
	}

	@Override
	public String getPrincipalName() {
		Subject currentUser = SecurityUtils.getSubject();
		Object principal = currentUser.getPrincipal();
		return (principal == null) ? null : String.valueOf(principal);
	}

	public User getUser() {
		return userService.findByEmail(getPrincipalName());
	}

}