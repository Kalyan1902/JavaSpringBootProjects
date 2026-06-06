package com.cts.vis.security;

import com.cts.vis.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        Optional<com.cts.vis.model.User> opt = userRepository.findByEmail(email);
        if (!opt.isPresent()) {
            throw new UsernameNotFoundException("User not found: " + email);
        }

        com.cts.vis.model.User u = opt.get();

        List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority(u.getRole().name()));

        return new org.springframework.security.core.userdetails.User(
                u.getEmail(),
                u.getPasswordHash(),
                authorities
        );
    }
}