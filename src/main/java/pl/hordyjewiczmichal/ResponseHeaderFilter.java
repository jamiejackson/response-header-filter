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
        HttpServletResponse response = (HttpServletResponse) resp;

        if (!setHeadersAfterServlet) {
            setHeadersToSet(response);
        }

        chain.doFilter(req, resp);

        if (setHeadersAfterServlet) {
            setHeadersToSet(response);
        }
    }

    private void setHeadersToSet(HttpServletResponse response) {
        headersToSet.forEach((headerName, values) -> {
            if (appendValues && response.containsHeader(headerName)) {
                String existingValue = response.getHeader(headerName);
                String newValue = String.join(", ", existingValue, String.join(", ", values));
                response.setHeader(headerName, newValue);
            } else {
                values.forEach(value -> response.addHeader(headerName, value));
            }
        });
    }

    @Override
    public void destroy() {
        // nothing to clean up.
    }

    public Map<String, List<String>> getHeadersToSet() {
        return headersToSet;
    }
}