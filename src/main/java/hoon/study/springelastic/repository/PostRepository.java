package hoon.study.springelastic.repository;

import hoon.study.springelastic.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {

}
