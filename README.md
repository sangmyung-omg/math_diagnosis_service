# WaplMath-SAP-Backend
- 와플 수학 학생 분석 플랫폼 (SAP) 백엔드 서비스 프로젝트
- Problem / Student Modeling / Recommend
- [Redmine](http://192.168.154.140:3001/projects/waplmath_sap/)

## 유의사항
### DB
- 개발 중 DB 생성 및 테스트는 로컬 tibero에 계정 생성 후 연결해서 사용해주세요.
  + Tibero 계정 ID/PW 는 waplmath/waplmath 로 일단 통일.
  + application.properties 파일 내 spring.datasource.url의 ip를 로컬 tibero ip로 변경.
  + application.properties 는 commit 제외.
  
*5/20* : Spring Boot에서 "DROP IF EXISTS" 적용 불가 (ORACLE의 PL/SQL이 schema.sql에서 적용 안됨)
  + 첫 서버 구동 시 schema.sql 적용 / 이후부터는 schema.sql의 맨 처음 주석 부분 (DROP문) 주석 해제하면 schema.sql 수정 및 반영 가능.
  
### Lombok 설치
- [lombok 설치하기](https://the-dev.tistory.com/27) 하단 "다운로드 한 Lombok 설치하기" 부터 참고



*5/20* : Spring Boot에서 "DROP IF EXISTS" 적용 불가 (ORACLE의 PL/SQL이 schema.sql에서 적용 안됨)
  + 첫 서버 구동 시 schema.sql 적용 / 이후부터는 schema.sql의 맨 처음 주석 부분 (DROP문) 주석 해제하면 schema.sql 수정 및 반영 가능.


## DB 생성 및 initialize
- src/main/resources 폴더 내로 sql 파일 위치
  + schema.sql = DB 테이블 생성
  + data.sql = DB 데이터 초기화

- src/main/resources/application.properties 파일의 'spring.datasource.initialization-mode' 를 never -> always 로 변경 및 저장
- Run (or 이미 Run 상태이면 자동으로 리부트 됨)

  
## Configuration
- [Spring Initilaizer](https://start.spring.io/) 를 이용한 초기 프로젝트 생성 옵션

### Environments
- language : java
- framework : Spring boot
- DB : Tibero
- data access : jpa

### Versions
- java : 1.8.0_241
- Spring : 2.4.5
- Gradle : 6.8.3
- Tibero : tibero6-jdbc

### Dependencies
- Spring Boot DevTools
- Spring Web
- Lombok
- Spring Data JPA
- Oracle Driver

