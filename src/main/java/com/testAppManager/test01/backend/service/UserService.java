package com.testAppManager.test01.backend.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.deltaspike.data.api.QueryResult;

import com.vaadin.data.provider.QuerySortOrder;
import com.testAppManager.test01.app.security.BcryptPasswordMatcher;
import com.testAppManager.test01.backend.UserRepository;
import com.testAppManager.test01.backend.data.entity.User;

@Stateless
public class UserService extends CrudService<User> {

	private static final String MODIFY_LOCKED_USER_NOT_PERMITTED = "User has been locked and cannot be modified or deleted";
	private final BcryptPasswordMatcher passwordEncoder;
	private final UserRepository userRepository;

	public UserService() {
		// An empty constructor is required by the EJB spec even though the
		// @Inject constructor is used
		passwordEncoder = null;
		userRepository = null;
	}

	@Inject
	public UserService(UserRepository userRepository, BcryptPasswordMatcher passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	public User findByEmail(String email) {
		return userRepository.findByEmail(email).orElse(null);
	}

	@Override
	public Stream<User> findAnyMatching(Optional<String> filter, int offset, int limit,
			List<QuerySortOrder> sortOrders) {
		QueryResult<User> result;
		if (filter.isPresent()) {
			String repositoryFilter = "%" + filter.get() + "%";
			result = userRepository.findByEmailLikeIgnoreCaseOrNameLikeIgnoreCaseOrRoleLikeIgnoreCase(repositoryFilter,
					repositoryFilter, repositoryFilter);
		} else {
			result = getRepository().queryAll();
		}
		result = QueryHelper.applyLimitsAndSortOrder(result, offset, limit, sortOrders);
		return result.getResultList().stream();
	}

	@Override
	public long countAnyMatching(Optional<String> filter) {
		if (filter.isPresent()) {
			String repositoryFilter = "%" + filter.get() + "%";
			return getRepository().countByEmailLikeIgnoreCaseOrNameLikeIgnoreCase(repositoryFilter, repositoryFilter);
		} else {
			return getRepository().count();
		}
	}

	@Override
	protected UserRepository getRepository() {
		return userRepository;
	}

	public String encodePassword(String value) {
		return passwordEncoder.encode(value);
	}

	@Override
	public User save(User entity) {
		throwIfUserLocked(entity.getId());
		return super.save(entity);
	}

	@Override
	public void delete(long userId) {
		throwIfUserLocked(userId);
		super.delete(userId);
	}

	private void throwIfUserLocked(Long userId) {
		if (userId == null) {
			return;
		}

		User dbUser = getRepository().findBy(userId);
		if (dbUser.isLocked()) {
			throw new UserFriendlyDataException(MODIFY_LOCKED_USER_NOT_PERMITTED);
		}
	}

}
