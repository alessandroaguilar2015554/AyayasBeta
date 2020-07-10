package com.testAppManager.test01.app.security;

import javax.enterprise.context.ApplicationScoped;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.mindrot.jbcrypt.BCrypt;

@ApplicationScoped
public class BcryptPasswordMatcher implements CredentialsMatcher {

	@Override
	public boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) {
		UsernamePasswordToken userToken = (UsernamePasswordToken) token;
		String password = String.valueOf(userToken.getPassword());
		String credentials = (String) info.getCredentials();
		String hashed = String.valueOf(credentials);
		return BCrypt.checkpw(password, hashed);
	}

	public String encode(String pwd) {
		return BCrypt.hashpw(pwd, BCrypt.gensalt());
	}

}
