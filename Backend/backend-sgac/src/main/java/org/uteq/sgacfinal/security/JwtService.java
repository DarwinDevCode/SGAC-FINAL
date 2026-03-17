package org.uteq.sgacfinal.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    private static final String CLAIM_TYPE         = "type";
    private static final String CLAIM_ROL          = "rol";
    private static final String CLAIM_ROLES        = "roles";
    private static final String CLAIM_NOMBRES      = "nombres";
    private static final String CLAIM_APELLIDOS    = "apellidos";
    private static final String CLAIM_CORREO       = "correo";
    private static final String CLAIM_ID_USUARIO   = "idUsuario";

    private static final String TYPE_PRE_AUTH = "PRE_AUTH";
    private static final String TYPE_FINAL    = "FINAL";

    private static final long PRE_AUTH_EXPIRY_MS = 1000L * 60 * 5;
    private static final long FINAL_EXPIRY_MS    = 1000L * 60 * 60 * 10;

    @Value("${jwt.secret:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}")
    private String secretKey;

    public String generatePreAuthToken(String username, Integer idUsuario,
                                       String nombres, String apellidos,
                                       String correo, String rolesCsv) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_TYPE,       TYPE_PRE_AUTH);
        claims.put(CLAIM_ROLES,      rolesCsv);      // ej: "ESTUDIANTE,COORDINADOR"
        claims.put(CLAIM_NOMBRES,    nombres);
        claims.put(CLAIM_APELLIDOS,  apellidos);
        claims.put(CLAIM_CORREO,     correo);
        claims.put(CLAIM_ID_USUARIO, idUsuario);

        return buildToken(claims, username, PRE_AUTH_EXPIRY_MS);
    }

    public String generateToken(String username, String rolActual) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_ROL,  rolActual);
        claims.put(CLAIM_TYPE, TYPE_FINAL);
        return buildToken(claims, username, FINAL_EXPIRY_MS);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    public boolean isPreAuthTokenValid(String token) {
        try {
            Claims claims = extractAllClaims(token);
            // Si llegamos aquí la firma es correcta y el token no expiró
            // (extractAllClaims lanza ExpiredJwtException si está expirado)
            return TYPE_PRE_AUTH.equals(claims.get(CLAIM_TYPE, String.class));
        } catch (Exception e) {
            return false;
        }
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(extractAllClaims(token));
    }

    public String extractRolesCsv(String token) {
        return extractClaim(token, c -> c.get(CLAIM_ROLES, String.class));
    }

    public String extractNombres(String token) {
        return extractClaim(token, c -> c.get(CLAIM_NOMBRES, String.class));
    }

    public String extractApellidos(String token) {
        return extractClaim(token, c -> c.get(CLAIM_APELLIDOS, String.class));
    }

    public String extractCorreo(String token) {
        return extractClaim(token, c -> c.get(CLAIM_CORREO, String.class));
    }

    public Integer extractIdUsuario(String token) {
        return extractClaim(token, c -> c.get(CLAIM_ID_USUARIO, Integer.class));
    }

    private String buildToken(Map<String, Object> claims, String subject, long expiryMs) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiryMs))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}