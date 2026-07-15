package com.ralsei.application;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * Provides the servlet initializer component for the application.
 */
public class ServletInitializer extends SpringBootServletInitializer {

	@Override
	/**
	 * Executes the configure operation.
	 *
	 * @param application the value supplied for this operation
	 *
	 * @return the operation result
	 */
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(Application.class);
	}

}
