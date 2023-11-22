package hoon.study.springelastic.service;

import hoon.study.springelastic.repository.PostRepository;
import org.springframework.stereotype.Service;

@Service
public class PostService {

    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public void createPost() {
        throw new RuntimeException();
    }
}
