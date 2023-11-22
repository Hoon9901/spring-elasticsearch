# spring-elasticsearch
Spring Boot 3.1.X + ElasticSearch 8.11.X


## Prerequsite
- JDK 17 (Java 17)

## 0. Project 구성
- Spring Boot 3.1.5
- Spring Data JPA
- Spring Data Elasticsearch
- Spring Boot DevTools
- Spring Configuration Processor
- H2 Database
- Lombok

## 0. application.yaml 생성
```yaml

elasticsearch:
  host: ${ELASTICSEARCH_HOST:localhost}
  username: ${ELASTICSEARCH_USERNAME:elastic}
  password: ${ELASTICSEARCH_PASSWORD:changeme}

spring:
  h2:
    console:
      enabled: true
      path: /h2-console

  datasource:
    url: jdbc:h2:mem:testdb
    username: sa
    password:
    driver-class-name: org.h2.Driver

  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    format-sql: true

logging:
  level:
    org.hibernate.type.descriptor.sql: trace
```

elastic search host, username, password는 환경변수로 설정할 수 있도록 환경을 설정하거나 
application.yaml 파일에 직접 설정하면 됩니다.

## 1. ElasticSearch Client Configuration

```java
@EnableElasticsearchRepositories
public class ElasticSearchConfig extends ElasticsearchConfiguration {

    @Value("${elasticsearch.host}")
    private String host;

    @Value("${elasticsearch.username}")
    private String username;

    @Value("${elasticsearch.password}")
    private String password;

    @Override
    public ClientConfiguration clientConfiguration() {
        return ClientConfiguration.builder()
                .connectedTo(host)
                .usingSsl() // ssl 사용
                .withBasicAuth(username, password)
                .build();
    }
}
```


## 2. ElasticSearch @Entity 와 @Document

### 2.2 @Entity와 @Document를 함께 사용하는 경우

Post Entity에서 @Document를 부착해도 됩니다.
하지만 Repository를 사용할 때 문제가 생기는데 이는 @EnableJpaRepositories와 @EnableElasticsearchRepositories가 충돌이 발생하기 때문
따라서 JPA와 ElasticSearch를 분리해서 사용해야 합니다. 하지만 동시에 사용할려고 하면은 하드코딩을 통해서 일일이 설정을 해줘야 합니다.
이는 Repository를 추가할 때 마다 설정을 해줘야 하기 때문에 번거로운 작업이 됩니다.

따라서 @Entity와 @Document를 분리해서 사용하는 것이 좋습니다.

### 2.1 @Entity와 @Document를 분리한 경우

Post Entity와 Post Document를 분리해서 사용합니다.

```java
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String content;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
```

```java
@Document(indexName = "post")
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
    private String title;

    @Field(type = FieldType.Text)
    private String content;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime createdAt;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime updatedAt;

    public static PostDocument from (Post post) {
        return PostDocument.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }

}
```

LocalDateTime을 ElasticSearch에 저장할 때는 format을 지정해줘야 합니다. 
따라서 @Field의 format을 지정하였습니다.

## 3. ElasticSearch 저장 및 검색 기능 구현

```java
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
```

2개의 API를 구현했습니다.

- POST /api/post
- GET /api/post/search

```java
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
        return postDocumentRepository.findByTitleOrContent(keyword, keyword);
    }
}
```

PostService에서는 PostRepository와 PostDocumentRepository를 사용합니다.
기본적인 Entity와 Document를 저장하는 기능은 PostRepository와 PostDocumentRepository에서 구현합니다.

```java
public interface PostDocumentRepository extends ElasticsearchRepository<PostDocument, Long> {
    List<PostDocument> findByTitleOrContent(String title, String content);
}


public interface PostRepository extends JpaRepository<Post, Long> {

}
```

## 4. 테스트

### 4.1 Post 생성

API 호출
```
http://localhost:8080/api/post?title=제목&content=내용
```

### 4.2 Post 검색

API 호출
```
http://localhost:8080/api/post/search?keyword=제목
```

API 호출 결과
```json
[
    {
        "id": 2,
        "title": "제목",
        "content": "내용",
        "createdAt": "2023-11-22T22:32:35",
        "updatedAt": null
    },
    {
        "id": 3,
        "title": "제목",
        "content": "내용",
        "createdAt": "2023-11-22T22:32:41",
        "updatedAt": null
    },
    {
        "id": 4,
        "title": "제목",
        "content": "내용",
        "createdAt": "2023-11-22T22:32:42",
        "updatedAt": null
    }
]
```

## SSL 관련 Connection Refused 문제가 발생할 시

```java
    @Override
    public ClientConfiguration clientConfiguration() {
        return ClientConfiguration.builder()
                .connectedTo(host)
                .usingSsl(disableSslVerification(), allHostsValid())
                .withBasicAuth(username, password)
                .build();
    }

    public static SSLContext disableSslVerification() {
        try {
            // ============================================
            // trust manager 생성(인증서 체크 전부 안함)
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }};

            // trust manager 설치
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            return sc;
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static HostnameVerifier allHostsValid() {

        // ============================================
        // host name verifier 생성(호스트 네임 체크안함)
        HostnameVerifier allHostsValid = (hostname, session) -> true;

        // host name verifier 설치
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        return allHostsValid;

    }
```

SSL 인증서가 없거나 사설 인증서로 통신하는 경우 Connection Refused 문제가 발생할 수 있습니다.
그래서 위 방법을 통해서 SSL 인증서를 체크하지 않도록 설정하면 됩니다.
ElasticSearch ClientConfiguration usingSsl() 메서드에 SSLContext와 HostNameVerifier를 설정하면 됩니다.


## 레퍼런스

- [Spring Data Elasticsearch 5.2.0 공식 문서](https://docs.spring.io/spring-data/elasticsearch/reference/elasticsearch.html)
- [SSL 인증서](https://www.skyer9.pe.kr/wordpress/?p=7544)