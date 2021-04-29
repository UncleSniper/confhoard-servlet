package org.unclesniper.confhoard.servlet.resource;

import java.util.Map;
import java.util.Locale;
import java.util.HashMap;
import java.util.ResourceBundle;

public class Localization {

	private Locale locale;

	private final Map<String, ResourceBundle> bundles = new HashMap<String, ResourceBundle>();

	private ClassLoader defaultLoader;

	public String getLocaleName() {
		return locale == null ? null : locale.toString();
	}

	public void setLocaleName(String name) {
		if(name == null) {
			locale = null;
			return;
		}
		int lpos = name.indexOf('_');
		if(lpos < 0) {
			if(name.trim().length() == 0)
				locale = null;
			else
				locale = new Locale(name);
		}
		else {
			String lang = name.substring(0, lpos);
			int rpos = name.indexOf('_', lpos + 1);
			if(rpos < 0)
				locale = new Locale(lang, name.substring(lpos + 1));
			else
				locale = new Locale(lang, name.substring(lpos + 1, rpos), name.substring(rpos + 1));
		}
	}

	public Locale getLocale() {
		return locale;
	}

	public Locale getEffectiveLocale() {
		return locale == null ? Locale.getDefault() : locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	public ClassLoader getDefaultLoader() {
		return defaultLoader;
	}

	public void setDefaultLoader(ClassLoader defaultLoader) {
		this.defaultLoader = defaultLoader;
	}

	public ResourceBundle getBundle(String name) {
		return getBundle(name, (ClassLoader)null);
	}

	public ResourceBundle getBundle(String name, Class<?> clazz) {
		return getBundle(name, clazz == null ? null : clazz.getClassLoader());
	}

	public ResourceBundle getBundle(String name, ClassLoader loader) {
		if(name == null)
			return null;
		synchronized(bundles) {
			ResourceBundle bundle = bundles.get(name);
			if(bundle != null)
				return bundle;
			if(loader == null)
				loader = Localization.class.getClassLoader();
			bundle = ResourceBundle.getBundle(name, locale == null ? Locale.getDefault() : locale, loader);
			bundles.put(name, bundle);
			return bundle;
		}
	}

	public ResourceBundle getBundleFromDefault(String name) {
		return getBundle(name, defaultLoader);
	}

	public ResourceBundle getBundleFromDefaultOr(String name, Class<?> clazz) {
		return defaultLoader == null ? getBundle(name, clazz) : getBundleFromDefault(name);
	}

	public ResourceBundle getBundleFromDefaultOr(String name, ClassLoader loader) {
		return defaultLoader == null ? getBundle(name, loader) : getBundleFromDefault(name);
	}

	public static String getBundleFor(Class<?> clazz, String name) {
		if(clazz == null)
			clazz = Localization.class;
		String cn = clazz.getName();
		int pos = cn.lastIndexOf('.');
		return pos < 0 ? name : cn.substring(0, pos + 1) + name;
	}

}
