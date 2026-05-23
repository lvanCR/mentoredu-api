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

    @EventListener
    public void onUserRegistered(UserRegisteredEvent event) {
        String displayName = (event.firstName() + " " + event.lastName()).trim();

        Profile profile = Profile.builder()
                .userId(event.userId())
                .displayName(displayName)
                .profileType(event.role())
                .build();

        profileRepository.save(profile);
    }
}
