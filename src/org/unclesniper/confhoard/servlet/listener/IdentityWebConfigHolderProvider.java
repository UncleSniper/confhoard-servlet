package org.unclesniper.confhoard.servlet.listener;

import java.util.function.Function;
import org.unclesniper.confhoard.servlet.WebConfigHolder;

public class IdentityWebConfigHolderProvider implements WebConfigHolderProvider {

	private WebConfigHolder webConfigHolder;

	public IdentityWebConfigHolderProvider() {}

	public IdentityWebConfigHolderProvider(WebConfigHolder webConfigHolder) {
		this.webConfigHolder = webConfigHolder;
	}

	public WebConfigHolder getWebConfigHolder() {
		return webConfigHolder;
	}

	public void setWebConfigHolder(WebConfigHolder webConfigHolder) {
		this.webConfigHolder = webConfigHolder;
	}

	@Override
	public WebConfigHolder getWebConfigHolder(Function<String, Object> requestParameters) {
		return webConfigHolder;
	}

}
