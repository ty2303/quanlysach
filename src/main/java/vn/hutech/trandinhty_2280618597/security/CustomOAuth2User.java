package vn.hutech.trandinhty_2280618597.security;

import java.util.Collection;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import vn.hutech.trandinhty_2280618597.entities.User;

public class CustomOAuth2User implements OAuth2User {

    private final OAuth2User oauth2User;
    private final User user;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomOAuth2User(OAuth2User oauth2User, User user, Collection<? extends GrantedAuthority> authorities) {
        this.oauth2User = oauth2User;
        this.user = user;
        this.authorities = authorities;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return oauth2User.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getName() {
        return user.getUsername();
    }

    public User getUser() {
        return user;
    }

    public String getEmail() {
        return user.getEmail();
    }
}
