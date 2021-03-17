package edu.clubhouseapi.jwt;

import edu.clubhouseapi.dto.ClubHouseAuthResult;
import edu.clubhouseapi.dto.SecurityUserJwtTokenBody;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

@Service
public class JwtServerUtil {

    private PrivateKey privateKey;

    @Autowired
    private JwtClientUtil jwtClientUtil;

    private int jwtExpirationInMs;

    private int refreshExpirationDateInMs;

    @Value("${jwt.privateKey}")
    public void setPrivateKey(String privateKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        this.privateKey = KeyFactory.getInstance("RSA")
                .generatePrivate(new PKCS8EncodedKeySpec(Decoders.BASE64.decode(privateKey)));
    }

    @Value("${jwt.expirationDateInMs}")
    public void setJwtExpirationInMs(int jwtExpirationInMs) {
        this.jwtExpirationInMs = jwtExpirationInMs;
    }

    @Value("${jwt.refreshExpirationDateInMs}")
    public void setRefreshExpirationDateInMs(int refreshExpirationDateInMs) {
        this.refreshExpirationDateInMs = refreshExpirationDateInMs;
    }

    public String generateToken(ClubHouseAuthResult clubHouseAuthResult, List<String> stringRoles) {
        SecurityUserJwtTokenBody tokenBody = SecurityUserJwtTokenBody.builder()
                .userId(clubHouseAuthResult.getUserId())
                .username(clubHouseAuthResult.getUsername())
                .name(clubHouseAuthResult.getName())
                .photoUrl(clubHouseAuthResult.getPhotoUrl())
                .userDevice(clubHouseAuthResult.getUserDevice())
                .userCookie(clubHouseAuthResult.getUserCookie())
                .userToken(clubHouseAuthResult.getUserToken())
                .userRefreshToken(clubHouseAuthResult.getUserRefreshToken())
                .roles(stringRoles)
                .build();

        return doGenerateToken(tokenBody, clubHouseAuthResult.getUserId());
    }

    public String generateToken(SecurityUserJwtTokenBody tokenBody) {
        return doGenerateToken(tokenBody, tokenBody.getUserId());
    }

    private String doGenerateToken(SecurityUserJwtTokenBody userJwtTokenBody, String subject) {
        Map<String, SecurityUserJwtTokenBody> claims = new HashMap<>();
        claims.put("claims", userJwtTokenBody);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationInMs))
                .signWith(privateKey)
                .compact();

    }

    public SecurityUserJwtTokenBody parseJwtToken(String token) {
        return jwtClientUtil.parseJwtToken(token);
    }

    public Mono<String> generateRefreshToken(String tokenParam,
            final @NotNull String newClubHouseAccessToken,
            final @NotNull String newClubHouseRefreshToken) {
        Mono<Claims> claims = Mono.just(tokenParam).map((jwtToken) -> {
            Claims claimsJws;
            try {
                if (!StringUtils.hasText(jwtToken)) {
                    throw new BadCredentialsException("JWT token is not valid");
                }
                claimsJws = validateToken(jwtToken).getBody();
            } catch (BadCredentialsException e) {
                throw new BadCredentialsException("JWT token is not valid", e);
            } catch (ExpiredJwtException e) {
                // refresh it
                claimsJws = e.getClaims();
            }
            return claimsJws;
        }).map(claimsOld -> {
            SecurityUserJwtTokenBody bodyNew = claimsOld.get("claims", SecurityUserJwtTokenBody.class);
            bodyNew = bodyNew.toBuilder()
                    .userToken(newClubHouseAccessToken)
                    .userRefreshToken(newClubHouseRefreshToken)
                    .build();
            claimsOld.put("claims", bodyNew);
            return claimsOld;
        });
        return claims.map(x -> doGenerateRefreshToken(x, x.get("sub").toString()));
    }

    private String doGenerateRefreshToken(Claims claims, String subject) {

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpirationDateInMs))
                .signWith(privateKey)
                .compact();

    }

    public Jws<Claims> validateToken(String authToken) {
        return jwtClientUtil.validateToken(authToken);
    }

}