package org.unclesniper.confhoard.servlet.listener;

import java.io.IOException;
import java.util.function.Function;
import org.unclesniper.confhoard.servlet.WebConfig;
import org.unclesniper.confhoard.core.ConfHoardException;
import org.unclesniper.confhoard.core.security.Credentials;

public interface WebConfigReconfigurationListener {

	public static class ReconfigurationEvent {

		private final WebConfig previousConfig;

		private final WebConfig nextConfig;

		private final Credentials credentials;

		private final Function<String, Object> requestParameters;

		public ReconfigurationEvent(WebConfig previousConfig, WebConfig nextConfig, Credentials credentials,
				Function<String, Object> requestParameters) {
			this.previousConfig = previousConfig;
			this.nextConfig = nextConfig;
			this.credentials = credentials;
			this.requestParameters = requestParameters;
		}

		public WebConfig getPreviousConfig() {
			return previousConfig;
		}

		public WebConfig getNextConfig() {
			return nextConfig;
		}

		public Credentials getCredentials() {
			return credentials;
		}

		public Object getRequestParameter(String key) {
			return key == null || requestParameters == null ? null : requestParameters.apply(key);
		}

	}

	void webConfigReconfigured(ReconfigurationEvent event) throws IOException, ConfHoardException;

}
