package com.github.fabriziocucci.spike.sleuth;

import org.jboss.logging.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.log.SleuthSlf4jProperties;
import org.springframework.cloud.sleuth.log.Slf4jSpanLogger;
import org.springframework.stereotype.Component;

@Component
class CustomSlf4jSpanLogger extends Slf4jSpanLogger {

	@Autowired
	public CustomSlf4jSpanLogger(SleuthSlf4jProperties sleuthSlf4jProperties) {
		super(sleuthSlf4jProperties.getNameSkipPattern());
	}
	
	@Override
	public void logStartedSpan(Span parent, Span span) {
		MDC.put(TenantIdFilter.HEADER_KEY_FOR_TENANT_ID, span.getBaggageItem(TenantIdFilter.HEADER_KEY_FOR_TENANT_ID));
		super.logStartedSpan(parent, span);
	}
	
	@Override
	public void logContinuedSpan(Span span) {
		MDC.put(TenantIdFilter.HEADER_KEY_FOR_TENANT_ID, span.getBaggageItem(TenantIdFilter.HEADER_KEY_FOR_TENANT_ID));
		super.logContinuedSpan(span);
	}
	
	@Override
	public void logStoppedSpan(Span parent, Span span) {
		super.logStoppedSpan(parent, span);
		if (span == null || parent == null) {
			MDC.remove(TenantIdFilter.HEADER_KEY_FOR_TENANT_ID);
		}
	}
	
}
