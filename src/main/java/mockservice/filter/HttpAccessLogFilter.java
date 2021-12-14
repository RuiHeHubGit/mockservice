package mockservice.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class HttpAccessLogFilter implements Filter {
    private final static Logger httpAccessLogger = LoggerFactory.getLogger("httpAccessLogger");

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        long startTime = System.currentTimeMillis();
        try {
            chain.doFilter(req, res);
        } finally {
            long costTime = System.currentTimeMillis() - startTime;
            HttpServletRequest request = (HttpServletRequest) req;
            HttpServletResponse response = (HttpServletResponse) res;
            httpAccessLogger.info(String.format("%s %s:%s -> %s %s %dms",
                    request.getMethod(), request.getRemoteHost(), request.getRemotePort(), request.getRequestURL(), HttpStatus.valueOf(response.getStatus()), costTime));
        }
    }
}
