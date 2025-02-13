package com.springboot.member.controller;

import com.springboot.dto.MultiResponseDto;
import com.springboot.dto.SingleResponseDto;
import com.springboot.member.dto.MemberDto;
import com.springboot.member.entity.Member;
import com.springboot.member.mapper.MemberMapper;
import com.springboot.member.service.MemberService;
import com.springboot.utils.UriCreator;
import lombok.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.net.URI;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/v11/members")
public class MemberController {
    private final static String MEMBER_DEFAULT_URL = "/v11/members";
    private final MemberService memberService;
    private final MemberMapper mapper;

    public MemberController(MemberService memberService, MemberMapper mapper) {
        this.memberService = memberService;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity postMember(@Valid @RequestBody  MemberDto.Post requestBody){
        //Dto -> Entity로 매핑
        Member member = mapper.memberDtoPostToMember(requestBody);
        Member createMember = memberService.createMember(member);
        //source 생성
        URI location = UriCreator.createUri(MEMBER_DEFAULT_URL, createMember.getMemberId());

        return ResponseEntity.created(location).build();
    }

    @PatchMapping("/{member-id}")
    public ResponseEntity patchMember(@PathVariable("member-id") @Positive long memberId,
                                      @Valid @RequestBody MemberDto.Patch requestBody) {
        //param으로 들어온 Id와 requestBody를 함꼐 검증
        requestBody.setMemberId(memberId);
        //Dto -> entity
        Member member = mapper.memberDtoPatchToMember(requestBody);
        Member patchMember = memberService.updateMember(member);

        return new ResponseEntity<>(mapper.memberToMemberDtoResponse(patchMember), HttpStatus.OK);
    }

    @GetMapping("/{member-id}")
    public ResponseEntity getMember(@PathVariable("member-id") @Positive long memberId) {
        Member member = memberService.findMember(memberId);

        return new ResponseEntity<>(
                new SingleResponseDto<>(mapper.memberToMemberDtoResponse(member)), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity getMembers(@Positive @RequestParam("page") int page,
                                     @Positive @RequestParam("size") int size) {
        Page<Member> members = memberService.findMembers(page, size);
//      mapper 에서 Entity -> ResponseDto 로 변경해야해서 List로 받음
        //getContent() 가 List로 반환
        List<Member> memberList = members.getContent();

        return new ResponseEntity<>(
                new MultiResponseDto<>(mapper.membersToMemberDtoResponses(memberList), members), HttpStatus.OK
        );
    }

    @DeleteMapping("/{member-id}")
    public ResponseEntity deleteMember(@PathVariable("member-id") @Positive long memberId) {
        memberService.deleteMember(memberId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
