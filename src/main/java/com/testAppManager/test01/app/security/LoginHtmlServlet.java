package com.testAppManager.test01.app.security;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.testAppManager.test01.app.HasLogger;

@WebServlet(asyncSupported = true, urlPatterns = LoginHtmlServlet.LOGIN_HTML)
public class LoginHtmlServlet extends HttpServlet implements HasLogger {

	public static final String LOGIN_HTML = "/login.html";

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		serveLoginHtml(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if (req.getAttribute("shiroLoginFailure") != null) {
			try {
				resp.sendRedirect(LOGIN_HTML + "?error");
				return;
			} catch (Exception e) {
				getLogger().error("Failed to redirect to login error page", e);
			}
		}
		serveLoginHtml(req, resp);
	}

	private void serveLoginHtml(HttpServletRequest request, HttpServletResponse response) throws IOException {
		InputStream loginHtml = request.getServletContext().getResourceAsStream(LOGIN_HTML);
		response.setCharacterEncoding("utf-8");
		org.apache.commons.io.IOUtils.copy(loginHtml, response.getOutputStream());

	}
}
