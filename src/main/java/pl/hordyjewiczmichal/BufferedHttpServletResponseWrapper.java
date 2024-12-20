package pl.hordyjewiczmichal;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * A custom HttpServletResponseWrapper that buffers the response content.
 * This allows for inspection and modification of the response headers and content
 * before the response is committed to the client.
 */
public class BufferedHttpServletResponseWrapper extends HttpServletResponseWrapper {
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private final PrintWriter writer = new PrintWriter(buffer);

    /**
     * Constructs a response adaptor wrapping the given response.
     *
     * @param response the original HttpServletResponse to wrap
     */
    public BufferedHttpServletResponseWrapper(HttpServletResponse response) {
        super(response);
    }

    /**
     * Returns a ServletOutputStream suitable for writing binary data in the response.
     *
     * @return a ServletOutputStream for writing binary data
     * @throws IOException if an input or output exception occurred
     */
    @Override
    public ServletOutputStream getOutputStream() {
        return new ServletOutputStream() {
            @Override
            public void write(int b) throws IOException {
                buffer.write(b);
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {
                // No-op implementation
            }
        };
    }

    /**
     * Returns a PrintWriter object that can send character text to the client.
     *
     * @return a PrintWriter object that can return character data to the client
     * @throws IOException if an input or output exception occurred
     */
    @Override
    public PrintWriter getWriter() {
        return writer;
    }

    /**
     * Returns the buffered response content as a byte array.
     *
     * @return the buffered response content
     */
    public byte[] getBuffer() {
        return buffer.toByteArray();
    }

    /**
     * Flushes the buffer content to the wrapped response's output stream.
     *
     * @throws IOException if an input or output exception occurred
     */
    public void flushBuffer() throws IOException {
        writer.flush();
        buffer.writeTo(getResponse().getOutputStream());
    }
}