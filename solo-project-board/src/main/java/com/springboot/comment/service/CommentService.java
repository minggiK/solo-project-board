package com.springboot.comment.service;

import com.springboot.board.entity.Board;
import com.springboot.board.service.BoardService;
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

@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final BoardService boardService;
    private final MemberService memberService;

    public CommentService(CommentRepository commentRepository, BoardService boardService, MemberService memberService) {
        this.commentRepository = commentRepository;
        this.boardService = boardService;
        this.memberService = memberService;
    }

    @Transactional
    public Comment createComment(Long boardId, Comment comment, Authentication authentication) {
        //

        //요구사항 1. 관리자만 등록할 수 있다. -> SecurityConfiguration 작성
        //요구사항 2. 답변은 관리자가 한건만 등록할 수 있다.
        //-> 생성 후 Board 상태 Answer로 변경 -> 이 상태일 때는 추가적으로 작성 못하게 설정!
            // + Delete 일때도 작성 X
        cannotLeaveComment(boardId);
        //CommentStatus는 BoardStatus (Public / Secret) 와 같아야한다. ->

        Comment saveComment = commentRepository.save(comment);

        //comment가 생성되면 Board의 상태 변화 시켜줘야함
        //트랜잭션 처리
        Board board = boardService.findVerifiedBoard(boardId);
        if(!board.getBoardStatus().equals(Board.BoardStatus.QUESTION_ANSWERED)) {
            board.setBoardStatus(Board.BoardStatus.QUESTION_ANSWERED);
        }

        return saveComment;
    }

    //검증 로직 : 등록된
//    public
    //검증 로직: BoardStatus가 Delete 일때는 Comment를 달 수 없다.
    public void cannotLeaveComment(long boardId) {
        //boardId를 통해서 등록된 board를 가져와
        Board findBoard = boardService.findVerifiedBoard(boardId);

        //board 상태가 삭제되었거나 답변완료 상태일 때 답변을 입력할 수 없다.
        if(findBoard.getBoardStatus().equals(Board.BoardStatus.QUESTION_DELETE)
                || findBoard.getBoardStatus().equals(Board.BoardStatus.QUESTION_ANSWERED)) {
            throw new BusinessLogicException(ExceptionCode.CONNOT_LEAVE_COMMENT);
        }

    }


}
