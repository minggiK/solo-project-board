package com.springboot.like.repository;

import com.springboot.like.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {
    Optional<Like> findByBoardIdAndMemberId(long boardId, long memberId);
}
