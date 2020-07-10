package com.testAppManager.test01.backend;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Repository;

import com.testAppManager.test01.backend.data.entity.HistoryItem;

@Repository
public interface HistoryItemRepository extends EntityRepository<HistoryItem, Long> {
}
