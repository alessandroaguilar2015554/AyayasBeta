package com.testAppManager.test01.backend;

import java.util.Optional;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.QueryResult;
import org.apache.deltaspike.data.api.Repository;

import com.testAppManager.test01.backend.data.entity.User;

@Repository
public interface UserRepository extends EntityRepository<User, Long> {

	Optional<User> findByEmail(String email);

	QueryResult<User> findByEmailLikeIgnoreCaseOrNameLikeIgnoreCaseOrRoleLikeIgnoreCase(String emailLike,
			String nameLike, String roleLike);

	@Query("select e from UserInfo e")
	QueryResult<User> queryAll();

	@Query("select count(e) from UserInfo e WHERE lower(e.email) like lower(?1) or lower(e.name) like lower(?2)")
	long countByEmailLikeIgnoreCaseOrNameLikeIgnoreCase(String emailLike, String nameLike);
}
