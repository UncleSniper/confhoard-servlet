package org.unclesniper.confhoard.servlet.listener;

import java.util.Set;
import java.util.List;
import java.util.HashSet;
import java.io.InputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Collections;
import org.unclesniper.ogdl.Injection;
import org.unclesniper.ogdl.ClassRegistry;
import org.unclesniper.confhoard.core.Slot;
import org.unclesniper.ogdl.TokenSinkWrapper;
import org.unclesniper.ogdl.StringClassMapper;
import org.unclesniper.confhoard.core.Fragment;
import org.unclesniper.confhoard.servlet.WebConfig;
import org.unclesniper.ogdl.ObjectDescriptionException;
import org.unclesniper.confhoard.core.ConfHoardException;
import org.unclesniper.confhoard.ogdl.BadOGDLInSlotException;
import org.unclesniper.confhoard.ogdl.BadOGDLRootObjectException;

public class OGDLWebConfigReconfiguringSlotListener extends AbstractWebConfigReconfiguringSlotListener {

	private boolean registerBuiltinStringClassMappers = true;

	private Set<StringClassMapper> stringClassMappers = new HashSet<StringClassMapper>();

	private final List<TokenSinkWrapper> sinkWrappers = new LinkedList<TokenSinkWrapper>();

	private String charset;

	public OGDLWebConfigReconfiguringSlotListener() {}

	public boolean isRegisterBuiltinStringClassMappers() {
		return registerBuiltinStringClassMappers;
	}

	public void setRegisterBuiltinStringClassMappers(boolean registerBuiltinStringClassMappers) {
		this.registerBuiltinStringClassMappers = registerBuiltinStringClassMappers;
	}

	public Set<StringClassMapper> getStringClassMappers() {
		return Collections.unmodifiableSet(stringClassMappers);
	}

	public void addStringClassMapper(StringClassMapper mapper) {
		if(mapper == null)
			throw new IllegalArgumentException("String class mapper cannot be null");
		stringClassMappers.add(mapper);
	}

	public boolean removeStringClassMapper(StringClassMapper mapper) {
		return stringClassMappers.remove(mapper);
	}

	public List<TokenSinkWrapper> getTokenSinkWrappers() {
		return Collections.unmodifiableList(sinkWrappers);
	}

	public void addTokenSinkWrapper(TokenSinkWrapper wrapper) {
		if(wrapper == null)
			throw new IllegalArgumentException("Token sink wrapper cannot be null");
		sinkWrappers.add(wrapper);
	}

	public boolean removeTokenSinkWrapper(TokenSinkWrapper wrapper) {
		return sinkWrappers.remove(wrapper);
	}

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	@Override
	protected WebConfig parseWebConfig(SlotEvent event, Fragment fragment) throws IOException, ConfHoardException {
		Slot slot = event.getSlot();
		Injection injection = new Injection(new ClassRegistry());
		if(registerBuiltinStringClassMappers)
			injection.registerBuiltinStringClassMappers();
		for(StringClassMapper mapper : stringClassMappers)
			injection.addStringClassMapper(mapper);
		for(TokenSinkWrapper wrapper : sinkWrappers)
			injection.addTokenSinkWrapper(wrapper);
		Object root;
		try(InputStream is = fragment.retrieve(event.getCredentials(), event.getConfState(),
				event::getRequestParameter)) {
			root = injection.readDescription(is, charset, slot.getKey()).getRootObject();
		}
		catch(ObjectDescriptionException ode) {
			throw new BadOGDLInSlotException(slot, ode);
		}
		if(!(root instanceof WebConfig))
			throw new BadOGDLRootObjectException(slot, WebConfig.class, root.getClass());
		return (WebConfig)root;
	}

}
