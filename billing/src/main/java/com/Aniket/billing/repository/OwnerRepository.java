package com.Aniket.billing.repository;

import com.Aniket.billing.model.Owner;
import org.springframework.data.couchbase.repository.CouchbaseRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OwnerRepository extends CouchbaseRepository<Owner, String> {
}