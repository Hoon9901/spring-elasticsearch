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

```