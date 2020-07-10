package com.testAppManager.test01.app.security;

import java.util.Collections;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAccount;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

import com.testAppManager.test01.backend.data.entity.User;
import com.testAppManager.test01.backend.service.UserService;

public class ShiroAppRealm extends AuthorizingRealm {

	private UserService userService;

	public ShiroAppRealm(CredentialsMatcher credentialsMatcher, UserService userService) {
		super(credentialsMatcher);
		this.userService = userService;
	}

	@Override
	public AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
		UsernamePasswordToken usernamePassword = (UsernamePasswordToken) token;
		String username = usernamePassword.getUsername();
		User user = userService.findByEmail(username);
		if (user == null) {
			return null;
		}
		SimpleAccount account = new SimpleAccount(user.getEmail(), user.getPasswordHash(), getName());
		account.setRoles(Collections.singleton(user.getRole()));
		return account;
	}

	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		String username = (String) principals.getPrimaryPrincipal();
		User user = userService.findByEmail(username);
		if (user == null) {
			return null;
		}
		return new SimpleAuthorizationInfo(Collections.singleton(user.getRole()));
	}

}
