package mockservice.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import mockservice.exception.BaseException;
import mockservice.exception.ErrorResponse;
import mockservice.exception.ExceptionRepository;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class LoginFilter implements Filter {
    private final static List<String> filterPaths = Arrays.asList(
            "", "api", "api/*", "apis", "oauth2/client/register", "oauth2/client", "oauth2/code", "oauth2/client/list", "oauth2/register/client");

    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpSession session = request.getSession();
        if (session.getAttribute("user") != null) {
            chain.doFilter(req, res);
            return;
        }

        String path = request.getServletPath().substring(request.getContextPath().length() + 1);
        for (String item : filterPaths) {
            if (path.equals(item) || item.contains("*") && match(item, path)) {
                HttpServletResponse response = (HttpServletResponse) res;
                if(isAcceptJson(request)) {
                    BaseException exception = ExceptionRepository.UNAUTHORIZED.getException();
                    response.getWriter().println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(
                            ErrorResponse.getErrorResponse(exception.getErrorCode(), exception.getDescription(), exception.getDetail(), null)));
                    response.setStatus(exception.getHttpStatus());
                    response.setContentType("application/json");
                    return;
                }

                String host = request.getRequestURI();
                host = host.substring(0, host.indexOf("/"));
                response.sendRedirect(String.format("%s/%s%s", host, request.getContextPath(), "oauth2/authorize"));
                return;
            }
        }
        chain.doFilter(req, res);
    }

    private boolean isAcceptJson(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        if(accept != null && (accept.contains("application/json"))) {
            return true;
        }

        String contentType = request.getHeader("Content-Type");
        if(contentType != null && (contentType.contains("application/json"))) {
            return true;
        }
        return false;
    }

    private boolean match(String item, String path) {
        if(path.isEmpty()) {
            return false;
        }
        char[] chars = item.toCharArray();
        int index = 0;
        for (int i = 0; i < chars.length;) {
            if(index == path.length()) {
                return false;
            }
            if(chars[i] == path.charAt(index)) {
                ++i;
                ++index;
            } else if(chars[i] == '*') {
                while (index < path.length() && path.charAt(index) != '/') {
                    ++index;
                }
                ++i;
            } else {
                return false;
            }

            if(index == path.length() && i == chars.length) {
                return true;
            }
        }
        return false;
    }
}
