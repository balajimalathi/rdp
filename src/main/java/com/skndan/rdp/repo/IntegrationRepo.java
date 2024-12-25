package com.skndan.rdp.repo;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.skndan.rdp.entity.Integration;
import com.skndan.rdp.entity.constants.CloudProvider;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public interface IntegrationRepo
        extends CrudRepository<Integration, UUID>, PagingAndSortingRepository<Integration, UUID> {

    Optional<Integration> findOneByCloudProvider(CloudProvider amazonWebService);

}
