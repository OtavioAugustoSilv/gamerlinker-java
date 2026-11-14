package com.gamerlinker.service;

import com.gamerlinker.model.Profile;
import com.gamerlinker.repository.ProfileRepository;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProfileService {

    private final ProfileRepository profileRepository;

    public ProfileService(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    public List<Profile> findAll() {
        return profileRepository.findAll();
    }

    public Optional<Profile> findById(UUID id) {
        return profileRepository.findById(id);
    }

    public Profile saveOrUpdate(Profile profile) {
        // Busca o perfil existente para preservar dados que venham nulos da requisição
        Optional<Profile> existingOpt = profileRepository.findById(profile.getId());
        
        if (existingOpt.isPresent()) {
            Profile existing = existingOpt.get();
            if (profile.getCreatedAt() == null) {
                profile.setCreatedAt(existing.getCreatedAt());
            }
            if (profile.getEmail() == null) {
                profile.setEmail(existing.getEmail());
            }
        } else {
            if (profile.getCreatedAt() == null) {
                profile.setCreatedAt(OffsetDateTime.now());
            }
        }

        return profileRepository.save(profile);
    }

    public boolean deleteById(UUID id) {
        if (profileRepository.existsById(id)) {
            profileRepository.deleteById(id);
            return true;
        }
        return false;
    }
}