package vn.tayjava.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.tayjava.model.User;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Query("""
            select distinct u
            from User u
            left join fetch u.addresses
            where u.id in :userIds
            """)
    List<User> findAllWithAddressesByIdIn(@Param("userIds") List<Long> userIds);
}
