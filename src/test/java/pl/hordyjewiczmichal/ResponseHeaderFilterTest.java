package pl.hordyjewiczmichal;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class ResponseHeaderFilterTest
{
    @Mock
    private HttpServletRequest req;

    @Mock
    private FilterChain chain;

    @Mock
    private FilterConfig config;

    private ResponseHeaderFilter filter;


    @Before
    public void setUp() {
        filter = new ResponseHeaderFilter();
    }

    private void assertHeaderValues(MockHttpServletResponse response, String headerName, String expectedValues) {
        assertEquals(expectedValues, response.getHeaderValues(headerName).stream().map(Object::toString).collect(Collectors.joining(", ")));
    }

    private void mockInitParameter(FilterConfig config, String parameterName, String returnValue) {
        Mockito.when(config.getInitParameter(parameterName)).thenReturn(returnValue);
    }

     @Test
     public void initTest() {
        // given
        Map<String, List<String>> expectedInitParamValues = new HashMap<>();
        expectedInitParamValues.put("x-header-1", Arrays.asList("value1", "value2"));
        expectedInitParamValues.put("x-header-2", Arrays.asList("value3"));
        expectedInitParamValues.put("x-header-4", Arrays.asList("value4", "value5"));
        expectedInitParamValues.put("x-header-5", Arrays.asList("extra-value"));

        // when
        Map<String, String> initParams = Map.of(
            "x-header-1", "value1\nvalue2",
            "x-header-2", "value3",
            "x-header-3", "",
            "x-header-4", "value4\nvalue5",
            "x-header-5", "extra-value"
        );
        initParams.forEach((key, value) -> mockInitParameter(config, key, value));

        Mockito.when(config.getInitParameterNames()).thenReturn(Collections.enumeration(initParams.keySet()));

        filter.init(config);

        // then
        assertEquals(expectedInitParamValues, filter.getHeaders());
    }

    @Test
    public void doFilterTest() throws ServletException, IOException {
        // given
        initTest();
        MockHttpServletResponse resp = new MockHttpServletResponse();

        // Test appending a value to an existing header
        resp.setHeader("x-header-5", "existing-value");

        // when
        filter.doFilter(req, resp, chain);

        // then
        assertHeaderValues(resp, "x-header-1", "value1, value2");
        assertHeaderValues(resp, "x-header-2", "value3");
        assertHeaderValues(resp, "x-header-4", "value4, value5");
        assertHeaderValues(resp, "x-header-5", "existing-value, extra-value");
    }

    @Test
    public void destroyTest() {
        filter.destroy();
        assertTrue(true);
    }
}