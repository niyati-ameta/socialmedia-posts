package com.intuit.socialmedia.posts.auth;

import com.intuit.socialmedia.posts.dto.request.UserLoginRequest;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtil {

    @Value("${jwtExpirationTimeInMillis}")
    private Long expirationTime;

    @Value("${publicKey}")
    private String publicKey;

    @Value("${privateKey}")
    private String privateKey;

    // Method to generate JWT Token using RSA Private Key
    public String generateToken(UserLoginRequest user) throws Exception {

        Map<String, Object> claimMap = Map.of("email", user.getEmail());
        Claims claims = Jwts.claims().add(claimMap).build();
        return Jwts.builder()
                .subject(user.getEmail())
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(SignatureAlgorithm.RS256, privateKey())
                .compact();
    }

    // Method to validate and parse token
    public Claims validateToken(String token, PublicKey publicKey) {
        return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public PrivateKey privateKey() throws Exception {
        String privateKeyPEM = privateKey;
        System.out.println(privateKeyPEM);
        return getPrivateKey(privateKeyPEM);
    }

    public PublicKey publicKey() throws Exception {
        String publicKeyPEM = publicKey;
        System.out.println(publicKeyPEM);
        return getPublicKey(publicKeyPEM);
    }

    // Method to convert String PEM to PrivateKey
    public PrivateKey getPrivateKey(String privateKeyPEM) throws NoSuchAlgorithmException, InvalidKeySpecException {
        String privateKeyFormatted = privateKeyPEM
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        byte[] decodedKey = Base64.getDecoder().decode(privateKeyFormatted);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decodedKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }

    // Method to convert String PEM to PublicKey
    public PublicKey getPublicKey(String publicKeyPEM) throws Exception {
        String publicKeyFormatted = publicKeyPEM
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        byte[] decodedKey = Base64.getDecoder().decode(publicKeyFormatted);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }
}
