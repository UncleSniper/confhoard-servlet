package org.unclesniper.confhoard.servlet;

import org.unclesniper.confhoard.core.Slot;
import org.unclesniper.confhoard.core.security.SlotAction;
import org.unclesniper.confhoard.core.security.Credentials;
import org.unclesniper.logging.StringAndExceptionLogMessage;

public class RequestErrorLogMessage extends StringAndExceptionLogMessage {

	private final String guruMeditation;

	private final Slot slot;

	private final SlotAction slotAction;

	private final Credentials credentials;

	public RequestErrorLogMessage(String guruMeditation, Slot slot, SlotAction slotAction, Credentials credentials,
			Exception exception) {
		super(RequestErrorLogMessage.generateMessage(guruMeditation, slot, slotAction, credentials, exception),
				exception);
		this.guruMeditation = guruMeditation;
		this.slot = slot;
		this.slotAction = slotAction;
		this.credentials = credentials;
	}

	public String getGuruMeditation() {
		return guruMeditation;
	}

	public Slot getSlot() {
		return slot;
	}

	public SlotAction getSlotAction() {
		return slotAction;
	}

	public Credentials getCredentials() {
		return credentials;
	}

	private static String generateMessage(String guruMeditation, Slot slot, SlotAction slotAction,
			Credentials credentials, Exception exception) {
		if(slot == null)
			throw new IllegalArgumentException("Slot cannot be null");
		if(slotAction == null)
			throw new IllegalArgumentException("Slot action cannot bt null");
		if(exception == null)
			throw new IllegalArgumentException("Exception cannot be null");
		StringBuilder builder = new StringBuilder();
		if(guruMeditation != null) {
			builder.append("Guru meditation ");
			builder.append(guruMeditation);
			builder.append(": ");
		}
		builder.append("Failed to ");
		builder.append(slotAction.getActionName());
		builder.append(" slot '");
		builder.append(slot.getKey());
		builder.append('\'');
		if(credentials != null) {
			builder.append(" for ");
			builder.append(credentials.toString());
		}
		builder.append(':');
		return builder.toString();
	}

}
