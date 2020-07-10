package com.testAppManager.test01.backend;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Repository;

import com.testAppManager.test01.backend.data.entity.Customer;

@Repository
public interface CustomerRepository extends EntityRepository<Customer, Long> {
}
