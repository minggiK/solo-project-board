package com.springboot.member.service;

import com.springboot.auth.utils.CustomAuthorityUtils;
import com.springboot.board.entity.Board;
import com.springboot.comment.entity.Comment;
import com.springboot.event.MemberRegistrationApplicationEvent;
import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.member.entity.Member;
import com.springboot.member.repository.MemberRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional
@Service
public class MemberService {
    private final MemberRepository memberRepository;
    //트랜잭션 : 회원가입 후 이벤트 발송 (가입되었다는 이메일 전송 )
    private final ApplicationEventPublisher publisher;
   //pw 암호화 기능
    private final PasswordEncoder passwordEncoder;
   //관리자용 권한부여
    private final CustomAuthorityUtils authorityUtils;

    public MemberService(MemberRepository memberRepository, ApplicationEventPublisher publisher, PasswordEncoder passwordEncoder, CustomAuthorityUtils authorityUtils) {
        this.memberRepository = memberRepository;
        this.publisher = publisher;
        this.passwordEncoder = passwordEncoder;
        this.authorityUtils = authorityUtils;
    }

    //회원가입 1. 권한 부여
    public Member createMember(Member member){
        //새로운 정보의 member가 맞는지 확인(중복이 있으면 등록시키면 안됨)
        verifyExistsMember(member.getEmail());
        //pw를 암호화 한다 : Token으로 인코딩(단방향 암호화)
        String encryptedPassword = passwordEncoder.encode(member.getPassword());
        member.setPassword(encryptedPassword);
        //권한 부여 : DB에 USER 권한
        List<String> roles = authorityUtils.createRoles(member.getEmail());
        member.setRoles(roles);
        //DB에 새로운 member 저장
        Member saveMember = memberRepository.save(member);
        publisher.publishEvent(new MemberRegistrationApplicationEvent(this, saveMember));
        return saveMember;
    }

    //격리수준 : propagation(다른 트랜잭션 내에서 어떻게 동작할지에 대한 규칙 정의 : 트랜잭션이 없으면 새로 시작할지 등 )
    //Propagation.REQUIRED : 기본값, 현재 트랜잭션이 존재하면 그 트랜잭션을 사용하고, 없으면 새로 트랜잭션을 시작
//    Isolation.SERIALIZABLE: 트랜잭션이 직렬화되어 다른 트랜잭션과 겹치지 않고 순차적으로 실행
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE)
    public Member updateMember(Member member) {
        //member 존재여부 확인
        Member findMember = findVerifiedMember(member.getMemberId());

        Optional.ofNullable(member.getPassword())
                .ifPresent(password -> findMember.setPassword(password));
        Optional.ofNullable(member.getUsername())
                .ifPresent(username -> findMember.setUsername(username));
        Optional.ofNullable(member.getPhone())
                .ifPresent(phone -> findMember.setPhone(phone));
        //DB에 수정사항 저장
        return memberRepository.save(findMember);
    }

    //읽기전용
    @Transactional(readOnly = true)
    public Member findMember(long memberId) {
       //조회된 member 찾기
        return findVerifiedMember(memberId);
    }

    @Transactional(readOnly = true)
    public Page<Member> findMembers(int page, int size){
       return memberRepository.findAll(PageRequest.of(
                page, size, Sort.by("memberId").descending()));
    }

    //board의 상태가 변경되었을 때, BoardRepository.save() 안해도 트랜잭션 범위 내 영속성 컨텍스트에서 변경된 엔티티 자동감지
    //-> JPA 가 자동으로 변경된 엔티티를 DB에 저장
    @Transactional
    public void deleteMember(long memberId) {
        //멤버 조회
        Member member = findVerifiedMember(memberId);
        //조회한 멤버의 상태를 MEMBER_QUIT(탈퇴상태)로 변경
        member.setMemberStatus(Member.MemberStatus.MEMBER_QUIT);
        //변경내용 저장
        memberRepository.save(member);

        //memberStatus가 탈퇴 상태 -> board의 상태도 비활성화로 변경되어야 한다.
        if(member.getMemberStatus().equals(Member.MemberStatus.MEMBER_QUIT)) {
            //List로 들고있으니 스트림 돌아서 하나씩 매핑
            member.getBoards().stream()
                    .forEach(board -> board.setBoardStatus(Board.BoardStatus.QUESTION_DEACTIVED));

        }
    }
    //검증 로직: 이메일 중복이 있는지 확인
    public void verifyExistsMember(String email) {
        //DB 데이터와 비교해야해 -> Repository 의 findByEmail() 을 통해 emali을 찾고 findMember에 담아줌
        Optional<Member> findMember = memberRepository.findByEmail(email);
        //email 이 있다면 true -> 이미 등록된 이메일이 있으니 등록할 수 없음 : 예외처리
        if(findMember.isPresent()) {
            //이미 등록된 회원이 있다고 사용자에게 알림
            throw new BusinessLogicException(ExceptionCode.MEMBER_EXISTS);
        }

    }

    public Long findMemberId(String email) {
        Member member = memberRepository.findByEmail(email).orElseThrow(
                ()-> new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND));

        return member.getMemberId();

    }
    //검증 로직: 회원이 존재하는지 확인, 존재한다면 그 회원을 DB에서 꺼내와야해
    public Member findVerifiedMember(long memberId) {
        //Repository에서 찾기 -> Optional
        //findMember에 DB에서 찾은 member를 담음
        Optional<Member> findMember = memberRepository.findById(memberId);
        //findMember가 비어있으면 예외처리 -> orElseThrow 해서 true면 Optional이 벗겨지고 객체가됨
        return findMember.orElseThrow(
                () -> new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND));
    }

    //검증 로직: 회원이 존재하는지 확인, 존재한다면 그 회원을 DB에서 꺼내와야해
    public Member findVerifiedMember(String email) {
        //Repository에서 찾기 -> Optional
        //findMember에 DB에서 찾은 member를 담음
        Optional<Member> findMember = memberRepository.findByEmail(email);
        //findMember가 비어있으면 예외처리 -> orElseThrow 해서 true면 Optional이 벗겨지고 객체가됨
        return findMember.orElseThrow(
                () -> new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND));
    }
    //검증로직 : 회원이 존재하는지, 글을 작성할 수 있는 상태인지 확인
    public void checkMemberStatus(long memberId) {
        //멤버가 존재하는지 확인, 없으면 member를 찾을 수 없다고 예외를 던짐
        Member member = findVerifiedMember(memberId);
        //member가 탈퇴상태면 글을 작성할 수없다고 예외 던져
        if(!member.getMemberStatus().equals(Member.MemberStatus.MEMBER_ACTIVE)) {
            throw new BusinessLogicException(ExceptionCode.MEMBER_FORBIDDEN);
        }
    }

    //검증 로직 : 관리자만 수정 가능 -> 관리자인지 확인
    public void roleAdmin(Member member) {
       if(!member.getRoles().contains("ADMIN")) {
           throw new BusinessLogicException(ExceptionCode.MEMBER_FORBIDDEN);
       }
    }
}
