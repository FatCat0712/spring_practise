package vn.tayjava.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.tayjava.model.User;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    @Query("""
            select distinct u
            from User u
            left join fetch u.addresses
            where u.id in :userIds
            """)
    List<User> findAllWithAddressesByIdIn(@Param("userIds") List<Long> userIds);

    //  -- Distinct --
    // @Query(value = "SELECT DISTINCT u FROM User u WHERE u.firstName = :firstName AND u.lastName = :lastName")
    List<User> findDistinctByFirstNameAndLastName(String firstName, String lastName);

    // -- Single field --
    // @Query(value = "SELECT u FROM User u WHERE u.email = ?1")
    List<User> findByEmail(String email);

    // -- OR --
    // @Query(value = "SELECT u FROM User u WHERE u.firstName = ?1 OR u.lastName = ?1")
    List<User> findByFirstNameOrLastName(String firstName, String lastName);

    // -- Is, Equals --
    // @Query(value = "SELECT u FROM User u WHERE u.firstName = :name")
    List<User> findByFirstNameIs(String name);
    List<User> findByFirstNameEquals(String name);
    List<User> findByFirstName(String name);

    // -- Between --
    // @Query(value = "SELECT u FROM User u WHERE u.createdAt BETWEEN :start AND :end")
    List<User> findByCreatedAtBetween(Date start, Date end);

    // Less than
    // @Query(value = "select * from User u where u.age < :age")
    List<User> findByAgeLessThan(int age);
    List<User> findByAgeLessThanEqual(int age);
    List<User> findByAgeGreaterThan(int age);
    List<User> findByAgeGreaterThanEqual(int age);

    // Before vs After
    //  @Query("SELECT u FROM User u WHERE u.createdAt < :date")
    List<User> findByCreatedAtBefore(Date date);
    //  @Query("SELECT u FROM User u WHERE u.createdAt > :date")
    List<User> findByCreatedAtAfter(Date date);

    // IsNull, Null
    //  @Query("SELECT u FROM User u WHERE u.email IS NULL")
    List<User> findByEmailIsNull();

    // IsNotNull, NotNull
    // @Query("SELECT u FROM User u WHERE u.firstName IS NOT NULL")
    List<User> findByAgeNotNull();

    // Like
    // @Query("SELECT u FROM User u WHERE u.lastName LIKE %:lastName%")
    List<User> findByLastNameLike(String lastName);

    // @Query("SELECT u FROM User u WHERE u.lastName NOT LIKE %:lastName%")
    List<User> findByLastNameNotLike(String lastName);

    //StartingWith
    // @Query("SELECT u FROM User u WHERE u.lastName LIKE :lastName%")
     List<User> findByLastNameStartingWith(String lastName);

     // EndingWith
    // @Query("SELECT u FROM User u WHERE u.lastName LIKE %:lastName")
     List<User> findByLastNameEndingWith(String lastName);

     // Containing
    //  @Query("SELECT u FROM User u WHERE u.lastName LIKE %:lastName%")
     List<User> findByLastNameContaining(String lastName);

    // Not
    // @Query("SELECT u FROM User u WHERE u.lastName <> :name")
     List<User> findByLastNameNot(String name);

     // In
    // @Query("SELECT u FROM User u WHERE u.age IN :ages")
    List<User> findByAgeIn(Collection<Integer> ages);

    // Not in
    // @Query("SELECT u FROM User u WHERE u.age NOT IN :ages")
    List<User> findByAgeNotIn(Collection<Integer> ages);

    // True/False
    // @Query("SELECT u FROM User u WHERE u.activated = true")
    List<User> findByActivatedTrue();
    List<User> findByActivatedFalse();

    // IgnoreCase
    // @Query("SELECT u FROM User u WHERE LOWER(u.lastName) = LOWER(:lastName)")
    List<User> findByLastNameIgnoreCase(String lastName);

    // OrderBy
    List<User> findByFirstNameOrderByCreatedAtDesc(String name);
    List<User> findByFirstNameAndLastNameAllIgnoreCaseOrderByCreatedAtDesc(String firstName, String lastName);

    @Query(value = "SELECT * FROM User u INNER JOIN Address a ON u.id = a.user_id WHERE a.city = :city", nativeQuery = true)
    List<User> getAllUser(String city);

}
