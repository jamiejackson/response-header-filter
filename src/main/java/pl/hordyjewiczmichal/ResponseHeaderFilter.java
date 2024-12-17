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
            response.addHeader("x-response-filter-header-set-before-servlet", "true");
            setHeadersToSet(response);
        }

        chain.doFilter(req, resp);

        if (setHeadersAfterServlet) {
            response.addHeader("x-response-filter-header-set-after-servlet", "true");
            setHeadersToSet(response);
        }
    }

    private void setHeadersToSet(HttpServletResponse response) {
        headersToSet.forEach((headerName, values) -> {
            values.forEach(value -> response.addHeader(headerName, value));
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