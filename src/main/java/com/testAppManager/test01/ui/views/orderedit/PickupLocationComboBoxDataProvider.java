package com.testAppManager.test01.ui.views.orderedit;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.vaadin.data.provider.Query;
import com.vaadin.data.provider.QuerySortOrder;
import com.vaadin.shared.data.sort.SortDirection;
import com.testAppManager.test01.backend.data.entity.PickupLocation;
import com.testAppManager.test01.backend.service.PickupLocationService;
import com.testAppManager.test01.ui.components.FilterablePageableDataProvider;

/**
 * A singleton data provider which knows which products are available.
 */
@Dependent
public class PickupLocationComboBoxDataProvider extends FilterablePageableDataProvider<PickupLocation, String> {

	private final PickupLocationService pickupLocationService;

	@Inject
	public PickupLocationComboBoxDataProvider(PickupLocationService pickupLocationService) {
		this.pickupLocationService = pickupLocationService;
	}

	@Override
	protected Stream<PickupLocation> fetchFromBackEnd(Query<PickupLocation, String> query,
			List<QuerySortOrder> sortOrders) {
		return pickupLocationService.findAnyMatching(query.getFilter(), query.getOffset(), query.getLimit(),
				sortOrders);
	}

	@Override
	protected int sizeInBackEnd(Query<PickupLocation, String> query) {
		return (int) pickupLocationService.countAnyMatching(query.getFilter());
	}

	@Override
	protected List<QuerySortOrder> getDefaultSortOrders() {
		List<QuerySortOrder> sortOrders = new ArrayList<>();
		sortOrders.add(new QuerySortOrder("name", SortDirection.ASCENDING));
		return sortOrders;
	}

}
