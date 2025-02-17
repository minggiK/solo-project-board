package com.springboot.board.controller;

import com.springboot.board.dto.BoardDto;
import com.springboot.board.entity.Board;
import com.springboot.board.mapper.BoardMapper;
import com.springboot.board.service.BoardService;
import com.springboot.dto.MultiResponseDto;
import com.springboot.dto.SingleResponseDto;
import com.springboot.member.entity.Member;
import com.springboot.utils.UriCreator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.net.URI;
import java.util.List;

@Controller
@RequestMapping("/v11/boards")
public class BoardController {
    private final static String BOARD_DEFAULT_URL = "/v11/boards";
    private final BoardService boardService;
    private final BoardMapper mapper;

    public BoardController(BoardService boardService, BoardMapper mapper) {
        this.boardService = boardService;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity postBoard(@Valid @RequestBody BoardDto.Post postDto,
                                    Authentication authentication) { //특정 회원의 정보를 가져올때 필요
        //Dto -> Entity
        postDto.setMemberId(boardService.findMemberId((String) authentication.getPrincipal()));
//        System.out.println(mapper.boardPostToBoard(postDto)); //오류 디버깅
        Board board = boardService.createBoard(mapper.boardPostToBoard(postDto));
        URI location = UriCreator.createUri(BOARD_DEFAULT_URL, board.getBoardId());
        return ResponseEntity.created(location).build();
    }

    @PatchMapping("{board-id}")
    public ResponseEntity patchBoard(@Positive @PathVariable("board-id") long boardId,
                                     @Valid @RequestBody BoardDto.Patch patchDto,
                                     Authentication authentication) {
        patchDto.setBoardId(boardId);
        Board board = boardService.updateBoard(mapper.boardPatchToBoard(patchDto), authentication);

        return new ResponseEntity<>(mapper.boardToBoardResponseDto(board), HttpStatus.OK);
    }

    @GetMapping("/{board-id}")
    public ResponseEntity getBoard(@Positive @PathVariable("board-id") long boardId,
                                   Authentication authentication) {

        Board board = boardService.findBoard(boardId, authentication);
        return new ResponseEntity<>(
                new SingleResponseDto<>(mapper.boardToBoardResponseDto(board)), HttpStatus.OK
        );

    }

    @GetMapping
    public ResponseEntity getBoards(@Positive @RequestParam("page") int page,
                                     @Positive @RequestParam("size") int size) {

        Page<Board> boardPage = boardService.findBoards(page-1, size);
        List<Board> boards = boardPage.getContent();

        return  new ResponseEntity<>(
                new MultiResponseDto<>(mapper.boardsToBoardsResponseDto(boards), boardPage), HttpStatus.OK);

    }

    @DeleteMapping("/{board-id}")
    public ResponseEntity deleteBoard(@PathVariable("board-id") long boardId, Authentication authentication){

        boardService.deleteBoard(boardId, authentication);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}