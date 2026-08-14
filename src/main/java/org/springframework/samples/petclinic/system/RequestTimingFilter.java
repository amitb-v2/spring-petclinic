package org.springframework.samples.petclinic.system;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Logs how long each request took. Operational instrumentation only — it changes no
 * behaviour the application exposes to a user.
 */
@Component
public class RequestTimingFilter extends OncePerRequestFilter {

	private static final Logger log = LoggerFactory.getLogger(RequestTimingFilter.class);

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
			FilterChain chain) throws ServletException, IOException {
		long started = System.nanoTime();
		try {
			chain.doFilter(request, response);
		}
		finally {
			long millis = (System.nanoTime() - started) / 1_000_000;
			log.debug("{} {} took {}ms", request.getMethod(), request.getRequestURI(), millis);
		}
	}

}
