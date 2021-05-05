package org.unclesniper.confhoard.servlet.security;

import java.util.Base64;
import java.nio.charset.StandardCharsets;
import javax.servlet.http.HttpServletRequest;
import org.unclesniper.confhoard.core.util.HexUtils;
import org.unclesniper.confhoard.core.util.HashUtils;
import org.unclesniper.confhoard.core.security.Credentials;

public class HTTPBasicAuthenticator extends AbstractUserPasswordAuthenticator {

	public HTTPBasicAuthenticator() {}

	@Override
	public Credentials authenticate(HttpServletRequest request) {
		String header = request.getHeader("Authorization");
		if(header == null)
			return null;
		if(!header.startsWith("Basic "))
			return null;
		header = header.substring(6);
		Base64.Decoder decoder = Base64.getDecoder();
		byte[] bytes = decoder.decode(header);
		String str = new String(bytes, StandardCharsets.UTF_8);
		int pos = str.indexOf(':');
		if(pos < 0)
			return null;
		String username = str.substring(0, pos);
		// password = provided password, plain and simple
		String password = str.substring(pos + 1);
		PasswordAndCredentials pac = getUser(username);
		if(pac == null)
			return null;
		// correctPassword = correct password (hashed with salt), as hex string
		String correctPassword = pac.getPassword();
		if(correctPassword == null)
			return null;
		// passwordBytes = provided password as bytes
		byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);
		// salt = salt, as hex string
		String salt = pac.getSalt();
		// saltBytes = salt, as bytes
		byte[] saltBytes = salt == null ? null : HexUtils.fromHexString(salt);
		if(saltBytes != null) {
			byte[] tmp = new byte[passwordBytes.length + saltBytes.length];
			for(int i = 0; i < saltBytes.length; ++i)
				tmp[i] = saltBytes[i];
			for(int i = 0; i < passwordBytes.length; ++i)
				tmp[saltBytes.length + i] = passwordBytes[i];
			passwordBytes = tmp;
		}
		// passwordBytes = salt + provided password, as bytes
		// passwordHash = hash(salt + provided password), as bytes
		byte[] passwordHash = HashUtils.hashBytes(passwordBytes, getEffectiveHashAlgorithm());
		// passwordHex = hash(salt + provided password), has hex string
		String passwordHex = HexUtils.toHexString(passwordHash);
		if(!passwordHex.equalsIgnoreCase(correctPassword))
			return null;
		return pac.getCredentials();
	}

}
