package com.fpt.project.util;

import com.fpt.project.entity.User;
import com.fpt.project.exception.ApiException;
import com.fpt.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
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

    private static UserRepository userRepository;

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        SecurityUtil.userRepository = userRepository;
    }


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
                .claim("id", user.getId())
                .build();

        JwsHeader jwsHeader = JwsHeader.with(JWT_ALGORITHM).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
    }

    public String getEmailRequest() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

//    public static User getCurrentUser() {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//
//        if (authentication == null || !authentication.isAuthenticated()) {
//            throw new ApiException(HttpStatus.UNAUTHORIZED, "User not authenticated");
//        }
//
//        String email = authentication.getName();
//
//        // Nếu findByEmail trả về Optional<User>
//        return userRepository.findByEmail(email)
//                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND.value(), "User not found"));
//
//        // Nếu findByEmail trả về User hoặc null, dùng:
//    /*
//    User user = userRepository.findByEmail(email);
//    if (user == null) {
//        throw new ApiException(HttpStatus.NOT_FOUND.value(), "User not found");
//    }
//    return user;
//    */
//    }



}