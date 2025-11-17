package com.elyes.chess.auth;

import com.elyes.chess.user.User;
import com.elyes.chess.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // token -> userId
    private final Map<String, Long> tokens = new ConcurrentHashMap<>();

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponse register(AuthRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already taken");
        }

        User user = new User(
                request.getUsername(),
                passwordEncoder.encode(request.getPassword())
        );
        userRepository.save(user);

        String token = UUID.randomUUID().toString();
        tokens.put(token, user.getId());

        return new AuthResponse(user.getId(), user.getUsername(), token);
    }

    public AuthResponse login(AuthRequest request) {
        Optional<User> optUser = userRepository.findByUsername(request.getUsername());
        if (optUser.isEmpty()) {
            throw new RuntimeException("Invalid credentials");
        }

        User user = optUser.get();
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        String token = UUID.randomUUID().toString();
        tokens.put(token, user.getId());

        return new AuthResponse(user.getId(), user.getUsername(), token);
    }

    public Long getUserIdFromToken(String token) {
        return tokens.get(token);
    }
}
