package mockservice.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import mockservice.domain.ClientInfo;
import mockservice.domain.CodeInfo;
import mockservice.domain.TokenInfo;
import mockservice.domain.User;
import mockservice.exception.ExceptionRepository;
import mockservice.service.Oauth2Service;
import mockservice.utils.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class Oauth2ServiceImpl implements Oauth2Service {
    private final static Logger logger = LoggerFactory.getLogger(Oauth2ServiceImpl.class);
    private static final Map<String, CodeInfo> codeInfoMap = new HashMap<>();
    private static final Map<String, User> onlineUsers = new ConcurrentHashMap<>();
    private static final Map<String, Long> refreshTokenMap = new ConcurrentHashMap<>();
    private static final Map<String, TokenInfo> tokenMap = new ConcurrentHashMap<>();
    private static final Map<String, ClientInfo> clientMap = new ConcurrentHashMap<>();
    private static final Map<String, User> userMap = new ConcurrentHashMap<>();
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Long CODE_EXPIRE_IN = 1000L * 60 * 5;
    private static final long TOKEN_EXPIRE_IN_SECOND = 3600;

    static {
        clientMap.put("test", new ClientInfo("test", "ab123456", "https://www.baidu.com", "*"));
        userMap.put("admin", new User(1000001L, "admin", "321123"));
    }

    @Override
    public Collection<ClientInfo> getClients() {
        return clientMap.values();
    }

    @Override
    public void registerClient(String clientId, String clientSecret, String redirectUri, String scope) {
        verifyClient(clientId, clientSecret, redirectUri, scope);
        clientMap.put(clientId, new ClientInfo(clientId, clientSecret, redirectUri, scope));
    }

    @Override
    public void registerClient(ClientInfo info) {
        registerClient(info.getClientId(), info.getClientSecret(), info.getRedirectUri(), info.getScope());
    }

    @Override
    public String authorize(User user, String username, String password, String responseType, String clientId, String redirectURI, String scope, String state, String from, Model model, HttpServletRequest req) {
        HttpSession session = req.getSession(true);
        if (user == null && username != null) {
            user = userMap.get(username);
            if (user == null || !user.getPassword().equals(password)) {
                model.addAttribute("error", ExceptionRepository.LOGIN_FAILED.getException());
            }
            session.setAttribute("user", user);
            if (redirectURI == null && session.getAttribute("redirect_uri") == null) {
                return "redirect:/";
            }
        }

        if (clientId != null && redirectURI != null && scope != null) {
            if (!"code".endsWith(responseType)) {
                throw ExceptionRepository.OAUTH_NOT_SUPPORT_AUTH_TYPE.getException(responseType);
            }

            checkClient(clientId, null, redirectURI, scope, false);
            session.setAttribute("redirect_uri", redirectURI);
            session.setAttribute("client_id", clientId);
            session.setAttribute("scope", scope);
            session.setAttribute("state", state);
        } else if(from != null) {
            return "redirect:" + from;
        }
        return "authorize.html";
    }

    @Override
    public String authCode(User user, String clientId, String redirectURI, String scope, String state) {
        String code = UUID.randomUUID().toString().replace("-", "");
        codeInfoMap.put(code,
                new CodeInfo(code, clientId, redirectURI, scope, state, user.getUsername(), System.currentTimeMillis()));

        redirectURI = String.format("redirect:%s?code=%s", redirectURI, code);
        if (state != null && !state.isEmpty()) {
            redirectURI = String.format("%s&state=%s", redirectURI, state);
        }
        logger.info("{}", redirectURI);
        return redirectURI;
    }

    @Override
    public TokenInfo token(String grantType, String clientId, String clientSecret, String redirectURI, String code, String refreshToken) {
        ClientInfo clientInfo = checkClient(clientId, clientSecret, redirectURI, null, true);

        User user;
        if ("refresh_token".equals(grantType)) {
            checkRefreshToken(refreshToken);
            refreshTokenMap.remove(refreshToken);
            user = onlineUsers.remove(refreshToken);
        } else if ("authorization_code".endsWith(grantType)) {
            checkCode(code, clientId, redirectURI);
            CodeInfo codeInfo = codeInfoMap.remove(code);
            user = userMap.get(codeInfo.getUsername());
        } else {
            throw ExceptionRepository.OAUTH_NOT_SUPPORT_AUTH_TYPE.getException(grantType);
        }

        String accessToken = JwtUtils.geneJsonWebToken(user);
        refreshToken = UUID.randomUUID().toString().replace("-", "");

        logger.info("uid:{},access_token:{},refresh_token:{}", user.getId(), accessToken, refreshToken);

        TokenInfo tokenInfo = new TokenInfo(accessToken, refreshToken, "bearer", clientInfo.getScope(), TOKEN_EXPIRE_IN_SECOND, user.getId(), Collections.EMPTY_MAP);
        tokenMap.put(accessToken, tokenInfo);
        onlineUsers.put(accessToken, user);
        refreshTokenMap.put(refreshToken, System.currentTimeMillis() + (long) (24 * 3600));

        return tokenInfo;
    }

    @Override
    public TokenInfo checkToken(String token) {
        TokenInfo tokenInfo;
        if (token == null || (tokenInfo = tokenMap.get(token)) == null) {
            throw ExceptionRepository.OAUTH_INVALID_TOKEN.getException();
        }

        long expire = tokenInfo.getExpires_in() * 1000 + tokenInfo.getCreateTime();
        if (expire < System.currentTimeMillis()) {
            tokenMap.remove(token);
            throw ExceptionRepository.OAUTH_TOKEN_EXPIRED.getException();
        }
        return tokenInfo;
    }

    private void verifyClient(String clientId, String clientSecret, String redirectURI, String scope) {
        if (isNullOrEmpty(clientId)) {
            throw ExceptionRepository.OAUTH_INVALID_CLIENT_ID.getException();
        }

        if (isNullOrEmpty(clientSecret)) {
            throw ExceptionRepository.OAUTH_INVALID_CLIENT_SECRET.getException();
        }

        if (isNullOrEmpty(redirectURI)) {
            throw ExceptionRepository.OAUTH_INVALID_REDIRECT_URI.getException();
        }

        if (isNullOrEmpty(scope)) {
            throw ExceptionRepository.OAUTH_INVALID_SCOPE.getException();
        }
    }

    private boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    private void checkRefreshToken(String refreshToken) {
        Long expires;
        if (refreshToken == null || (expires = refreshTokenMap.get(refreshToken)) == null && onlineUsers.get(refreshToken) == null) {
            throw ExceptionRepository.OAUTH_INVALID_REFRESH_TOKEN.getException(refreshToken);
        }

        if (expires < System.currentTimeMillis()) {
            throw ExceptionRepository.OAUTH_REFRESH_TOKEN_EXPIRED.getException(refreshToken);
        }
    }

    private ClientInfo checkClient(String clientId, String clientSecret, String redirectURL, String scope, boolean hasSecret) {
        ClientInfo clientInfo;
        if (clientId == null || (clientInfo = clientMap.get(clientId)) == null) {
            throw ExceptionRepository.OAUTH_INVALID_CLIENT_ID.getException();
        }

        if (hasSecret && !Objects.equals(clientInfo.getClientSecret(), clientSecret)) {
            throw ExceptionRepository.OAUTH_INVALID_CLIENT_SECRET.getException();
        }

        if (!Objects.equals(clientInfo.getRedirectUri(), redirectURL)) {
            throw ExceptionRepository.OAUTH_INVALID_REDIRECT_URI.getException("invalid redirect_uri");
        }

        if (!hasSecret && !"*".equals(clientInfo.getScope()) && !Objects.equals(clientInfo.getScope(), scope)) {
            throw ExceptionRepository.OAUTH_INVALID_SCOPE.getException(scope);
        }

        return clientInfo;
    }

    private void checkCode(String code, String clientId, String redirectURI) {
        CodeInfo codeInfo;
        if (code == null || (codeInfo = codeInfoMap.get(code)) == null) {
            throw ExceptionRepository.OAUTH_INVALID_CODE.getException();
        }

        if (!Objects.equals(codeInfo.getClientId(), clientId)) {
            throw ExceptionRepository.OAUTH_INVALID_CLIENT_ID.getException();
        }

        if (!Objects.equals(codeInfo.getRedirectURI(), redirectURI)) {
            throw ExceptionRepository.OAUTH_INVALID_REDIRECT_URI.getException();
        }

        long expire = codeInfo.getCreateTime() + CODE_EXPIRE_IN;
        if(expire < System.currentTimeMillis()) {
            throw ExceptionRepository.OAUTH_CODE_EXPIRED.getException();
        }
    }
}
