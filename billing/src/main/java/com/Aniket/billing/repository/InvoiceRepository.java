package com.Aniket.billing.repository;

import com.Aniket.billing.model.Invoice;
import org.springframework.data.couchbase.repository.CouchbaseRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoiceRepository extends CouchbaseRepository<Invoice, String> {
}
