package com.aerolane.config;

import com.aerolane.model.AppUser;
import com.aerolane.repository.AppUserRepository;
import java.util.HashSet;
import java.util.Set;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

/**
 * Keycloak answers "who is this?" (authentication). This service answers
 * "what can they do?" by looking the identity up in the local app_users table
 * (authorization). Unknown SSO identities authenticate fine but carry no role,
 * so they can't touch anything role-guarded — least privilege by default.
 */
@Service
@Profile("sso")
public class LocalRoleOidcUserService implements OAuth2UserService<OidcUserRequest, OidcUser> {

    private final OidcUserService delegate = new OidcUserService();
    private final AppUserRepository appUserRepository;

    public LocalRoleOidcUserService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = delegate.loadUser(userRequest);

        String username = oidcUser.getPreferredUsername();
        String nameAttributeKey = username != null ? "preferred_username" : "sub";

        Set<GrantedAuthority> authorities = new HashSet<>(oidcUser.getAuthorities());
        if (username != null) {
            appUserRepository.findByUsername(username)
                    .map(AppUser::getRole)
                    .ifPresent(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role.name())));
        }

        return new DefaultOidcUser(authorities, oidcUser.getIdToken(), oidcUser.getUserInfo(), nameAttributeKey);
    }
}
