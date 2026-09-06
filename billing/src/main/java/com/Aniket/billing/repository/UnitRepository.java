package com.Aniket.billing.repository;

import com.Aniket.billing.model.Unit;
import org.springframework.data.couchbase.repository.CouchbaseRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UnitRepository extends CouchbaseRepository<Unit,String> {
}
