package edu.clubhouseapi.jwt;

import edu.clubhouseapi.dto.SecurityUserJwtTokenBody;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.jackson.io.JacksonDeserializer;
import io.jsonwebtoken.lang.Maps;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.List;

@Service
public class JwtClientUtil {

    private JwtParser jwtParser;

    @Value("${jwt.publicKey}")
    public void setPublicKey(String publicKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        final PublicKey publicKeyObject =
                KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(Decoders.BASE64.decode(publicKey)));
        this.jwtParser = Jwts.parserBuilder()
                .setSigningKey(publicKeyObject)
                .deserializeJsonWith(new JacksonDeserializer(Maps.of("claims", SecurityUserJwtTokenBody.class).build()))
                .build();
    }

    public SecurityUserJwtTokenBody parseJwtToken(String token) {
        Claims claims = this.jwtParser.parseClaimsJws(token).getBody();
        return claims.get("claims", SecurityUserJwtTokenBody.class);
    }

    public SecurityUserJwtTokenBody extractFromClaims(Claims claims) {
        return claims.get("claims", SecurityUserJwtTokenBody.class);
    }

    public Jws<Claims> validateToken(String authToken) {
        try {
            Jws<Claims> claims = this.jwtParser.parseClaimsJws(authToken);
            return claims;
        } catch (SecurityException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException ex) {
            throw new BadCredentialsException("INVALID_CREDENTIALS", ex);
        } catch (ExpiredJwtException ex) {
            throw ex;
        }
    }

    public String extractJwtFromRequest(ServerHttpRequest request) {
        final List<String> authorization = request.getHeaders().get("Authorization");
        if (authorization == null || authorization.isEmpty()) {
            return null;
        }
        String bearerToken = authorization.get(0);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    public String formBearerHeader(String token) {
        return "Bearer " + token;
    }

}