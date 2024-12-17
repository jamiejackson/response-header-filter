package pl.hordyjewiczmichal;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

public class ResponseHeaderFilter implements Filter {
    private Map<String, List<String>> headersToSet = new HashMap<>();
    private boolean setHeadersAfterServlet;

    @Override
    public void init(FilterConfig config) {
        this.setHeadersAfterServlet = Boolean.parseBoolean(config.getInitParameter("setHeadersAfterServlet"));

        Enumeration<String> paramNames = config.getInitParameterNames();
        if (paramNames == null) return;

        while (paramNames.hasMoreElements()) {
            String paramName = paramNames.nextElement();
            if ("setHeadersAfterServlet".equals(paramName)) {
                continue; // Skip the setHeadersAfterServlet parameter
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


        response.addHeader("x-response-filter-header-in-doFilter", "true");
        response.addHeader("instance_var_setHeadersAfterServlet", String.valueOf(setHeadersAfterServlet));
        response.addHeader("instance_var_setHeadersAfterServlet_is_true", String.valueOf(setHeadersAfterServlet==true));

        if (!setHeadersAfterServlet) {
            response.addHeader("debug","setHeadersAfterServlet is false");
            setHeadersToSet(response);
            response.addHeader("debug","setHeadersToSet called");
            response.addHeader("x-response-filter-header-set-before-servlet", "true");
            response.addHeader("debug", "x-response-thing-called");
        }

        chain.doFilter(req, resp);

        if (setHeadersAfterServlet) {
            response.addHeader("debug","setHeadersAfterServlet is true");
            setHeadersToSet(response);
            response.addHeader("debug","setHeadersToSet called");
            response.addHeader("x-response-filter-header-set-after-servlet", "true");
            response.addHeader("debug", "x-response-thing-called");
        }
    }

    private void setHeadersToSet(HttpServletResponse response) {
        StringBuilder headersValue = new StringBuilder();
        response.addHeader("debug","in setHeadersToSet");
        headersToSet.forEach((headerName, values) -> {
            values.forEach(value -> {
                response.addHeader("debug","in inner loop");
                response.addHeader(headerName, value);
                headersValue.append(headerName).append("=").append(value).append(", ");
            });
        });
        if (headersValue.length() > 0) {
            headersValue.setLength(headersValue.length() - 2); // Remove the trailing comma and space
        }
        response.addHeader("debug","setting x-filter-headers");
        response.setHeader("x-filter-headers", headersValue.toString());
        response.addHeader("debug","set x-filter-headers");
    }

    @Override
    public void destroy() {
        // nothing to clean up.
    }

    public Map<String, List<String>> getHeadersToSet() {
        return headersToSet;
    }
}