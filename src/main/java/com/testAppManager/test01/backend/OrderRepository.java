package com.testAppManager.test01.backend;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.apache.deltaspike.data.api.AbstractEntityRepository;
import org.apache.deltaspike.data.api.EntityGraph;
import org.apache.deltaspike.data.api.EntityGraphType;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.QueryResult;
import org.apache.deltaspike.data.api.Repository;

import com.testAppManager.test01.backend.data.OrderState;
import com.testAppManager.test01.backend.data.entity.Order;

@Repository
public abstract class OrderRepository extends AbstractEntityRepository<Order, Long> {

	@EntityGraph(value = "Order.allData", type = EntityGraphType.LOAD)
	public abstract Optional<Order> findById(Long id);

	@EntityGraph(value = "Order.gridData", type = EntityGraphType.LOAD)
	public abstract QueryResult<Order> findByDueDateGreaterThan(LocalDate filterDate);

	@EntityGraph(value = "Order.gridData", type = EntityGraphType.LOAD)
	@Query("select e from OrderInfo e")
	public abstract QueryResult<Order> queryAll();

	@EntityGraph(value = "Order.gridData", type = EntityGraphType.LOAD)
	@Query("select e from OrderInfo e where lower(e.customer.fullName) like lower(?1)")
	public abstract QueryResult<Order> findByCustomerFullNameLike(String searchQuery);

	@EntityGraph(value = "Order.gridData", type = EntityGraphType.LOAD)
	@Query("select e from OrderInfo e where lower(e.customer.fullName) like lower(?1) and e.dueDate > ?2")
	public abstract QueryResult<Order> findByCustomerFullNameLikeAndDueDateGreaterThan(String searchQuery,
			LocalDate dueDate);

	@Query("select count(e) from OrderInfo e WHERE e.dueDate > ?1 AND e.state IN ?2")
	public abstract long countByDueDateAfterAndStateIn(LocalDate dueDate, Collection<OrderState> states);

	@Query("select count(e) from OrderInfo e WHERE e.dueDate > ?1")
	public abstract long countByDueDateAfter(LocalDate dueDate);

	@Query("select count(e) from OrderInfo e WHERE lower(e.customer.fullName) like lower(?1)")
	public abstract long countByCustomerFullNameContainingIgnoreCase(String searchQuery);

	@Query("select count(e) from OrderInfo e WHERE e.dueDate > ?2 AND lower(e.customer.fullName) like lower(?1)")
	public abstract long countByCustomerFullNameContainingIgnoreCaseAndDueDateAfter(String searchQuery,
			LocalDate dueDate);

	@Query("select count(e) from OrderInfo e WHERE e.dueDate = ?1")
	public abstract long countByDueDate(LocalDate dueDate);

	@Query("select count(e) from OrderInfo e WHERE e.dueDate = ?1 AND e.state IN ?2")
	public abstract long countByDueDateAndStateIn(LocalDate dueDate, Collection<OrderState> state);

	@Query("select count(e) from OrderInfo e WHERE e.state=?1")
	public abstract long countByState(OrderState state);

	public List<Object[]> countPerMonth(OrderState orderState, int year) {
		String query;
		if (JPAFeatureDetector.isDayMonthYearFunctionsSupported()) {
			query = "SELECT month(e.dueDate) as month, count(e) as deliveries FROM OrderInfo e where e.state=?1 and year(e.dueDate)=?2 group by month(e.dueDate)";
		} else {
			query = "SELECT extract(month e.dueDate) as month, count(e) as deliveries FROM OrderInfo e where e.state=?1 and extract(year e.dueDate)=?2 group by extract(month e.dueDate)";
		}

		return entityManager().createQuery(query, Object[].class).setParameter(1, orderState).setParameter(2, year)
				.getResultList();
	}

	public List<Object[]> sumPerMonthLastThreeYears(OrderState orderState, int year) {
		String query;
		if (JPAFeatureDetector.isDayMonthYearFunctionsSupported()) {
			query = "SELECT year(e.dueDate) as y, month(e.dueDate) as m, sum(oi.quantity*p.price) as deliveries FROM OrderInfo e JOIN e.items oi JOIN oi.product p where e.state=?1 and year(e.dueDate)<=?2 AND year(e.dueDate)>=(?2-3) group by year(e.dueDate),month(e.dueDate) order by y desc,month(e.dueDate)";
		} else {
			query = "SELECT extract(year e.dueDate) as y, extract(month e.dueDate) as m, sum(oi.quantity*p.price) as deliveries FROM OrderInfo e JOIN e.items oi JOIN oi.product p where e.state=?1 and extract(year e.dueDate)<=?2 AND extract(year e.dueDate)>=(?2-3) group by extract(year e.dueDate),extract(month e.dueDate) order by y desc,extract(month e.dueDate)";
		}
		return entityManager().createQuery(query, Object[].class).setParameter(1, orderState).setParameter(2, year)
				.getResultList();
	}

	public List<Object[]> countPerDay(OrderState orderState, int year, int month) {
		String query;
		if (JPAFeatureDetector.isDayMonthYearFunctionsSupported()) {
			query = "SELECT day(e.dueDate) as d, count(e) as deliveries FROM OrderInfo e where e.state=?1 and year(e.dueDate)=?2 and month(e.dueDate)=?3 group by day(e.dueDate)";
		} else {
			query = "SELECT extract(day e.dueDate), count(e) as deliveries FROM OrderInfo e where e.state=?1 and extract(year e.dueDate)=?2 and extract(month e.dueDate)=?3 group by extract(day e.dueDate)";
		}
		return entityManager().createQuery(query, Object[].class).setParameter(1, orderState).setParameter(2, year)
				.setParameter(3, month).getResultList();
	}

	public List<Object[]> countPerProduct(OrderState orderState, int year, int month) {
		String query;
		if (JPAFeatureDetector.isDayMonthYearFunctionsSupported()) {
			query = "SELECT sum(oi.quantity),p FROM OrderInfo e JOIN e.items oi JOIN oi.product p WHERE e.state=?1 AND year(e.dueDate)=?2 AND month(e.dueDate)=?3 GROUP BY p.id ORDER BY p.id";
		} else {
			query = "SELECT sum(oi.quantity),p FROM OrderInfo e JOIN e.items oi JOIN oi.product p WHERE e.state=?1 AND extract(year e.dueDate)=?2 AND extract(month e.dueDate)=?3 GROUP BY p.id ORDER BY p.id";
		}
		return entityManager().createQuery(query, Object[].class).setParameter(1, orderState).setParameter(2, year)
				.setParameter(3, month).getResultList();
	}

}
