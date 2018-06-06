package com.github.fabriziocucci.spike.sleuth;

import java.io.IOException;
import java.util.Optional;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

@Component
class TenantIdFilter extends GenericFilterBean {

	static final String HEADER_KEY_FOR_TENANT_ID = "X-Tenant-Id";
	
	private final Tracer tracer;
	
	@Autowired
	public TenantIdFilter(Tracer tracer) {
		this.tracer = tracer;
	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
		Optional<String> tenantIdFromBaggage = extractTenantIdFromBaggage();
		if (!tenantIdFromBaggage.isPresent()) {
			String tenantIdFromHeader = extractTenantIdFromHeader(servletRequest);			
			Span currentSpan = this.tracer.getCurrentSpan();
			currentSpan.setBaggageItem(HEADER_KEY_FOR_TENANT_ID, tenantIdFromHeader);
		}
		filterChain.doFilter(servletRequest, servletResponse);
	}
	
	private Optional<String> extractTenantIdFromBaggage() {
		Span currentSpan = this.tracer.getCurrentSpan();
		return Optional.ofNullable(currentSpan.getBaggageItem(HEADER_KEY_FOR_TENANT_ID));
	}

	private static String extractTenantIdFromHeader(ServletRequest servletRequest) {
		HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
		Optional<String> tenantId = Optional.ofNullable(httpServletRequest.getHeader(HEADER_KEY_FOR_TENANT_ID));
		return tenantId.orElseThrow(() -> new IllegalStateException("Unable to find header " + HEADER_KEY_FOR_TENANT_ID));
	}
	
}
