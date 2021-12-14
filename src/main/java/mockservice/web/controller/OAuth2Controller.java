package mockservice.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import mockservice.domain.ClientInfo;
import mockservice.domain.CodeInfo;
import mockservice.domain.TokenInfo;
import mockservice.domain.User;
import mockservice.exception.ExceptionRepository;
import mockservice.service.Oauth2Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.*;

@Controller
@RequestMapping("oauth2")
public class OAuth2Controller {
    private final static Logger logger = LoggerFactory.getLogger(OAuth2Controller.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    Oauth2Service service;

    @GetMapping("client")
    public String client(Model model) {
        model.addAttribute("clients", service.getClients());
        return "client.html";
    }

    @PostMapping("register/client")
    public String registerClient(@RequestParam String clientId,
                                 @RequestParam String clientSecret,
                                 @RequestParam String redirectUri,
                                 @RequestParam String scope,
                                 RedirectAttributes attr) {
        try {
            service.registerClient(clientId, clientSecret, redirectUri, scope);
        } catch (Exception e) {
            attr.addFlashAttribute("error", e);
        }
        return "redirect:/oauth2/client";
    }

    @ResponseBody
    @PostMapping("client/register")
    public ClientInfo registerClient(@RequestBody ClientInfo info) {
        service.registerClient(info);
        return info;
    }

    @ResponseBody
    @GetMapping("client/list")
    public Collection<ClientInfo> clientList() {
        return service.getClients();
    }

    @GetMapping("logout")
    public String logout(HttpServletRequest request) {
        request.getSession(true).invalidate();
        return "redirect:/";
    }

    @GetMapping("authorize")
    public String authorize(@SessionAttribute(value = "user", required = false) User user,
                            @RequestParam(value = "username", required = false) String username,
                            @RequestParam(value = "password", required = false) String password,
                            @RequestParam(value = "response_type", required = false) String responseType,
                            @RequestParam(value = "client_id", required = false) String clientId,
                            @RequestParam(value = "redirect_uri", required = false) String redirectURI,
                            @RequestParam(value = "scope", required = false) String scope,
                            @RequestParam(value = "state", required = false) String state,
                            @RequestParam(value = "from", required = false) String from,
                            @RequestParam Map<String, Object> params,
                            Model model,
                            HttpServletRequest req) {
        return service.authorize(user, username, password, responseType, clientId, redirectURI, scope, state, from, model, req);
    }

    @GetMapping("code")
    public String authCode(@SessionAttribute("user") User user,
                           @SessionAttribute(value = "client_id") String clientId,
                           @SessionAttribute(value = "redirect_uri") String redirectURI,
                           @SessionAttribute(value = "scope") String scope,
                           @SessionAttribute(value = "state", required = false) String state,
                           HttpServletRequest req) {
        HttpSession session = req.getSession(true);
        session.removeAttribute("redirect_uri");
        session.removeAttribute("client_id");
        session.removeAttribute("scope");
        session.removeAttribute("state");

        return service.authCode(user, clientId, redirectURI, scope, state);
    }

    @ResponseBody
    @PostMapping("token")
    public TokenInfo token(@RequestParam Map<String, Object> params,
                           @RequestParam(value = "grant_type", defaultValue = "authorization_code") String grantType,
                           @RequestParam("client_id") String clientId,
                           @RequestParam("client_secret") String clientSecret,
                           @RequestParam("redirect_uri") String redirectURI,
                           @RequestParam(value = "code", required = false) String code,
                           @RequestParam(value = "refresh_token", required = false) String refreshToken) {

        try {
            logger.info(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(params));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return service.token(grantType, clientId, clientSecret, redirectURI, code, refreshToken);
    }

    @ResponseBody
    @GetMapping("check")
    public TokenInfo check(@RequestParam("token") String token) {
        return service.checkToken(token);
    }
}
