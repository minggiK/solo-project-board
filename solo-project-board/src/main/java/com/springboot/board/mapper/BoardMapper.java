package com.springboot.board.mapper;

import com.springboot.board.dto.BoardDto;
import com.springboot.board.entity.Board;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

//, unmappedTargetPolicy = ReportingPolicy.IGNORE
@Mapper(componentModel = "spring")
public interface BoardMapper {
    //postDto 가 가지고 있는 필드랑 entity가 가지고 있는 필드명이 달라서
    //target, source로 서로 지정해줌
    @Mapping(target = "member.memberId", source = "memberId")
    Board boardPostToBoard(BoardDto.Post postDto);
    Board boardPatchToBoard(BoardDto.Patch patchDto);
    @Mapping(target = "comment", source = "comment.content")
    BoardDto.Response boardToBoardResponseDto(Board board);
    List<BoardDto.Response> boardsToBoardsResponseDto(List<Board> boards);

}

