package org.unclesniper.confhoard.servlet;

import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Collections;
import org.unclesniper.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import org.unclesniper.confhoard.core.security.Credentials;

public class WebConfig {

	public static final String DEFAULT_LOGGER_KEY = "org.unclesniper.confhoard.logger";

	public static final String DEFAULT_WEBCONFIGHOLDER_KEY = "org.unclesniper.confhoard.webConfigHolder";

	public static final String DEFAULT_MIMETYPE_KEY = "org.unclesniper.confhoard.mimeType";

	public static final String DEFAULT_DEFAULT_UPLOAD_PARAM = "fragment";

	public static final String DEFAULT_UPLOAD_PARAM_PARAM = "uploadField";

	private final List<Authenticator> authenticators = new LinkedList<Authenticator>();

	private Logger logger;

	private String loggerKey;

	private final Map<String, Object> defaultRequestParameters = new HashMap<String, Object>();

	private String webConfigHolderKey;

	private String mimeTypeKey;

	private String defaultUploadParam;

	private String uploadParamParam;

	public WebConfig() {}

	public void addAuthenticator(Authenticator authenticator) {
		if(authenticator == null)
			throw new IllegalArgumentException("Authenticator cannot be null");
		authenticators.add(authenticator);
	}

	public boolean removeAuthenticator(Authenticator authenticator) {
		return authenticators.remove(authenticator);
	}

	public Logger getLogger() {
		return logger;
	}

	public void setLogger(Logger logger) {
		this.logger = logger;
	}

	public String getLoggerKey() {
		return loggerKey;
	}

	public String getEffectiveLoggerKey() {
		return loggerKey == null ? WebConfig.DEFAULT_LOGGER_KEY : loggerKey;
	}

	public void setLoggerKey(String loggerKey) {
		this.loggerKey = loggerKey;
	}

	public Set<String> getDefaultRequestParameters() {
		return Collections.unmodifiableSet(defaultRequestParameters.keySet());
	}

	public void setDefaultRequestParameter(String key, Object value) {
		if(key == null)
			throw new IllegalArgumentException("Parameter name cannot be null");
		if(value == null)
			defaultRequestParameters.remove(key);
		else
			defaultRequestParameters.put(key, value);
	}

	public Object getDefaultRequestParameter(String key) {
		return defaultRequestParameters.get(key);
	}

	public String getWebConfigHolderKey() {
		return webConfigHolderKey;
	}

	public String getEffectiveWebConfigHolderKey() {
		return webConfigHolderKey == null ? WebConfig.DEFAULT_WEBCONFIGHOLDER_KEY : webConfigHolderKey;
	}

	public void setWebConfigHolderKey(String webConfigHolderKey) {
		this.webConfigHolderKey = webConfigHolderKey;
	}

	public String getMimeTypeKey() {
		return mimeTypeKey;
	}

	public String getEffectiveMimeTypeKey() {
		return mimeTypeKey == null ? WebConfig.DEFAULT_MIMETYPE_KEY : mimeTypeKey;
	}

	public void setMimeTypeKey(String mimeTypeKey) {
		this.mimeTypeKey = mimeTypeKey;
	}

	public String getDefaultUploadParam() {
		return defaultUploadParam;
	}

	public String getEffectiveDefaultUploadParam() {
		return defaultUploadParam == null || defaultUploadParam.length() == 0
				? WebConfig.DEFAULT_DEFAULT_UPLOAD_PARAM : defaultUploadParam;
	}

	public void setDefaultUploadParam(String defaultUploadParam) {
		this.defaultUploadParam = defaultUploadParam;
	}

	public String getUploadParamParam() {
		return uploadParamParam;
	}

	public String getEffectiveUploadParamParam() {
		return uploadParamParam == null ? WebConfig.DEFAULT_UPLOAD_PARAM_PARAM : uploadParamParam;
	}

	public void setUploadParamParam(String uploadParamParam) {
		this.uploadParamParam = uploadParamParam;
	}

	public Credentials authenticate(HttpServletRequest request) {
		for(Authenticator authenticator : authenticators) {
			Credentials credentials = authenticator.authenticate(request);
			if(credentials != null)
				return credentials;
		}
		return null;
	}

}
