package com.domus.server.common.web;

public final class RequestCorrelation {

    public static final String HEADER_NAME = "X-Correlation-Id";
    public static final String MDC_KEY = "correlationId";
    public static final String REQUEST_ATTRIBUTE = RequestCorrelation.class.getName() + ".id";

    private RequestCorrelation() {
    }
}
