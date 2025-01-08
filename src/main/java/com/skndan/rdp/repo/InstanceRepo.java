package com.skndan.rdp.repo;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.skndan.rdp.entity.Instance;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public interface InstanceRepo extends CrudRepository<Instance, UUID>, PagingAndSortingRepository<Instance, UUID> {

  Optional<Instance> findByInstanceId(String instanceId);

}
