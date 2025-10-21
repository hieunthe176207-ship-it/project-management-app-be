package com.fpt.project.util;

import com.fpt.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component("userDetailsService")
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        com.fpt.project.entity.User u = userRepository.findByEmail(username);
        if (u == null) {
            throw new UsernameNotFoundException(username);
        }
        return new User(
                u.getEmail(),
                u.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("user")));
    }
}
