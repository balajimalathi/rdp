package com.skndan.rdp.repo;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.skndan.rdp.entity.Ami;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public interface AmiRepo extends CrudRepository<Ami, UUID>, PagingAndSortingRepository<Ami, UUID> {

  Optional<Ami> findByImageId(String amiId);

}
