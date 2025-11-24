package com.masterpiece.IPiece.user.infra;

import com.masterpiece.IPiece.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    //사용자 ID 중복체크용 메서드
    //boolean existsByUserMadeId(String userMadeId);

    Optional<User> findByUserMadeId(String loginId);

    boolean existsByUserMadeId(String userId);

    @Query("select u.userMadeId from User u where u.userId = :userId")
    String findUserMadeIdByUserId(@Param("userId") Long userId);

}
