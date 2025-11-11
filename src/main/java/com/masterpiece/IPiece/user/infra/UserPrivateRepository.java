package com.masterpiece.IPiece.user.infra;

import com.masterpiece.IPiece.user.domain.UserPrivate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserPrivateRepository extends JpaRepository<UserPrivate, Long> {

}
