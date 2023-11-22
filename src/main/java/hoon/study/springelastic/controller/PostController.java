package hoon.study.springelastic.controller;

import hoon.study.springelastic.entity.PostDocument;
import hoon.study.springelastic.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/post")
public class PostController {
    private final PostService postService;

    @Autowired
    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    public ResponseEntity createPost(
            @RequestParam(value = "title") String title,
            @RequestParam(value = "content") String content
    ) {

        postService.createPost(title, content);

        return new ResponseEntity("", HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity searchPost(
            @RequestParam(value = "keyword") String keyword) {
        List<PostDocument> posts = postService.searchPost(keyword);

        return new ResponseEntity(posts, HttpStatus.OK);
    }
}
