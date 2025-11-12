package com.masterpiece.IPiece.favorite.infra;


import com.masterpiece.IPiece.favorite.domain.FavoriteList;
import com.masterpiece.IPiece.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FavoriteListRepository extends JpaRepository<FavoriteList, Long> {

    // 특정 유저의 찜 목록 조회
    List<FavoriteList> findAllByUser(User user);

}