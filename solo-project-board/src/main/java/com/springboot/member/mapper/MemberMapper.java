package com.springboot.member.mapper;

import com.springboot.member.dto.MemberDto;
import com.springboot.member.entity.Member;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

//componentModel = "spring": MapStruct가 생성하는 매퍼 클래스를 Spring Bean으로 관리하도록 설정.
//unmappedSourcePolicy = ReportingPolicy.IGNORE: 매핑되지 않은 필드는 무시하도록 설정.
@Mapper(componentModel = "spring", unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface MemberMapper {
    Member memberDtoPostToMember(MemberDto.Post requestBody);
    Member memberDtoPatchToMember(MemberDto.Patch requestBody);
    MemberDto.Response memberToMemberDtoResponse(Member member);
    List<MemberDto.Response> membersToMemberDtoResponses(List<Member> members);

}

