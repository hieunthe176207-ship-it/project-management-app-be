package com.fpt.project.util;

import com.fpt.project.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class SecurityUtil {

    public static final MacAlgorithm JWT_ALGORITHM = MacAlgorithm.HS512;

    private final JwtEncoder jwtEncoder;

    @Value("${token-time:3600}")
    private long tokenExpireTime;

    public String createToken(User user) {
        Instant now = Instant.now();
        Instant validity = now.plus(tokenExpireTime, ChronoUnit.SECONDS);

        // User chỉ có 1 role
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuedAt(now)
                .expiresAt(validity)
                .subject(user.getEmail())
                .build();

        JwsHeader jwsHeader = JwsHeader.with(JWT_ALGORITHM).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
    }

    public String getEmailRequest() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
