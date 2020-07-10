package com.testAppManager.test01.backend.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.deltaspike.data.api.QueryResult;

import com.vaadin.data.provider.QuerySortOrder;
import com.vaadin.shared.data.sort.SortDirection;
import com.testAppManager.test01.backend.PickupLocationRepository;
import com.testAppManager.test01.backend.data.entity.PickupLocation;

@Stateless
public class PickupLocationService {

	private final PickupLocationRepository pickupLocationRepository;

	public PickupLocationService() {
		// An empty constructor is required by the EJB spec even though the
		// @Inject constructor is used
		pickupLocationRepository = null;
	}

	@Inject
	public PickupLocationService(PickupLocationRepository pickupLocationRepository) {
		this.pickupLocationRepository = pickupLocationRepository;
	}

	public Stream<PickupLocation> findAnyMatching(Optional<String> filter, int offset, int limit,
			List<QuerySortOrder> sortOrders) {
		String repositoryFilter = "%";
		if (filter.isPresent()) {
			repositoryFilter = "%" + filter.get() + "%";
		}

		QueryResult<PickupLocation> result = pickupLocationRepository.findByNameLikeIgnoreCase(repositoryFilter);
		result = QueryHelper.applyLimitsAndSortOrder(result, offset, limit, sortOrders);
		return result.getResultList().stream();
	}

	public long countAnyMatching(Optional<String> filter) {
		String repositoryFilter = "%";
		if (filter.isPresent()) {
			repositoryFilter = "%" + filter.get() + "%";
		}
		return pickupLocationRepository.countByNameLikeIgnoreCase(repositoryFilter);
	}

	public PickupLocation getDefault() {
		return findAnyMatching(Optional.empty(), 0, 1,
				Collections.singletonList(new QuerySortOrder("name", SortDirection.ASCENDING))).iterator().next();
	}

}
