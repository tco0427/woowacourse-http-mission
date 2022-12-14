package org.apache.coyote.request.startline;

import org.apache.coyote.request.query.QueryParams;

public class StartLine {

    private static final int START_LINE_ELEMENT_SIZE = 3;
    private static final String ROOT = "/";
    private static final int URI_INDEX = 1;
    private static final int METHOD_INDEX = 0;
    private static final int PATH_INDEX = 0;
    private static final int QUERY_INDEX = 1;
    private static final String START_LINE_DELIMITER = " ";
    private static final String QUERY_DELIMITER = "\\?";
    private static final int NO_QUERY_LENGTH = 1;

    private final HttpMethod method;
    private final String requestPath;
    private final QueryParams queryParams;

    private StartLine(final HttpMethod method, final String requestPath, final QueryParams queryParams) {
        this.method = method;
        this.requestPath = requestPath;
        this.queryParams = queryParams;
    }

    public static StartLine from(String startLine) {
        final String[] startLineElements = splitStartLine(startLine);

        final HttpMethod httpMethod = HttpMethod.from(startLineElements[METHOD_INDEX]);

        final String requestUri = startLineElements[URI_INDEX];
        final String[] requestUriElements = requestUri.split(QUERY_DELIMITER);

        final String requestPath = requestUriElements[PATH_INDEX];
        final QueryParams queryParams = extractQueryParams(requestUriElements);

        return new StartLine(httpMethod, requestPath, queryParams);
    }

    private static QueryParams extractQueryParams(final String[] requestUriElements) {
        if (requestUriElements.length == NO_QUERY_LENGTH) {
            return QueryParams.empty();
        }
        return QueryParams.from(requestUriElements[QUERY_INDEX]);
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getRequestPath() {
        return requestPath;
    }

    public boolean isSameMethod(HttpMethod httpMethod) {
        return this.method.equals(httpMethod);
    }

    private static String[] splitStartLine(String startLine) {
        final String[] startLineElements = startLine.split(START_LINE_DELIMITER);

        checkStartLineSize(startLineElements);
        checkRequestUri(startLineElements[URI_INDEX]);

        return startLineElements;
    }

    private static void checkStartLineSize(String[] startLineElements) {
        if (startLineElements.length != START_LINE_ELEMENT_SIZE) {
            throw new IllegalArgumentException("????????? HTTP ???????????????.");
        }
    }

    private static void checkRequestUri(String requestUri) {
        if (!requestUri.startsWith(ROOT)) {
            throw new IllegalArgumentException("????????? ?????? ???????????????.");
        }
    }

    public QueryParams getQueryParams() {
        return queryParams;
    }
}
