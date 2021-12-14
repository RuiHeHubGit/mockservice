package mockservice.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import mockservice.domain.User;
import mockservice.exception.ExceptionRepository;

import java.util.Date;

public class JwtUtils {

    public static final String SUBJECT = "onehee";

    public static final long EXPIRE = 1000 * 60 * 60;

    public static final String APPSECRET = "onehee666";

    /**
     * gen jwt
     *
     * @param user
     * @return
     */
    public static String geneJsonWebToken(User user) {

        if (user == null || user.getId() == null || user.getUsername() == null) {
            return null;
        }
        String token = Jwts.builder().setSubject(SUBJECT)
                .claim("id", user.getId())
                .claim("username", user.getUsername())
                .claim("img", user.getHeadImg())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRE))
                .signWith(SignatureAlgorithm.HS256, APPSECRET).compact();

        return token;
    }


    /**
     * check token
     *
     * @param token
     * @return
     */
    public static Claims checkJWT(String token) {
        try {
            final Claims claims = Jwts.parser().setSigningKey(APPSECRET).
                    parseClaimsJws(token).getBody();
            return claims;
        } catch (Exception e) {
            ExceptionRepository.OAUTH_INVALID_TOKEN.getException(e.getMessage());
        }
        return null;

    }
}
