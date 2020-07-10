package com.testAppManager.test01.backend;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.testAppManager.test01.app.HasLogger;

/**
 * Checks whether non-standard JPA features are available with the used provider
 * and database.
 * <p>
 * JPA 2.1 does not define any functions for getting the day of month, month of
 * year or year from a date column. Hibernate has support for this through
 * {@code day(dateColumn)},{@code month(dateColumn)},{@code year(dateColumn)}.
 * EclipseLink supports it through {@code extract(month dateColumn)}.
 * <p>
 * This class exists to be able to support multiple JPA providers and databases
 * with the same code. If you are targeting a specific JPA implementation and
 * database with your application, you can safely delete this class and use the
 * functions available in your combination.
 */
@Singleton
@Startup
public class JPAFeatureDetector implements HasLogger {

	@PersistenceContext
	private EntityManager entityManager;

	private static boolean dayMonthYearFunctionsSupported;

	/**
	 * Detects which features are supported by the current JPA + DB combination.
	 */
	@PostConstruct
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public void checkSupport() {
		try {
			entityManager
					.createQuery(
							"SELECT day(e.dueDate) as d, month(e.dueDate) as m, year(e.dueDate) as y from OrderInfo e")
					.setMaxResults(1).getResultList();
			dayMonthYearFunctionsSupported = true;
		} catch (Exception e) {
			dayMonthYearFunctionsSupported = false;
		}
		String supported;
		if (dayMonthYearFunctionsSupported) {
			supported = "supported";
		} else {
			supported = "NOT supported";
		}
		getLogger().info("Date functions (day(), month(), year()) are " + supported + " in JPQL queries");
	}

	/**
	 * Checks if {@code day()}, {@code month()} and {@code year()} can be used
	 * in a JPQL query.
	 *
	 * @return <code>true</code> if the given functions are available in a JPQL
	 *         query, <code>false</code> otherwise
	 */
	public static boolean isDayMonthYearFunctionsSupported() {
		return dayMonthYearFunctionsSupported;
	}

}
