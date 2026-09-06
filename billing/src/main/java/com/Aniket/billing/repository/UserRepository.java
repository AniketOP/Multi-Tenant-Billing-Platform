package com.Aniket.billing.repository;

import com.Aniket.billing.model.User;
import org.springframework.data.couchbase.repository.CouchbaseRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends CouchbaseRepository<User,String > {
    Optional<User> findByUsername(String username);
}
