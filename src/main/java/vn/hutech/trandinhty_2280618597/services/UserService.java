package vn.hutech.trandinhty_2280618597.services;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import vn.hutech.trandinhty_2280618597.dto.UserRegistrationDTO;
import vn.hutech.trandinhty_2280618597.entities.Role;
import vn.hutech.trandinhty_2280618597.entities.User;
import vn.hutech.trandinhty_2280618597.repositories.RoleRepository;
import vn.hutech.trandinhty_2280618597.repositories.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User registerNewUser(UserRegistrationDTO dto) {
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));

        // Assign USER role by default
        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByName(Role.ROLE_USER)
                .orElseGet(() -> {
                    Role newRole = new Role(Role.ROLE_USER);
                    return roleRepository.save(newRole);
                });
        roles.add(userRole);
        user.setRoles(roles);

        return userRepository.save(user);
    }

    public User registerAdmin(UserRegistrationDTO dto) {
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));

        // ADMIN chỉ có ROLE_ADMIN, không có ROLE_USER
        Set<Role> roles = new HashSet<>();
        Role adminRole = roleRepository.findByName(Role.ROLE_ADMIN)
                .orElseGet(() -> {
                    Role newRole = new Role(Role.ROLE_ADMIN);
                    return roleRepository.save(newRole);
                });
        roles.add(adminRole);
        user.setRoles(roles);

        return userRepository.save(user);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
