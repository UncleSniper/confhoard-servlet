package org.unclesniper.confhoard.servlet;

import java.util.Date;
import java.util.Iterator;
import org.unclesniper.logging.LogMessage;
import org.unclesniper.confhoard.core.Slot;
import org.unclesniper.logging.util.MultiIterator;
import org.unclesniper.confhoard.core.SlotUpdateIssue;
import org.unclesniper.confhoard.core.security.SlotAction;
import org.unclesniper.confhoard.core.security.Credentials;

public class SlotUpdateFailedLogMessage implements LogMessage, Iterable<String> {

	private final RequestErrorLogMessage requestError;

	private final SlotUpdateIssue updateIssue;

	public SlotUpdateFailedLogMessage(String guruMeditation, Slot slot, SlotAction slotAction,
			Credentials credentials, Exception exception, SlotUpdateIssue updateIssue) {
		requestError = new RequestErrorLogMessage(guruMeditation, slot, slotAction, credentials, exception);
		if(updateIssue == null)
			throw new IllegalArgumentException("Slot update issue cannot be null");
		this.updateIssue = updateIssue;
	}

	public RequestErrorLogMessage getRequestError() {
		return requestError;
	}

	public SlotUpdateIssue getUpdateIssue() {
		return updateIssue;
	}

	@Override
	public Iterable<String> toLogMessageLines() {
		return this;
	}

	@Override
	public Date getLogMessageTimestamp() {
		return requestError.getLogMessageTimestamp();
	}

	@Override
	public Iterator<String> iterator() {
		return new MultiIterator<String>(requestError, () -> updateIssue.getMessageLines());
	}

}
