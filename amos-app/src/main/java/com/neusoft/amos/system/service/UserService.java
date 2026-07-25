package com.neusoft.amos.system.service;

import com.neusoft.amos.system.domain.AmosUser;
import com.neusoft.amos.system.repository.AmosUserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final AmosUserRepository userRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public UserService(AmosUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public AmosUser loadByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("user not found: " + username));
    }

    public boolean checkPassword(String username, String rawPassword) {
        Optional<AmosUser> opt = userRepository.findByUsername(username);
        if (opt.isEmpty()) {
            return false;
        }
        return encoder.matches(rawPassword, opt.get().getPasswordHash());
    }

    public String encodePassword(String raw) {
        return encoder.encode(raw);
    }
}
