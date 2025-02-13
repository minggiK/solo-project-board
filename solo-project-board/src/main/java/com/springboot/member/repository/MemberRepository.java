package com.springboot.member.repository;

import com.springboot.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    //email로 member 조회
    Optional<Member> findByEmail (String email);
}
