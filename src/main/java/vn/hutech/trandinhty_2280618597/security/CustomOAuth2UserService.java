package vn.hutech.trandinhty_2280618597.security;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import vn.hutech.trandinhty_2280618597.entities.Role;
import vn.hutech.trandinhty_2280618597.entities.User;
import vn.hutech.trandinhty_2280618597.repositories.RoleRepository;
import vn.hutech.trandinhty_2280618597.repositories.UserRepository;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");
        String sub = oauth2User.getAttribute("sub"); // Google user ID

        // Find or create user
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> createNewUser(email, name, sub));

        // Build authorities from user roles
        var authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());

        return new CustomOAuth2User(oauth2User, user, authorities);
    }

    private User createNewUser(String email, String name, String googleId) {
        User user = new User();
        user.setEmail(email);
        user.setUsername(name != null ? name : email.split("@")[0]);
        user.setPassword(""); // No password for OAuth2 users
        user.setProvider("GOOGLE");
        user.setProviderId(googleId);

        // Assign USER role for shopping
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
}
