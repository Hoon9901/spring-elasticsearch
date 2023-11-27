package hoon.study.springelastic.service;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import hoon.study.springelastic.entity.Post;
import hoon.study.springelastic.entity.PostDocument;
import hoon.study.springelastic.repository.PostDocumentRepository;
import hoon.study.springelastic.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final PostDocumentRepository postDocumentRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    @Autowired
    public PostService(PostRepository postRepository, PostDocumentRepository postDocumentRepository, ElasticsearchOperations elasticsearchOperations) {
        this.postRepository = postRepository;
        this.postDocumentRepository = postDocumentRepository;
        this.elasticsearchOperations = elasticsearchOperations;
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
        Query query = QueryBuilders.match(queryBuilder -> queryBuilder.field("content").query(keyword));
        NativeQuery nativeQuery = NativeQuery.builder().withQuery(query).build();
        SearchHits<PostDocument> result = elasticsearchOperations.search(nativeQuery, PostDocument.class);
        return result
                .stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());
    }
}
