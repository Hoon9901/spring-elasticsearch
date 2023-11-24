package hoon.study.springelastic.entity;

import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

@Document(indexName = "artscope-test-post")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostDocument {

    @Id
    @Field(type = FieldType.Long)
    private Long id;

    @Field(type = FieldType.Text)
    private String content;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime createdTime;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime updatedTime;

    public static PostDocument from (Post post) {
        return PostDocument.builder()
                .id(post.getId())
                .content(post.getContent())
                .createdTime(post.getCreatedAt())
                .updatedTime(post.getUpdatedAt())
                .build();
    }

}
