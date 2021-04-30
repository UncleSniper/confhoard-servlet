package org.unclesniper.confhoard.servlet;

import java.util.Random;
import java.util.Iterator;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.OutputStream;
import javax.servlet.http.Part;
import javax.servlet.ServletConfig;
import java.util.function.Function;
import org.unclesniper.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import org.unclesniper.ogdl.Injection;
import org.unclesniper.logging.LogSource;
import org.unclesniper.ogdl.ClassRegistry;
import org.unclesniper.logging.LogMessage;
import org.unclesniper.confhoard.core.Slot;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.unclesniper.logging.DefaultLogLevel;
import org.unclesniper.confhoard.core.ConfState;
import java.util.concurrent.atomic.AtomicInteger;
import org.unclesniper.confhoard.core.util.HoardSink;
import org.unclesniper.confhoard.core.SlotUpdateIssue;
import org.unclesniper.ogdl.ObjectDescriptionException;
import org.unclesniper.confhoard.core.ConfHoardException;
import org.unclesniper.confhoard.core.ConfManagementState;
import org.unclesniper.confhoard.core.security.SlotAction;
import org.unclesniper.confhoard.core.security.Credentials;
import org.unclesniper.logging.StringAndExceptionLogMessage;
import org.unclesniper.confhoard.core.SlotAccessForbiddenException;

public class ConfHoardServlet extends HttpServlet implements WebConfigHolder {

	public static final String DEFAULT_WEBCONFIG_FILE = "/etc/confhoard/web.ogdl";

	public static final String DEFAULT_CONFSTATE_FILE = "/etc/confhoard/confstate.ogdl";

	public static final String DEFAULT_MIME_TYPE = "application/octet-stream";

	private static final Random RANDOM = new Random();

	private static final AtomicInteger GURU_KICKER = new AtomicInteger();

	private static final String HEX_PADDING = "0000000000000000";

	private static final LogSource HANDLEIOEXCEPTION_LOG_SOURCE
			= LogSource.in(ConfHoardServlet.class, "handleIOException");

	private static final LogSource HANDLESLOTACCESSFORBIDDENEXCEPTION_LOG_SOURCE
			= LogSource.in(ConfHoardServlet.class, "handleSlotAccessForbiddenException");

	private static final LogSource HANDLECONFHOARDEXCEPTION_LOG_SOURCE
			= LogSource.in(ConfHoardServlet.class, "handleConfHoardException");

	private static final LogSource HANDLEINITEXCEPTION_LOG_SOURCE
			= LogSource.in(ConfHoardServlet.class, "handleInitException");

	private WebConfig webConfig;

	private ConfManagementState managementState;

	public ConfHoardServlet() {}

	private <T> T loadOGDL(String path, Class<T> type) throws ServletException {
		Injection injection = new Injection(new ClassRegistry());
		injection.setConstructionClassLoader(getClass().getClassLoader());
		injection.registerBuiltinStringClassMappers();
		Object obj;
		try {
			obj = injection.readDescription(path).getRootObject();
		}
		catch(ObjectDescriptionException ode) {
			String message = ode.getMessage();
			throw new ServletException("Error in '" + path + "'" + (message == null || message.length() == 0
					? "" : ": " + message), ode);
		}
		catch(IOException ioe) {
			String message = ioe.getMessage();
			throw new ServletException("I/O error reading '" + path + "'"
					+ (message == null || message.length() == 0 ? "" : ": " + message), ioe);
		}
		if(!type.isInstance(obj))
			throw new ServletException("Root object in '" + path + "' is a " + obj.getClass().getName()
					+ ", but expected a " + type.getName());
		return type.cast(obj);
	}

	private String getSlotKey(HttpServletRequest request) {
		String pathInfo = request.getPathInfo();
		if(pathInfo == null)
			return "";
		if(pathInfo.length() > 0 && pathInfo.charAt(0) == '/')
			return pathInfo.substring(1);
		return pathInfo;
	}

	private void noSuchSlot(String key, HttpServletResponse response) throws IOException {
		response.setStatus(404);
		response.setContentType("text/plain");
		response.getWriter().println("No such slot: " + key);
	}

	private Slot getSlot(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String slotKey = getSlotKey(request);
		Slot slot = managementState.getSlot(slotKey);
		if(slot == null)
			noSuchSlot(slotKey, response);
		return slot;
	}

	private String getContentType(HttpServletRequest request) {
		String contentType = request.getContentType();
		if(contentType == null)
			return ConfHoardServlet.DEFAULT_MIME_TYPE;
		int pos = contentType.indexOf(';');
		return pos > 0 ? contentType.substring(0, pos) : contentType;
	}

