package com.springboot.board.service;

import com.springboot.board.entity.Board;
import com.springboot.board.repository.BoardRepository;
import com.springboot.comment.repository.CommentRepository;
import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.member.entity.Member;
import com.springboot.member.service.MemberService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional
@Service
public class BoardService {
    private final BoardRepository boardRepository;
    private final MemberService memberService;


    public BoardService(BoardRepository boardRepository, MemberService memberService, CommentRepository commentRepository) {
        this.boardRepository = boardRepository;
        this.memberService = memberService;
    }

    //board 생성
    public Board createBoard(Board board) {
        //글을 작성할 수 있는 member가 맞는지 확인
        //->오류 발생 :Dto, mapper에 추가 지정
        memberService.checkMemberStatus(board.getMember().getMemberId());
        //글이 등록되면 board를 repsitory에 save
        return boardRepository.save(board);

    }


    public Board updateBoard(Board board, Authentication authentication) {
        //board(-> 수정사항이 담긴 데이터)와 동일한 id가 존재하는지 검증
        //기존에 DB에 저장된 데이터
        Board findBoard = findVerifiedBoard(board.getBoardId());
        //요구사항 5. 답변이 완료된 질문은 수정할 수 없다,
        //기존에 저장된 데이터의 상태 확인
        cannotChangeBoard(findBoard);
        //요구사항 1. 질문의 제목, 내용은 등록한 회원만 수정 가능
//        findRegisteredMember(findboard, authentication);
        //요구사항 2. 비밀글로 변경할 경우 공개 상태를 QUESTION_SECRET 상태로 수정해야한다.
        //요구사항 3. 질문 상태 QUESTION_ANSWERED 로 변경은 관리자만 가능하다.  -> postComment 에서 설정
        //요구사항 4. 회원이 질문을 삭제할 경우, QUESTION_DELETE로 상태 변경되어야한다. -> deleteBoard 에서 실행

        //요구사항 1. 질문의 제목, 내용은 등록한 회원만 수정 가능
        Member member =
                memberService.findVerifiedMember(authentication.getPrincipal().toString());

        //글을 작성한 회원이 맞다면 수정 가능
        if(member.getMemberId().equals(findBoard.getMember().getMemberId())) {
            //변경될 값이 null이 아니라면,
            Optional.ofNullable(board.getTitle())
                    //기존 DB에 있던 데이터에 변경
                    .ifPresent(title -> findBoard.setTitle(title));
            Optional.ofNullable(board.getContent())
                    .ifPresent(content -> findBoard.setContent(content));
            Optional.ofNullable(board.getPublicStatus())
                    .ifPresent(publicStatus -> findBoard.setPublicStatus(publicStatus));
            return findBoard;
        }

        return boardRepository.save(findBoard);
    }

    @Transactional(readOnly = true)
    //요구사항 1. 1건의 특정 질문은 회원과 관리자 모두 조회가능
    public Board findBoard(Long boardId, Authentication authentication) {
        //요구사항 2. 비밀글 상태의 경우, 등록한 회원과 관리자만 조회가능
        Board findBoard = findVerifiedBoard(boardId);
        boardSecretStatus(findBoard, authentication);
        //요구사항 4. 삭제한 질문은 조회할 수 없다.
        boardStatusDelete(findBoard);
        //요구사항 3. 1건의 질문 조회 시, 해당 질문에 대한 답변이 존재한다면 답변도 함께 조회
//        commentExistsBoard(board.getBoardId());
        return findBoard;
    }


    @Transactional(readOnly = true)
    public Page<Board> findBoards(int page, int size) {
        //요구사항 1. 일반 회원, 관리자 모두 조회 가능 -> 비밀글의 경우 목록에서 제외시키지말고 비밀글임을 알려줘야한다.

        //요구사항 2. 삭제 상태가 아닌 질문만 조회 가능
//        boardStatusDelete();
        //요구사항 3. 답변이 존재한다면 각 질문에 대한 답변도 함꼐 조회
        //요구사항 4. 페이지네이션 처리가 되어 일정 건수 만큼 데이터만 조회할 수 있다,
        //조회 조건 정렬 : 최신글 순 / 오래된 글 순 / 좋아요 많은 순, 적은 순 / 조회수 많은 순, 적은 순

        return boardRepository.findAll(PageRequest.of(
                page, size, org.springframework.data.domain.Sort.by("boardId").descending()));
    }


    public void deleteBoard(long boardId, Authentication authentication) {
        //등록되어있는 게시글인지 확인
        Board findBoard = findVerifiedBoard(boardId);
        //요구사항 1. 1건의 질문은 작성한 회원만 삭제할 수 있다.
        findRegisteredMember(findBoard, authentication);
        //요구사항 4. 이미 삭제된 질문은 삭제할 수 없다.
        boardStatusDelete(findBoard);
        //요구사항 3. 질문 삭제 시, 질문의 상태만 변경되어야 한다.
        findBoard.setBoardStatus(Board.BoardStatus.QUESTION_DELETE);

        boardRepository.save(findBoard);
    }

    //검증 로직: board가 이미 있다면 등록되어있는 board를 return, 없다면 예외처리
    public Board findVerifiedBoard(long boardId) {
        Optional<Board> optionalBoard = boardRepository.findById(boardId);
        //
        return optionalBoard.orElseThrow(
                () -> new BusinessLogicException(ExceptionCode.BOARD_NOT_FOUND));
    }

    //검증 로직: 게시글이 '비밀글' 상태인 경우 접근 제한 주기
    public void boardSecretStatus(Board board, Authentication authentication) {
        //등록된 Board를 findBoard에 담아줌

        //글을 등록한 회원의 정보를 담은 Member를 생성
        Member member = memberService.findVerifiedMember(authentication.getPrincipal().toString());
//       getPrincipal()의 반환값이 사용자 주체정보(현재는 String -> Member로 강제 형변환 안됨
//        Member member = (Member) authentication.getPrincipal();

        //Board가 비공개 상태라면, 등록한 회원과 관리자만 조회가 가능하다.
        if (board.getPublicStatus().equals(Board.BoardPublicStatus.SECRET)
                && !member.getMemberId().equals(board.getMember().getMemberId())) {
            throw new BusinessLogicException(ExceptionCode.BOARD_UNAUTHORIZED);
        }

    }

    //검증 로직: 삭제된 게시글이라면 조회할 수 없다.
    public void boardStatusDelete(Board board) {
        if (board.getBoardStatus().equals(Board.BoardStatus.QUESTION_DELETE)) {
            throw new BusinessLogicException(ExceptionCode.BOARD_NOT_FOUND);
        }
    }

    //검증 로직: 답글이 작성되었다면 함께 조회, 작성되지 않았다면 질문만 조회
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

    public Long findMemberId(String email) {
        return memberService.findMemberId(email);
    }

    //작성한 회원만 접근가능
    public void findRegisteredMember(Board board, Authentication authentication) {
//        Board findBoard = findVerifiedBoard(boardId);
        Member member =
                memberService.findVerifiedMember(authentication.getPrincipal().toString());

        if (!board.getMember().getMemberId().equals(member.getMemberId())) {
            throw new BusinessLogicException(ExceptionCode.BOARD_UNAUTHORIZED);
        }

    }

    //답변이 완료된 게시글(질문)은 수정이 안된다.
    public void cannotChangeBoard(Board board) {
        Board findBoard = findVerifiedBoard(board.getBoardId());

        if(findBoard.getBoardStatus().equals(Board.BoardStatus.QUESTION_ANSWERED)) {
            throw new BusinessLogicException(ExceptionCode.CANNOT_CHANGE_BOARD);
        }
    }


}
