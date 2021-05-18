# Recommend Demo Backend

- 와플 수학 문제 추천 데모를 위한 백엔드 서비스 프로젝트
- [API 리스트 및 명세](https://docs.google.com/spreadsheets/d/13y4vFdhUcUabgrKaNC15KFTJS0ZJw_uyagDbU_u_2yU/edit?usp=sharing)
- 문제 추천 데모 프론트엔드 [프로젝트](http://gitlab.tmax-work.shop/ae1-3/recommend/recommend-demo-frontend)

## Configuration
- [Spring Initilaizer](https://start.spring.io/) 를 이용한 초기 프로젝트 생성 옵션

### Project
- Spring Boot (2.4.5)
- Gradle
- Packaging = Jar
- Java = 8

### Dependencies
- Spring Web
- Spring Boot DevTools
- Lombok
- Spring Data JPA
- MySQL Server

## DB 생성 및 Initialize
- src/main/resources 폴더 내로 sql 파일 위치
  - schema.sql = DB 테이블 생성
  - data.sql = DB 데이터 초기화

- src/main/resources/application.properties 파일의 'spring.datasource.initialization-mode' 를 never -> always 로 변경 및 저장
- Run (or 이미 Run 상태이면 자동으로 리부트 됨)
