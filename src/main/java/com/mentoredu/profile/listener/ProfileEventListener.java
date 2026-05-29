package com.mentoredu.profile.listener;

import com.mentoredu.auth.event.UserRegisteredEvent;
import com.mentoredu.profile.model.Profile;
import com.mentoredu.profile.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProfileEventListener {

    private final ProfileRepository profileRepository;

    private static final java.util.Set<String> TYPED_ROLES =
            java.util.Set.of("STUDENT", "TEACHER", "ACADEMY");

    @EventListener
    public void onUserRegistered(UserRegisteredEvent event) {
        // Only STUDENT / TEACHER / ACADEMY have specialized profile tables.
        // ADMIN and MODERATOR are provisioned directly in the DB and don't need profile records.
        if (!TYPED_ROLES.contains(event.role())) return;

        String displayName = (event.firstName() + " " + event.lastName()).trim();

        Profile profile = Profile.builder()
                .userId(event.userId())
                .displayName(displayName)
                .profileType(event.role())
                .build();

        profileRepository.save(profile);
    }
}
