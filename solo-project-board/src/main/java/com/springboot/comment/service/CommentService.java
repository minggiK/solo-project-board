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

import java.rmi.server.ExportException;
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

        //comment를 등록할 Board를 꺼내서 findBoard에 할당
        Board findBoard = boardService.findVerifiedBoard(boardId);
        //요구사항 1. 관리자만 등록할 수 있다. -> SecurityConfiguration 작성
        Member findMember = memberService.findVerifiedMember(authentication.getPrincipal().toString());
        //요구사항 2. 답변은 관리자가 한건만 등록할 수 있다.
        memberService.roleAdmin(findMember);
        //-> 생성 후 Board 상태 Answer로 변경 -> 이 상태일 때는 추가적으로 작성 못하게 설정!
            // + Delete, Deactived 일때도 작성 X
        cannotLeaveComment(findBoard, comment);
        //CommentStatus는 BoardStatus (Public / Secret) 와 같아야한다. ->

        return commentRepository.save(comment);
    }

    @Transactional
    public Comment updateComment(Long bardId, Comment comment, Authentication authentication) {
        //등록된 답변은 관리자만 수정할 수 있어야한다.
        Board findBoard = boardService.findVerifiedBoard(bardId);
        //회원정보가 관리자일때
        Member findMember = memberService.findVerifiedMember(authentication.getPrincipal().toString());
        memberService.roleAdmin(findMember);

        Optional.ofNullable(comment.getText())
                .ifPresent(content -> findBoard.getComment().setText(content));


        return commentRepository.save(comment);
    }

    @Transactional
    public void deleteComment(long boardId, Authentication authentication){
        Member findMember = memberService.findVerifiedMember(authentication.getPrincipal().toString());
        //Admin 만 삭제 가능
        memberService.roleAdmin(findMember);
        //등록된 Board 찾아서 Board의 Comment 상태 변경
        Board findBoard = boardService.findVerifiedBoard(boardId);
        commentStatusDelete(findBoard);
        
        Comment comment = findBoard.getComment();

        commentRepository.save(comment);
    }

    //검증 로직: BoardStatus가 Registered 외의 상태 (Delete,Answer,Deactived) 일때는 Comment를 달 수 없다.
    public Board cannotLeaveComment(Board board, Comment comment) {
       //BoardStatus가 REGISTERED 일때만 comment 추가
        if(board.getBoardStatus().equals(Board.BoardStatus.QUESTION_REGISTERED)){
            board.setComment(comment);
            //Board의 상태 변화 : comment가 1개 달리면 상태는 Answer로 변경해야함
            //트랜잭션 처리
            board.setBoardStatus(Board.BoardStatus.QUESTION_ANSWERED);
            //board 상태가 삭제되었거나 답변완료 상태일 때 답변을 입력할 수 없다.
        } else {
            throw new BusinessLogicException(ExceptionCode.CONNOT_LEAVE_COMMENT);
        }

        return board;
    }


    //delete 삭제변화
    public Board commentStatusDelete(Board board) {
        //이미 삭제된 Comment 라면, 예외처리
       if(board.getComment().getCommentStatus().equals(Comment.CommentStatus.COMMENT_DELETE)) {
           throw new BusinessLogicException(ExceptionCode.COMMENT_NOT_FOUND);
       } else {
           //Delete 상테가 아니라면, 상태변경하고,
           board.getComment().setCommentStatus(Comment.CommentStatus.COMMENT_DELETE);
           //Board 조회했을 때 Comment 내용이 보이면 안된다.
           board.setComment(new Comment());
       }
        return board;
    }

}
