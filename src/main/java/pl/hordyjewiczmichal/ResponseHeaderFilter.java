package pl.hordyjewiczmichal;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ResponseHeaderFilter implements Filter {
    private static final Logger logger = Logger.getLogger(ResponseHeaderFilter.class.getName());

    private Map<String, List<String>> headersToSet = new HashMap<>();
    private boolean setHeadersAfterServlet = false;
    private boolean appendValues = false;

    @Override
    public void init(FilterConfig config) {
        logger.setLevel(Level.FINE);

        Enumeration<String> paramNames = config.getInitParameterNames();
        if (paramNames == null) return;

        this.setHeadersAfterServlet = Boolean.parseBoolean(config.getInitParameter("setHeadersAfterServlet"));
        this.appendValues = Boolean.parseBoolean(config.getInitParameter("appendValues"));

        while (paramNames.hasMoreElements()) {
            String paramName = paramNames.nextElement();
            if ("setHeadersAfterServlet".equals(paramName) || "appendValues".equals(paramName)) {
                continue; // skip these metadata parameters
            }
            String paramValue = config.getInitParameter(paramName);
            if (paramValue != null && !paramValue.trim().isEmpty()) {
                headersToSet.put(paramName, Arrays.asList(paramValue.split("\n")));
            }
        }
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws ServletException, IOException {
        HttpServletResponse response = (HttpServletResponse) resp;


        response.setHeader("foo", "bar");

        if (!setHeadersAfterServlet) {
            logger.log(Level.FINE, "setHeadersAfterServlet=false");
            logResponseHeaders(response, "Headers before filter sets headers:");
            setHeadersToSet(response);
            logResponseHeaders(response, "Headers after filter set headers:");
        }

        logResponseHeaders(response, "Headers before servlet is called:");
        chain.doFilter(req, resp);
        logger.log(Level.FINE, "doFilter called");
        logResponseHeaders(response, "Headers after servlet was called:");

        if (setHeadersAfterServlet) {
            logger.log(Level.FINE, "setHeadersAfterServlet=true");
            logResponseHeaders(response, "Headers before filter sets headers:");
            setHeadersToSet(response);
            logResponseHeaders(response, "Headers after filter set headers:");
        }
    }

    private void setHeader(HttpServletResponse response, String name, String value, boolean append) {
        logger.log(Level.FINE, "In setHeader. name=" + name + "; value=" + value + "; append=" + append);
        if (append && response.containsHeader(name)) {
            String existingValue = response.getHeader(name);
            String newValue = String.join(", ", existingValue, value);
            logger.log(Level.FINE, "appending to existing header: " + name + ": " + newValue);
            response.setHeader(name, newValue);
        } else {
            logger.log(Level.FINE, "adding header: " + name + ": " + value);
            response.addHeader(name, value);
        }
    }

    private void setHeadersToSet(HttpServletResponse response) {
        headersToSet.forEach((headerName, values) -> {
            logger.log(Level.FINE, "in setHeadersToSet. working on: " + headerName + ": " + values);
            values.forEach(value -> setHeader(response, headerName, value, appendValues));
        });
    }

    @Override
    public void destroy() {
        // nothing to clean up.
    }

    public Map<String, List<String>> getHeadersToSet() {
        return headersToSet;
    }

    private void logResponseHeaders(HttpServletResponse response, String message) {
        logger.log(Level.FINE, message);
        Collection<String> headerNames = response.getHeaderNames();
        for (String headerName : headerNames) {
            String headerValue = response.getHeader(headerName);
            logger.log(Level.FINE, "Header: " + headerName + " = " + headerValue);
        }
    }

    // helper for step debugging
    public Map<String, String> getResponseHeaders(HttpServletResponse response) {
        Map<String, String> headers = new HashMap<>();
        Collection<String> headerNames = response.getHeaderNames();
        for (String headerName : headerNames) {
            headers.put(headerName, response.getHeader(headerName));
        }
        return headers;
    }

    public int getResponseIdentityHashCode(HttpServletResponse response) {
        return System.identityHashCode(response);
    }
}