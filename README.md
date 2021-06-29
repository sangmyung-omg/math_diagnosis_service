# WaplMath-SAP-Backend

- 와플 수학 학생 분석 플랫폼 (SAP) 백엔드 서비스 프로젝트
- Mastery / AnalysisReport / Recommend / Problem
- [Redmine](http://192.168.154.140:3001/projects/waplmath_sap/)
- API docs
  - [AnalysisReport](http://gitlab.tmax-work.shop/ae1-3/api_temp.git)
  - [Mastery, Recommend, etc](http://gitlab.tmax-work.shop/ae1-3/api_temp.git)
- (21.06.23) ae1-3/dev 브랜치가 최신

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

## 유의사항

### DB

- AE 1-2팀 Tibero로 설정 되어있음.
  - application.properties 파일 내 주석 처리된 spring.datasource.initialization-mode=never 부분 수정 금지.
- 로컬 Tibero 설정 하고 싶으면 (권장 X)
  - application.properties 파일 내 spring.datasource.url의 ip를 로컬 tibero ip로 변경.
  - src/main/resources 폴더 내로 sql 파일 위치
    - schema.sql = DB 테이블 생성
    - data.sql = DB 데이터 초기화
    - (21.06.23) ddl, 초기화 스크립트 최신버전 업데이트 아직 안됨.
  - IP 변경됐는지 확인 후, 첫 서버 구동 시 schema.sql 적용 / 이후부터는 schema.sql의 맨 처음 주석 부분 (DROP문) 주석 해제하면 schema.sql 수정 및 반영 가능.
  - src/main/resources/application.properties 파일의 'spring.datasource.initialization-mode' 를 never -> always 로 변경 및 저장
  - application.properties 는 commit 제외.

### Lombok 설치

- [lombok 설치하기](https://the-dev.tistory.com/27) 하단 "다운로드 한 Lombok 설치하기" 부터 참고
