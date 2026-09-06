package com.Aniket.billing.repository;

import com.Aniket.billing.model.Profile;
import org.springframework.data.couchbase.repository.CouchbaseRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfileRepository extends CouchbaseRepository<Profile, String> {
}