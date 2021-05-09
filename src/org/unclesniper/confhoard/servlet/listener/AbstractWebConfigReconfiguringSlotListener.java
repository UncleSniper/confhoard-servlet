package org.unclesniper.confhoard.servlet.listener;

import java.io.IOException;
import java.util.function.Function;
import org.unclesniper.confhoard.core.Fragment;
import org.unclesniper.confhoard.servlet.WebConfig;
import org.unclesniper.confhoard.core.util.Listeners;
import org.unclesniper.confhoard.core.ConfHoardException;
import org.unclesniper.confhoard.servlet.WebConfigHolder;
import org.unclesniper.confhoard.core.security.Credentials;
import org.unclesniper.confhoard.core.listener.SelectingSlotListener;

public abstract class AbstractWebConfigReconfiguringSlotListener extends SelectingSlotListener {

	private final Listeners<WebConfigReconfigurationListener> reconfigurationListeners
			= new Listeners<WebConfigReconfigurationListener>();

	private WebConfigHolderProvider webConfigHolder;

	private WebConfigHolder cachedWebConfigHolder;

	private boolean cacheWebConfigHolder;

	public AbstractWebConfigReconfiguringSlotListener() {
		webConfigHolder = new RequestParameterWebConfigHolderProvider();
	}

	public void addReconfigurationListener(WebConfigReconfigurationListener listener) {
		reconfigurationListeners.addListener(listener);
	}

	public boolean removeReconfigurationListener(WebConfigReconfigurationListener listener) {
		return reconfigurationListeners.removeListener(listener);
	}

	public void fireWebConfigReconfigured(WebConfigReconfigurationListener.ReconfigurationEvent event)
			throws IOException, ConfHoardException {
		reconfigurationListeners.confFire(listener -> listener.webConfigReconfigured(event), null, null);
	}

	public WebConfigHolderProvider getWebConfigHolder() {
		return webConfigHolder;
	}

	public void setWebConfigHolder(WebConfigHolderProvider webConfigHolder) {
		this.webConfigHolder = webConfigHolder;
	}

	public void setWebConfigHolder(WebConfigHolder webConfigHolder) {
		this.webConfigHolder = webConfigHolder == null
				? null : new IdentityWebConfigHolderProvider(webConfigHolder);
	}

	public void setWebConfigHolder(String parameter) {
		webConfigHolder = parameter == null ? null : new RequestParameterWebConfigHolderProvider(parameter);
	}

	public boolean isCacheWebConfigHolder() {
		return cacheWebConfigHolder;
	}

	public void setCacheWebConfigHolder(boolean cacheWebConfigHolder) {
		this.cacheWebConfigHolder = cacheWebConfigHolder;
	}

	private WebConfigHolder getEffectiveWebConfigHolder(Function<String, Object> requestParameters) {
		if(cachedWebConfigHolder != null)
			return cachedWebConfigHolder;
		if(webConfigHolder == null)
			throw new IllegalStateException("No WebConfigHolder provider has been configured");
		WebConfigHolder holder = webConfigHolder.getWebConfigHolder(requestParameters);
		if(holder == null)
			throw new IllegalStateException("WebConfigHolder provider did not provide a WebConfigHolder");
		if(cacheWebConfigHolder)
			cachedWebConfigHolder = holder;
		return holder;
	}

	private void reconfigure(SlotEvent event, Fragment fragment) throws IOException, ConfHoardException {
		Function<String, Object> requestParameters = event::getRequestParameter;
		WebConfigHolder holder = getEffectiveWebConfigHolder(requestParameters);
		if(fragment == null)
			return;
		Credentials credentials = event.getCredentials();
		WebConfig newConfig = parseWebConfig(event, fragment);
		if(newConfig == null)
			return;
		WebConfig oldConfig = holder.getWebConfig();
		fireWebConfigReconfigured(new WebConfigReconfigurationListener.ReconfigurationEvent(oldConfig,
				newConfig, credentials, requestParameters));
		holder.setWebConfig(newConfig);
	}

	protected abstract WebConfig parseWebConfig(SlotEvent event, Fragment fragment)
			throws IOException, ConfHoardException;

	@Override
	protected void selectedSlotLoaded(SlotLoadedEvent event) throws IOException, ConfHoardException {
		reconfigure(event, event.getSlot().getFragment());
	}

	@Override
	protected void selectedSlotUpdated(SlotUpdatedEvent event) throws IOException, ConfHoardException {
		reconfigure(event, event.getNextFragment());
	}

}
