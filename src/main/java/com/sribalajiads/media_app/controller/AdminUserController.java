package com.sribalajiads.media_app.controller;

import com.sribalajiads.media_app.dto.CreateUserRequest;
import com.sribalajiads.media_app.dto.UserResponse;
import com.sribalajiads.media_app.model.Role;
import com.sribalajiads.media_app.model.User;
import com.sribalajiads.media_app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/users")
// @PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping
    public ResponseEntity<List<UserResponse>> listUsers() {
        List<UserResponse> users = userRepository.findAll().stream()
                .map(user -> new UserResponse(user.getId(), user.getUsername(), user.getRole(), user.isEnabled()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

 @Autowired
private JdbcTemplate jdbcTemplate;

@PostMapping
public ResponseEntity<?> createUser(@RequestBody CreateUserRequest request) {

    if (userRepository.findByUsername(request.getUsername()).isPresent()) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("message", "Username already exists"));
    }

    Long nextId = jdbcTemplate.queryForObject(
            "SELECT IFNULL(MAX(id),0)+1 FROM users",
            Long.class
    );

    jdbcTemplate.update(
            "INSERT INTO users(id,username,password,role,enabled) VALUES (?,?,?,?,?)",
            nextId,
            request.getUsername(),
            passwordEncoder.encode(request.getPassword()),
            request.getRole().name(),
            true
    );

    return ResponseEntity.ok(Map.of("message","User created"));
}

    @PatchMapping("/{id}/enable")
    public ResponseEntity<?> enableUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        user.setEnabled(true);
        userRepository.save(user);
        return ResponseEntity.ok(new UserResponse(user.getId(), user.getUsername(), user.getRole(), user.isEnabled()));
    }

    @PatchMapping("/{id}/disable")
    public ResponseEntity<?> disableUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        // Prevent admin from disabling themselves
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        if (user.getUsername().equals(currentUsername)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "You cannot disable your own account"));
        }

        user.setEnabled(false);
        userRepository.save(user);
        return ResponseEntity.ok(new UserResponse(user.getId(), user.getUsername(), user.getRole(), user.isEnabled()));
    }
    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication){

        return ResponseEntity.ok(Map.of(
                "username", authentication.getName(),
                "authorities", authentication.getAuthorities()
        ));
    }
}
