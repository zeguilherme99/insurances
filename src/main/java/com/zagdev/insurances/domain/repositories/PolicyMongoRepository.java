package com.zagdev.insurances.domain.repositories;

import com.zagdev.insurances.domain.entity.Policy;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PolicyMongoRepository extends MongoRepository<Policy, UUID> {

    List<Policy> findByCustomerId(UUID customerId);
}
