package com.testAppManager.test01.ui.views.orderedit;

import java.util.stream.Stream;

import javax.persistence.OptimisticLockException;

import org.apache.commons.lang3.exception.ExceptionUtils;

public class PersistenceExceptionUtil {

	/**
	 * Checks if the given exception is caused by an optimistic locking problem.
	 *
	 * @param exception
	 *            the exception to check
	 * @return <code>true</code> if the exception chain contains an optimistic
	 *         locking exception, <code>false</code> otherwise
	 */
	public static boolean isOptimisticLockingException(Exception exception) {
		return ExceptionUtils.indexOfThrowable(exception, OptimisticLockException.class) != -1;
	}

	/**
	 * Checks if the given exception is caused by a constraint violation
	 * problem.
	 *
	 * @param exception
	 *            the exception to check
	 * @return <code>true</code> if the exception chain contains a constraint
	 *         violation exception, <code>false</code> otherwise
	 */
	public static boolean isConstraintViolationException(Exception exception) {
		Stream<Throwable> throwables = ExceptionUtils.getThrowableList(exception).stream();
		Stream<String> exceptionTypes = throwables.map(e -> e.getClass().getSimpleName());
		return exceptionTypes.filter(name -> "ConstraintViolationException".equals(name)).findAny().isPresent();
	}

}
