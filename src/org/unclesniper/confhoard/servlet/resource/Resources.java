package org.unclesniper.confhoard.servlet.resource;

import java.util.ResourceBundle;

public final class Resources {

	public static final String L10N_BUNDLE = Localization.getBundleFor(Resources.class, "confhoard-servlet");

	private Resources() {}

	public static ResourceBundle getBundle(Localization localization) {
		if(localization == null)
			localization = new Localization();
		return localization.getBundleFromDefaultOr(Resources.L10N_BUNDLE, Resources.class);
	}

}
