package com.springboot.board.service;

import com.springboot.board.entity.Board;
import com.springboot.board.repository.BoardRepository;
import com.springboot.comment.entity.Comment;
import com.springboot.comment.repository.CommentRepository;
import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.member.entity.Member;
import com.springboot.member.service.MemberService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional
@Service
public class BoardService {
    private final BoardRepository boardRepository;
    private final MemberService memberService;
    private final CommentRepository commentRepository;

    public BoardService(BoardRepository boardRepository, MemberService memberService, CommentRepository commentRepository) {
        this.boardRepository = boardRepository;
        this.memberService = memberService;
        this.commentRepository = commentRepository;
    }

    //board 생성
    public Board createBoard(Board board) {
        //글을 작성할 수 있는 member가 맞는지 확인
        //->오류 발생 :Dto, mapper에 추가 지정
        memberService.checkMemberStatus(board.getMember().getMemberId());
        //글이 등록되면 board를 repsitory에 save
        return boardRepository.save(board);

    }


//    public Board updateBoard(Board board) {
//        //board가 존재하는지 검증
//        Board findboard = findVerifiedBoard(board.getBoardId());
//        //요구사항 1. 질문의 제목, 내용은 등록한 회원만 수정 가능
//        //요구사항 2. 비밀글로 변경할 경우 공개 상태를 QUESTION_SECRET 상태로 수정해야한다.
//        //요구사항 3. 질문 상태 QUESTION_ANSWERED 로 변경은 관리자만 가능하다.
//        //요구사항 4. 회원이 질문을 삭제할 경우, QUESTION_DELETE로 상태 변경되어야한다.
//        //요구사항 5. 답변이 완료된 질문은 수정할 수 없다,
//
//    }

    @Transactional(readOnly = true)
    //요구사항 1. 1건의 특정 질문은 회원과 관리자 모두 조회가능
    public Board findBoard(Long boardId, Authentication authentication) {
        //요구사항 2. 비밀글 상태의 경우, 등록한 회원과 관리자만 조회가능
        Board board = boardSecretStatus(boardId, authentication);
        //요구사항 4. 삭제한 질문은 조회할 수 없다.
        boardStatusDelete(board);
        //요구사항 3. 1건의 질문 조회 시, 해당 질문에 대한 답변이 존재한다면 답변도 함께 조회
        commentExistsBoard(board.getBoardId());
        return board;
    }

    public void deleteBoard (long boardId) {
        //등록되어있는 게시글인지 확인
        Board findBoard = findVerifiedBoard(boardId);

        //요구사항 1. 1건의 질문은 작성한 회원만 삭제할 수 있다.
        //요구사항 2. 1건의 질문 삭제는 질문을 등록한 회원만 가능하다
        //요구사항 3. 질문 삭제 시, 질문의 상태만 변경되어야 한다.


        //요구사항 4. 이미 삭제된 질문은 삭제할 수 없다.
        boardStatusDelete(findBoard);


    }

    //검증 로직: board가 이미 있다면 등록되어있는 board를 return, 없다면 예외처리
    public Board findVerifiedBoard(long boardId) {
    Optional<Board> optionalBoard = boardRepository.findById(boardId);
    //
    return optionalBoard.orElseThrow(
            () -> new BusinessLogicException(ExceptionCode.BOARD_NOT_FOUND));
    }

    //검증 로직: 게시글이 '비밀글' 상태인 경우 접근 제한 주기
    public Board boardSecretStatus(long boardId, Authentication authentication){
        //등록된 Board를 findBoard에 담아줌
        Board findBoard = findVerifiedBoard(boardId);
        //글을 등록한 회원의 정보를 담은 Member를 생성
        Member member = (Member) authentication.getPrincipal();
        //Board가 비공개 상태라면, 등록한 회원과 관리자만 조회가 가능하다.
        if (findBoard.getPublicStatus().equals(Board.BoardPublicStatus.QUESTION_SECRET)) {
            //가져온 memberId와 board가 가지고 있는 memberId가 다르면 접근할 수 없다고 예외 날림
            if (!member.getMemberId().equals(findBoard.getMember().getMemberId())) {
                throw new BusinessLogicException(ExceptionCode.BOARD_UNAUTHORIZED);
            }
        }
        return findBoard;
    }

    //검증 로직: 삭제된 게시글이라면 조회할 수 없다.
    public void boardStatusDelete(Board board){
        if (board.getBoardStatus().equals(Board.BoardStatus.QUESTION_DELETE)) {
            throw new BusinessLogicException(ExceptionCode.BOARD_NOT_FOUND);
        }
    }

//    검증 로직: 답글이 작성되었다면 함께 조회, 작성되지 않았다면 질문만 조회
    public Board commentExistsBoard(long boardId) {
       return boardRepository.findById(boardId).orElseThrow(
               () -> new BusinessLogicException(ExceptionCode.BOARD_NOT_FOUND)
       );

       //해당 Board를 찾아서 새 객체에 담아줌
//        Board board = new Board();
//        board.setTitle(findBoard.getTitle());
//        board.setContent(findBoard.getContent());
//        board.setMember(findBoard.getMember());
//        //게시판에서 comment 가 있다면,
//        if(findBoard.getComment() != null) {
//            board.setComment(findBoard.getComment());
//
//        } else {
//            board.setComment(new Comment());
//        }
//        return board;
    }

    public Long findMemberId (String email){
        return memberService.findMemberId(email);
    }

//    public

}
