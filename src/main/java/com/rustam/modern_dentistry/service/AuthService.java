package com.rustam.modern_dentistry.service;

import com.rustam.modern_dentistry.dao.entity.users.BaseUser;
import com.rustam.modern_dentistry.dto.request.read.AuthRequest;
import com.rustam.modern_dentistry.dto.response.TokenPair;
import com.rustam.modern_dentistry.dto.response.read.AuthResponse;
import com.rustam.modern_dentistry.exception.custom.IncorrectPasswordException;
import com.rustam.modern_dentistry.util.UtilService;
import com.rustam.modern_dentistry.util.user.UserDetailsServiceImpl;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class AuthService {

    UtilService utilService;
    PasswordEncoder passwordEncoder;
    UserDetailsServiceImpl userDetailsService;
    RedisTemplate<String,String> redisTemplate;
    public AuthResponse login(AuthRequest authRequest) {
        BaseUser baseUser = utilService.findByUsername(authRequest.getUsername());
        if (!passwordEncoder.matches(authRequest.getPassword(), baseUser.getPassword())) {
            throw new IncorrectPasswordException("password does not match");
        }
        UserDetails userDetails = userDetailsService.loadUserByUsername(baseUser.getId());
        TokenPair tokenPair = utilService.tokenProvider(baseUser.getId(), userDetails);
        try {
            String redisKey = "refresh_token:" + baseUser.getId();
            redisTemplate.opsForValue().set(redisKey, tokenPair.getRefreshToken(), Duration.ofDays(2)); // 2 gün müddətinə saxla
        } catch (Exception e) {
            log.error("Failed to store refresh token in Redis for user {}: {}", baseUser.getId(), e.getMessage());
        }
        return AuthResponse.builder()
                .tokenPair(tokenPair)
                .build();
    }
}
