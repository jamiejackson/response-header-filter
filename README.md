# Response Header Filter for Tomcat
Add custom headers easily to your Tomcat response using param-name and param-value tags in web.xml

## Installation
* Download latest release (.jar file)
* Put `response-header-filter.jar` in your `$CATALINA_HOME/lib` directory
* Edit your `web.xml` located in `$CATALINA_HOME/conf`
* Add the following filter and filter mapping inside `<web-app>` tag:
    ```xml
    <filter>
        <filter-name>ResponseHeaderFilter</filter-name>
        <filter-class>pl.hordyjewiczmichal.ResponseHeaderFilter</filter-class>
        <init-param>
            <param-name>setHeadersAfterServlet</param-name> <!-- optional; defaults to false (set headers before servlet) -->
            <param-value>true</param-value>
        </init-param>
        <init-param>
            <param-name>Your-Header-1</param-name> <!-- put your any header name -->
            <param-value>
                your value1
                your value2
                your value3
            </param-value> <!-- put your header value(s) separated by a new line  -->
        </init-param>
        <init-param>
            <param-name>Your-Header-2</param-name>
            <param-value>some other value</param-value>
        </init-param>
    </filter>
    <filter-mapping>
        <filter-name>ResponseHeaderFilter</filter-name>
        <url-pattern>/*</url-pattern> <!-- choose where header will be added -->
    </filter-mapping>
    ```
In `<init-param>` use `<param-name>` tag to add your custom header and below use `<param-value>` to add value to your header.
You can use multiple values for your header in `<param-value>` - just separate them by a new line.

The `setHeadersAfterServlet` parameter controls whether headers are set before or after the Java application processes the request.
 * If run before (default), then the application can clobber a header set by the filter.
 * If run after, `setHeadersAfterServlet=true`, then the application will append any values to a header that was already written by the application.