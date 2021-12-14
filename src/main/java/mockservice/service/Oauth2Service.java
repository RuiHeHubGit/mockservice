package mockservice.service;

import mockservice.domain.ClientInfo;
import mockservice.domain.TokenInfo;
import mockservice.domain.User;
import org.springframework.ui.Model;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;

public interface Oauth2Service {
    Collection<ClientInfo> getClients();

    void registerClient(String clientId, String clientSecret, String redirectUri, String scope);

    void registerClient(ClientInfo info);

    String authorize(User user, String username, String password, String responseType, String clientId,
                     String redirectURI, String scope, String state, String from, Model model, HttpServletRequest req);

    String authCode(User user, String clientId, String redirectURI, String scope, String state);

    TokenInfo token(String grantType, String clientId, String clientSecret, String redirectURI, String code, String refreshToken);

    TokenInfo checkToken(String token);

}
