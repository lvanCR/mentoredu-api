package com.mentoredu.config;

import com.mentoredu.config.exception.NotAuthenticatedException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;

public final class SecurityUtils {

    private SecurityUtils() {}

    public static Authentication auth() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public static boolean isUnauthenticated(Authentication auth) {
        return auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken;
    }

    public static String currentEmail() {
        Authentication auth = auth();
        if (isUnauthenticated(auth)) {
            throw new NotAuthenticatedException("Autenticación requerida.");
        }
        return auth.getName();
    }

    public static void requireAuth() {
        if (isUnauthenticated(auth())) {
            throw new NotAuthenticatedException("Autenticación requerida.");
        }
    }

    public static void requireAnyRole(String... roles) {
        Authentication auth = auth();
        if (isUnauthenticated(auth)) {
            throw new NotAuthenticatedException("Autenticación requerida.");
        }
        boolean hasRole = Arrays.stream(roles).anyMatch(role ->
            auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_" + role)));
        if (!hasRole) {
            throw new AccessDeniedException("Acceso denegado.");
        }
    }
}
