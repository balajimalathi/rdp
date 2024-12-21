package com.skndan.rdp.repo;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.skndan.rdp.entity.Profile;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public interface ProfileRepo extends CrudRepository<Profile, UUID>, PagingAndSortingRepository<Profile, UUID> {

  Optional<Profile> findByUserId(String id);

  Optional<Profile> findByEmail(String email);

}
