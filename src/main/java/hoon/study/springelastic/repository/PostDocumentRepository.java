package hoon.study.springelastic.repository;

import hoon.study.springelastic.entity.Post;
import hoon.study.springelastic.entity.PostDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface PostDocumentRepository extends ElasticsearchRepository<PostDocument, Long> {
    List<PostDocument> findByTitleOrContent(String title, String content);
}
