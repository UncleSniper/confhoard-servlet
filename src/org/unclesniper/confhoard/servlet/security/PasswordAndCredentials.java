package org.unclesniper.confhoard.servlet.security;

import org.unclesniper.confhoard.core.security.Credentials;

public class PasswordAndCredentials {

	private String password;

	private Credentials credentials;

	private String salt;

	public PasswordAndCredentials() {}

	public PasswordAndCredentials(String password, Credentials credentials) {
		this.password = password;
		this.credentials = credentials;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Credentials getCredentials() {
		return credentials;
	}

	public void setCredentials(Credentials credentials) {
		this.credentials = credentials;
	}

	public String getSalt() {
		return salt;
	}

	public void setSalt(String salt) {
		this.salt = salt;
	}

}
