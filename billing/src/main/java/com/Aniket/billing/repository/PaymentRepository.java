package com.Aniket.billing.repository;

import com.Aniket.billing.model.Payment;
import org.springframework.data.couchbase.repository.CouchbaseRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends CouchbaseRepository<Payment, String> {
}