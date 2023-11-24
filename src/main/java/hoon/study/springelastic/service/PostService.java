package hoon.study.springelastic.service;

import hoon.study.springelastic.entity.Post;
import hoon.study.springelastic.entity.PostDocument;
import hoon.study.springelastic.repository.PostDocumentRepository;
import hoon.study.springelastic.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PostService {

    private final PostRepository postRepository;

    private final PostDocumentRepository postDocumentRepository;

    @Autowired
    public PostService(PostRepository postRepository, PostDocumentRepository postDocumentRepository) {
        this.postRepository = postRepository;
        this.postDocumentRepository = postDocumentRepository;
    }

    public void createPost(String title, String content) {
        Post post = Post.builder()
                .title(title)
                .content(content)
                .createdAt(LocalDateTime.now())
                .build();

        postRepository.save(post);
        postDocumentRepository.save(PostDocument.from(post));

    }

    public List<PostDocument> searchPost(String keyword) {
        return postDocumentRepository.findByContent(keyword);
    }
}
