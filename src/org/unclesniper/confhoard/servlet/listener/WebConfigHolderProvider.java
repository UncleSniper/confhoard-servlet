package org.unclesniper.confhoard.servlet.listener;

import java.util.function.Function;
import org.unclesniper.confhoard.servlet.WebConfigHolder;

public interface WebConfigHolderProvider {

	WebConfigHolder getWebConfigHolder(Function<String, Object> requestParameters);

}
