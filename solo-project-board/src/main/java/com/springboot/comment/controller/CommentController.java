package com.springboot.comment.controller;

import com.springboot.comment.dto.CommentDto;
import com.springboot.comment.entity.Comment;
import com.springboot.comment.mapper.CommentMapper;
import com.springboot.comment.service.CommentService;
import com.springboot.member.service.MemberService;
import com.springboot.utils.UriCreator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.net.URI;

@Controller
@RequestMapping("/v11/boards/{board-id}/comments")
public class CommentController {
    //ResourceId 가 boardId라 새로 생성 X
//    private final static String COMMENT_DEFAULT_URL = "/v11/boards/{board-id}/comments";
    private final CommentService commentService;
    private final CommentMapper mapper;
    private final MemberService memberService;

    public CommentController(CommentService commentService, CommentMapper mapper,  MemberService memberService) {
        this.commentService = commentService;
        this.mapper = mapper;
        this.memberService = memberService;
    }

    @PostMapping
    public ResponseEntity postComment (@Positive @PathVariable("board-id") long boardId,
                                       @Valid @RequestBody CommentDto.Post postDto,
                                       Authentication authentication) {

        postDto.setMemberId(memberService.findMemberId(authentication.getPrincipal().toString()));
        commentService.createComment(boardId, mapper.commentPostDtoToComment(postDto), authentication);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

}