	private Function<String, Object> getRequestParameters(HttpServletRequest request, String knownContentType) {
		String contentType = knownContentType == null ? getContentType(request) : knownContentType;
		String mimeTypeKey = webConfig.getEffectiveMimeTypeKey();
		String loggerKey = webConfig.getEffectiveLoggerKey();
		String webConfigHolderKey = webConfig.getEffectiveWebConfigHolderKey();
		return key -> {
			if(key == null)
				return null;
			Object override = webConfig.getDefaultRequestParameter(key);
			if(override != null)
				return override;
			if(key.equals(mimeTypeKey))
				return contentType;
			if(key.equals(loggerKey))
				return webConfig.getLogger();
			if(key.equals(webConfigHolderKey))
				return this;
			return request.getParameter(key);
		};
	}

	private static String getGuruMeditation() {
		long hi = System.currentTimeMillis(), lo;
		synchronized(ConfHoardServlet.RANDOM) {
			lo = ConfHoardServlet.RANDOM.nextLong();
		}
		int kicker;
		synchronized(ConfHoardServlet.GURU_KICKER) {
			kicker = ConfHoardServlet.GURU_KICKER.incrementAndGet();
		}
		StringBuilder builder = new StringBuilder();
		String piece = Long.toHexString(hi).toUpperCase();
		int length = piece.length();
		if(length < 16)
			builder.append(ConfHoardServlet.HEX_PADDING.substring(length));
		builder.append(piece);
		piece = Long.toHexString(lo).toUpperCase();
		length = piece.length();
		if(length < 16)
			builder.append(ConfHoardServlet.HEX_PADDING.substring(length));
		builder.append(piece);
		piece = Integer.toHexString(kicker).toUpperCase();
		length = piece.length();
		if(length < 8)
			builder.append(ConfHoardServlet.HEX_PADDING.substring(length + 8));
		builder.append(piece);
		return builder.toString();
	}

	private void handleIOException(Slot slot, SlotAction action, Credentials credentials, IOException e,
			HttpServletResponse response) throws IOException {
		handleException(slot, action, credentials, e, action.isReadOnly() ? 500 : 409,
				ConfHoardServlet.HANDLEIOEXCEPTION_LOG_SOURCE, null, response);
	}

	private void handleSlotAccessForbiddenException(Credentials credentials, SlotAccessForbiddenException e,
			HttpServletResponse response) throws IOException {
		handleException(e.getSlot(), e.getSlotAction(), credentials, e, 403,
				ConfHoardServlet.HANDLESLOTACCESSFORBIDDENEXCEPTION_LOG_SOURCE, null, response);
	}

	private void handleConfHoardException(Slot slot, SlotAction action, Credentials credentials,
			ConfHoardException e, HttpServletResponse response) throws IOException {
		SlotUpdateIssue issue = e instanceof SlotUpdateIssue ? (SlotUpdateIssue)e : null;
		handleException(slot, action, credentials, e, action.isReadOnly() ? 500 : 409,
				ConfHoardServlet.HANDLECONFHOARDEXCEPTION_LOG_SOURCE, issue, response);
	}

	private void handleException(Slot slot, SlotAction action, Credentials credentials, Exception e, int status,
			LogSource logSource, SlotUpdateIssue issue, HttpServletResponse response) throws IOException {
		String guruMeditation = ConfHoardServlet.getGuruMeditation();
		Logger logger = webConfig.getLogger();
		if(logger == null)
			e.printStackTrace();
		else {
			LogMessage message;
			if(issue == null)
				message = (LogMessage)new RequestErrorLogMessage(guruMeditation, slot, action, credentials, e);
			else
				message = (LogMessage)new SlotUpdateFailedLogMessage(guruMeditation, slot, action, credentials,
						e, issue);
			logger.log(DefaultLogLevel.ERROR, logSource, message);
		}
		if(response.isCommitted())
			return;
		response.setStatus(status);
		response.setContentType("text/plain");
		StringBuilder builder = new StringBuilder();
		builder.append("Failed to ");
		builder.append(action.getActionName());
		builder.append(" slot '");
		builder.append(slot.getKey());
		builder.append('\'');
		if(credentials != null) {
			builder.append(" for ");
			builder.append(credentials.toString());
		}
		builder.append(": Guru meditation ");
		builder.append(guruMeditation);
		PrintWriter out = response.getWriter();
		out.println(builder.toString());
		if(issue != null) {
			Iterator<String> it = issue.getMessageLines();
			while(it.hasNext())
				out.println(it.next());
		}
	}

	private void retrieveSlot(HttpServletRequest request, HttpServletResponse response) throws IOException {
		Slot slot = getSlot(request, response);
		if(slot == null)
			return;
		Credentials credentials = webConfig.authenticate(request);
		String predefinedResponseType = slot.getMimeType();
		String responseType = predefinedResponseType == null
				? ConfHoardServlet.DEFAULT_MIME_TYPE : predefinedResponseType;
		HoardSink<InputStream> sink = is -> {
			if(is == null) {
				response.setStatus(204);
				return;
			}
			response.setContentType(responseType);
			OutputStream os = response.getOutputStream();
			byte[] buffer = new byte[512];
			for(;;) {
				int count = is.read(buffer);
				if(count <= 0)
					break;
				os.write(buffer, 0, count);
			}
			os.flush();
		};
		try {
			managementState.retrieveSlot(slot, credentials, true, managementState,
					getRequestParameters(request, null), sink);
		}
		catch(IOException ioe) {
			handleIOException(slot, SlotAction.RETRIEVE, credentials, ioe, response);
		}
		catch(SlotAccessForbiddenException safe) {
			handleSlotAccessForbiddenException(credentials, safe, response);
		}
		catch(ConfHoardException che) {
			handleConfHoardException(slot, SlotAction.RETRIEVE, credentials, che, response);
		}
	}

