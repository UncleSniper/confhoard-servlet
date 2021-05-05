package org.unclesniper.confhoard.servlet.security;

import java.util.Map;
import java.util.HashMap;
import org.unclesniper.confhoard.core.ConfState;

public abstract class AbstractUserPasswordAuthenticator implements Authenticator {

	private final Map<String, PasswordAndCredentials> users = new HashMap<String, PasswordAndCredentials>();

	private String hashAlgorithm;

	public AbstractUserPasswordAuthenticator() {}

	public PasswordAndCredentials getUser(String username) {
		return users.get(username);
	}

	public void setUser(String username, PasswordAndCredentials pac) {
		if(username == null)
			throw new IllegalArgumentException("Username cannot be null");
		if(pac == null)
			throw new IllegalArgumentException("Password-and-credentials cannot be null");
		PasswordAndCredentials old = users.get(username);
		if(old != null && old != pac)
			throw new IllegalArgumentException("Duplicate username: " + username);
		users.put(username, pac);
	}

	public String getHashAlgorithm() {
		return hashAlgorithm;
	}

	public void setHashAlgorithm(String hashAlgorithm) {
		this.hashAlgorithm = hashAlgorithm;
	}

	public String getEffectiveHashAlgorithm() {
		return hashAlgorithm == null ? ConfState.DEFAULT_HASH_ALGORITHM : hashAlgorithm;
	}

}
