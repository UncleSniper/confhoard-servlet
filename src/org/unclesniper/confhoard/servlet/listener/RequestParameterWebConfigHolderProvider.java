package org.unclesniper.confhoard.servlet.listener;

import java.util.function.Function;
import org.unclesniper.confhoard.servlet.WebConfig;
import org.unclesniper.confhoard.servlet.WebConfigHolder;

public class RequestParameterWebConfigHolderProvider implements WebConfigHolderProvider {

	private String parameter;

	public RequestParameterWebConfigHolderProvider() {}

	public RequestParameterWebConfigHolderProvider(String parameter) {
		this.parameter = parameter;
	}

	public String getParameter() {
		return parameter;
	}

	public void setParameter(String parameter) {
		this.parameter = parameter;
	}

	@Override
	public WebConfigHolder getWebConfigHolder(Function<String, Object> requestParameters) {
		if(requestParameters == null)
			return null;
		String param = parameter;
		if(param == null)
			param = WebConfig.DEFAULT_WEBCONFIGHOLDER_KEY;
		Object obj = requestParameters.apply(param);
		return obj instanceof WebConfigHolder ? (WebConfigHolder)obj : null;
	}

}