	private void updateSlot(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		Slot slot = getSlot(request, response);
		if(slot == null)
			return;
		Credentials credentials = webConfig.authenticate(request);
		String contentType = getContentType(request);
		InputStream bodyIn;
		switch(contentType) {
			case "application/x-www-form-urlencoded":
				response.setStatus(415);
				response.setContentType("text/plain");
				response.getWriter().println("No upload possible for request type "
						+ "application/x-www-form-urlencoded");
				return;
			case "multipart/form-data":
				{
					String uploadParam = request.getParameter(webConfig.getEffectiveUploadParamParam());
					if(uploadParam == null || uploadParam.length() == 0)
						uploadParam = webConfig.getEffectiveDefaultUploadParam();
					Part uploadPart = request.getPart(uploadParam);
					if(uploadPart == null) {
						response.setStatus(400);
						response.setContentType("text/plain");
						StringBuilder builder = new StringBuilder();
						builder.append("Request does not have a multipart part named '");
						builder.append(uploadParam);
						builder.append('\'');
						response.getWriter().println(builder.toString());
						return;
					}
					bodyIn = uploadPart.getInputStream();
				}
				break;
			default:
				bodyIn = request.getInputStream();
				break;
		}
		try(InputStream is = bodyIn) {
			managementState.updateSlot(slot, is, credentials, true, managementState,
					getRequestParameters(request, contentType));
		}
		catch(IOException ioe) {
			handleIOException(slot, SlotAction.UPDATE, credentials, ioe, response);
			return;
		}
		catch(SlotAccessForbiddenException safe) {
			handleSlotAccessForbiddenException(credentials, safe, response);
			return;
		}
		catch(ConfHoardException che) {
			handleConfHoardException(slot, SlotAction.UPDATE, credentials, che, response);
			return;
		}
		response.setStatus(200);
		response.setContentType("text/plain");
		PrintWriter out = response.getWriter();
		out.print("Slot '");
		out.print(slot.getKey());
		out.println("' updated successfully");
	}

	@Override
	public void init() throws ServletException {
		ServletConfig config = getServletConfig();
		String webWiring = config.getInitParameter("webWiringFile");
		if(webWiring == null || webWiring.length() == 0)
			webWiring = ConfHoardServlet.DEFAULT_WEBCONFIG_FILE;
		String stateWiring = config.getInitParameter("stateWiringFile");
		if(stateWiring == null || stateWiring.length() == 0)
			stateWiring = ConfHoardServlet.DEFAULT_CONFSTATE_FILE;
		webConfig = loadOGDL(webWiring, WebConfig.class);
		managementState = new ConfManagementState();
		ConfState confState = loadOGDL(stateWiring, ConfState.class);
		String loggerKey = webConfig.getEffectiveLoggerKey();
		String webConfigHolderKey = webConfig.getEffectiveWebConfigHolderKey();
		Function<String, Object> params = key -> {
			if(key == null)
				return null;
			Object override = webConfig.getDefaultRequestParameter(key);
			if(override != null)
				return override;
			if(key.equals(loggerKey))
				return webConfig.getLogger();
			if(key.equals(webConfigHolderKey))
				return this;
			return null;
		};
		try {
			confState.getLoadedStorage(managementState, params);
		}
		catch(IOException ioe) {
			handleInitException("I/O error", ioe);
		}
		catch(ConfHoardException che) {
			handleInitException("Error", che);
		}
		managementState.setConfState(confState);
	}

	private void handleInitException(String prefix, Exception e) throws ServletException {
		Logger logger = webConfig.getLogger();
		if(logger == null)
			logger.log(DefaultLogLevel.FATAL, ConfHoardServlet.HANDLEINITEXCEPTION_LOG_SOURCE,
					(LogMessage)new StringAndExceptionLogMessage(prefix + " loading storage", e));
		String message = e.getMessage();
		throw new ServletException(prefix + " loading storage" + (message == null || message.length() == 0
				? "" : ": " + message), e);
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		retrieveSlot(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		updateSlot(request, response);
	}

	@Override
	protected void doPut(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		updateSlot(request, response);
	}

	@Override
	public WebConfig getWebConfig() {
		return webConfig;
	}

	@Override
	public void setWebConfig(WebConfig webConfig) {
		if(webConfig == null)
			throw new IllegalArgumentException("Web config cannot be null");
		this.webConfig = webConfig;
	}

}
