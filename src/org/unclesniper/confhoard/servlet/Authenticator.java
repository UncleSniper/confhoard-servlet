package org.unclesniper.confhoard.servlet;

import javax.servlet.http.HttpServletRequest;
import org.unclesniper.confhoard.core.security.Credentials;

public interface Authenticator {

	Credentials authenticate(HttpServletRequest request);

}
