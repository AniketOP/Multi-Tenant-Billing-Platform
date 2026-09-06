package com.Aniket.billing.repository;

import com.Aniket.billing.model.Tenant;
import org.springframework.data.couchbase.repository.CouchbaseRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TenantRepository extends CouchbaseRepository<Tenant,String> {

}
