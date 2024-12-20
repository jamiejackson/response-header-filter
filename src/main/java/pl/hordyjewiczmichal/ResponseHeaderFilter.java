package pl.hordyjewiczmichal;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

public class ResponseHeaderFilter implements Filter {
    private Map<String, List<String>> headersToSet = new HashMap<>();
    private boolean setHeadersAfterServlet = false;
    private boolean appendValues = false;

    @Override
    public void init(FilterConfig config) {
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
        // use a wrapper to buffer the response content; otherwise the response will be committed when chain.doFilter
        //  is called
        BufferedHttpServletResponseWrapper response = new BufferedHttpServletResponseWrapper((HttpServletResponse) resp);

        response.setHeader("foo", "bar");

        if (!setHeadersAfterServlet) {
            setHeadersToSet(response);
        }

        chain.doFilter(req, response);

        response.setHeader("baz", "qux");
        if (setHeadersAfterServlet) {
            setHeadersToSet(response);
        }

        // commit the response
        response.flushBuffer();
    }

    private void setHeader(HttpServletResponse response, String name, String value, boolean append) {
        if (append && response.containsHeader(name)) {
            String existingValue = response.getHeader(name);
            String newValue = String.join(", ", existingValue, value);
            response.setHeader(name, newValue);
        } else {
            response.addHeader(name, value);
        }
    }

    private void setHeadersToSet(HttpServletResponse response) {
        headersToSet.forEach((headerName, values) -> {
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

    // helper for step debugging
    public Map<String, String> getResponseHeaders(HttpServletResponse response) {
        Map<String, String> headers = new HashMap<>();
        Collection<String> headerNames = response.getHeaderNames();
        for (String headerName : headerNames) {
            headers.put(headerName, response.getHeader(headerName));
        }
        return headers;
    }
}