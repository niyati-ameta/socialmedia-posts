package com.intuit.socialmedia.posts.config;

import com.fasterxml.uuid.Generators;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Data
@Slf4j
@EqualsAndHashCode(callSuper = false)
@Component
public class Slf4jMDCFilter extends OncePerRequestFilter {

	private static final String TRACE_HEADER = "X-Request-Id";
	
	@Value("${requestLogExcludeUrls}")
	private String[] requestLogExcludeUrls;

	@Override
	protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response,
			final FilterChain chain) throws java.io.IOException, ServletException {
		try {
			preHandle(request, response);
			chain.doFilter(request, response);
			afterCompletion(request, response);
		} catch (Exception e) {
			log.error("Request:{} , Message:{}", request.getRequestURI(), e.getMessage());
		} finally {
			MDC.get("requestId");
		}
	}

	public void preHandle(HttpServletRequest request, HttpServletResponse response) {
		String traceId = request.getHeader(TRACE_HEADER);
		if (StringUtils.isBlank(request.getHeader(TRACE_HEADER))) {
			traceId = Generators.timeBasedEpochGenerator().generate().toString();
		}
		MDC.put("requestId", traceId);
		response.addHeader(TRACE_HEADER, traceId);
	}

	public void afterCompletion(HttpServletRequest request, HttpServletResponse response) {
		if (response.getStatus() == HttpStatus.OK.value()) {
			log.debug("Request:{} , Status:{}", request.getRequestURI(), response.getStatus());
		} else {
			log.warn("Request:{} , Status:{}", request.getRequestURI(), response.getStatus());
		}
	}
	
	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
		return StringUtils.startsWithAny(request.getRequestURI(),requestLogExcludeUrls);
	}
}