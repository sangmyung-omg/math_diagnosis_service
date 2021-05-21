DROP TABLE DIAGNOSIS_PROBLEM;
DROP TABLE PROBLEM_IMAGE;
DROP TABLE PROBLEM_UK_REL;
DROP TABLE USER_EMBEDDING;
DROP TABLE USER_KNOWLEDGE;
DROP TABLE EXAM_CARD_PROBLEM;
DROP TABLE USER_EXAM_CARD_HISTORY;
DROP TABLE USER_MASTER;
DROP TABLE UK_REL;
DROP TABLE UK_MASTER;
DROP TABLE PROBLEM;
DROP TABLE PROBLEM_TYPE_MASTER;
DROP TABLE CURRICULUM_MASTER;

-- 테이블 순서는 관계를 고려하여 한 번에 실행해도 에러가 발생하지 않게 정렬되었습니다.

-- CURRICULUM_MASTER Table Create SQL
CREATE TABLE CURRICULUM_MASTER(
    CURRICULUM_ID          VARCHAR2(32)     NOT NULL, 
    SCHOOL_TYPE            VARCHAR2(20)     NULL, 
    GRADE                  VARCHAR2(20)     NULL, 
    SEMESTER               VARCHAR2(20)     NULL, 
    CHAPTER                VARCHAR2(100)    NULL, 
    SECTION                VARCHAR2(100)    NULL, 
    SUB_SECTION            VARCHAR2(100)    NULL, 
    PART                   VARCHAR2(20)     NULL, 
    CURRICULUM_SEQUENCE    NUMBER           NULL, 
    CONSTRAINT PK_CURRICULUM_MASTER PRIMARY KEY (CURRICULUM_ID)
);



-- CURRICULUM_MASTER Table Create SQL
CREATE TABLE PROBLEM_TYPE_MASTER(
    TYPE_ID          NUMBER           NOT NULL, 
    TYPE_NAME        VARCHAR2(200)    NOT NULL, 
    SEQUENCE         NUMBER           NULL, 
    CURRICULUM_ID    VARCHAR2(32)     NULL, 
    CONSTRAINT PK_PROBLEM_TYPE_MASTER PRIMARY KEY (TYPE_ID)
);


ALTER TABLE PROBLEM_TYPE_MASTER
    ADD CONSTRAINT FK_PROBELM_TYPE_MASTER_CURRICU FOREIGN KEY (CURRICULUM_ID)
        REFERENCES CURRICULUM_MASTER (CURRICULUM_ID);



-- CURRICULUM_MASTER Table Create SQL
CREATE TABLE PROBLEM(
    PROB_ID                NUMBER             NOT NULL, 
    TYPE_ID                NUMBER             NOT NULL, 
    ANSWER_TYPE            VARCHAR2(20)       NOT NULL, 
    LEARNING_DOMAIN        VARCHAR2(20)       NULL, 
    QUESTION               VARCHAR2(40000)    NOT NULL, 
    SOLUTION               VARCHAR2(40000)    NOT NULL, 
    SOURCE                 VARCHAR2(60)       NULL, 
    CORRECT_RATE           FLOAT              NULL, 
    DIFFICULTY             VARCHAR2(20)       NULL, 
    CREATOR_ID             VARCHAR2(32)       NOT NULL, 
    CREATE_DATE            TIMESTAMP          NOT NULL, 
    EDITOR_ID              VARCHAR2(32)       NULL, 
    EDIT_DATE              TIMESTAMP          NULL, 
    VALIDATOR_ID           VARCHAR2(32)       NULL, 
    VALIDATE_DATE          TIMESTAMP          NULL, 
    STATUS                 VARCHAR2(20)       NULL, 
    TIME_RECOMMENDATION    FLOAT              NULL, 
    FREQUENT               VARCHAR2(10)       NULL, 
    CATEGORY               VARCHAR2(20)       NULL, 
    CONSTRAINT PK_PROBLEM PRIMARY KEY (PROB_ID)
);


ALTER TABLE PROBLEM
    ADD CONSTRAINT FK_PROBLEM_TYPE_ID_PROBELM_TYP FOREIGN KEY (TYPE_ID)
        REFERENCES PROBLEM_TYPE_MASTER (TYPE_ID);



-- CURRICULUM_MASTER Table Create SQL
CREATE TABLE UK_MASTER(
    UK_ID             NUMBER            NOT NULL, 
    UK_NAME           VARCHAR2(200)     NOT NULL, 
    UK_DESCRIPTION    VARCHAR2(1000)    NULL, 
    TRAIN_UNSEEN      CHAR(1)           NOT NULL, 
    CURRICULUM_ID     VARCHAR2(32)      NULL, 
    CONSTRAINT PK_UK_MASTER PRIMARY KEY (UK_ID)
);


ALTER TABLE UK_MASTER
    ADD CONSTRAINT FK_UK_MASTER_CURRICULUM_ID_CUR FOREIGN KEY (CURRICULUM_ID)
        REFERENCES CURRICULUM_MASTER (CURRICULUM_ID);



-- CURRICULUM_MASTER Table Create SQL
CREATE TABLE USER_MASTER(
    USER_UUID                VARCHAR2(32)    NOT NULL, 
    GRADE                    VARCHAR2(20)    NULL, 
    SEMESTER                 VARCHAR2(20)    NULL, 
    NAME                     VARCHAR(100)    NULL, 
    CURRENT_CURRICULUM_ID    VARCHAR2(32)    NULL, 
    EXAM_TYPE                VARCHAR2(32)    NULL, 
    EXAM_START_DATE          TIMESTAMP       NULL, 
    EXAM_DUE_DATE            TIMESTAMP       NULL, 
    EXAM_TARGET_SCORE        INT             NULL, 
    CONSTRAINT PK_USER_MASTER PRIMARY KEY (USER_UUID)
);



-- CURRICULUM_MASTER Table Create SQL
CREATE TABLE USER_EXAM_CARD_HISTORY(
    CARD_ID          VARCHAR2(32)    NOT NULL, 
    USER_UUID        VARCHAR2(32)    NOT NULL, 
    CARD_TYPE        VARCHAR2(32)    NOT NULL, 
    CARD_TITLE       VARCHAR2(32)    NULL, 
    SECTION_ID       VARCHAR2(32)    NULL, 
    TYPE_ID          NUMBER          NULL, 
    CARD_SEQUENCE    INT             NOT NULL, 
    CONSTRAINT PK_USER_EXAM_CARD_HISTORY PRIMARY KEY (CARD_ID)
);

ALTER TABLE USER_EXAM_CARD_HISTORY
    ADD CONSTRAINT FK_USER_EXAM_CARD_HISTORY_USER FOREIGN KEY (USER_UUID)
        REFERENCES USER_MASTER (USER_UUID);


ALTER TABLE USER_EXAM_CARD_HISTORY
    ADD CONSTRAINT FK_USER_EXAM_CARD_HISTORY_TYPE FOREIGN KEY (TYPE_ID)
        REFERENCES PROBLEM_TYPE_MASTER (TYPE_ID);


ALTER TABLE USER_EXAM_CARD_HISTORY
    ADD CONSTRAINT FK_USER_EXAM_CARD_HISTORY_SECT FOREIGN KEY (SECTION_ID)
        REFERENCES CURRICULUM_MASTER (CURRICULUM_ID);



-- CURRICULUM_MASTER Table Create SQL
CREATE TABLE UK_REL(
    BASE_UK_ID            NUMBER          NOT NULL, 
    PRE_UK_ID             NUMBER          NOT NULL, 
    RELATION_REFERENCE    VARCHAR2(32)    NULL   
);


ALTER TABLE UK_REL
    ADD CONSTRAINT FK_UK_REL_BASE_UK_ID_UK_MASTER FOREIGN KEY (BASE_UK_ID)
        REFERENCES UK_MASTER (UK_ID);


ALTER TABLE UK_REL
    ADD CONSTRAINT FK_UK_REL_PRE_UK_ID_UK_MASTER_ FOREIGN KEY (PRE_UK_ID)
        REFERENCES UK_MASTER (UK_ID);



-- CURRICULUM_MASTER Table Create SQL
CREATE TABLE USER_KNOWLEDGE(
    USER_UUID      VARCHAR2(32)    NOT NULL, 
    UK_ID          NUMBER          NOT NULL, 
    UK_MASTERY     FLOAT           NULL, 
    UPDATE_DATE    TIMESTAMP       NULL, 
    CONSTRAINT PK_USER_KNOWLEDGE PRIMARY KEY (USER_UUID, UK_ID)
);


ALTER TABLE USER_KNOWLEDGE
    ADD CONSTRAINT FK_USER_KNOWLEDGE_UK_ID_UK_MAS FOREIGN KEY (UK_ID)
        REFERENCES UK_MASTER (UK_ID);


ALTER TABLE USER_KNOWLEDGE
    ADD CONSTRAINT FK_USER_KNOWLEDGE_USER_UUID_US FOREIGN KEY (USER_UUID)
        REFERENCES USER_MASTER (USER_UUID);



-- CURRICULUM_MASTER Table Create SQL
CREATE TABLE USER_EMBEDDING(
    USER_UUID         VARCHAR2(32)    NOT NULL, 
    USER_EMBEDDING    LONG            NULL, 
    UPDATE_DATE       TIMESTAMP       NULL, 
    CONSTRAINT PK_USER_EMBEDDING PRIMARY KEY (USER_UUID)
);

ALTER TABLE USER_EMBEDDING
    ADD CONSTRAINT FK_USER_EMBEDDING_USER_UUID_US FOREIGN KEY (USER_UUID)
        REFERENCES USER_MASTER (USER_UUID);



-- CURRICULUM_MASTER Table Create SQL
CREATE TABLE EXAM_CARD_PROBLEM(
    CARD_ID          VARCHAR2(32)    NOT NULL, 
    PROB_ID          NUMBER          NOT NULL, 
    PROB_SEQUENCE    INT             NOT NULL
);


ALTER TABLE EXAM_CARD_PROBLEM
    ADD CONSTRAINT FK_EXAM_CARD_PROBLEM_CARD_ID_U FOREIGN KEY (CARD_ID)
        REFERENCES USER_EXAM_CARD_HISTORY (CARD_ID);


ALTER TABLE EXAM_CARD_PROBLEM
    ADD CONSTRAINT FK_EXAM_CARD_PROBLEM_PROB_ID_P FOREIGN KEY (PROB_ID)
        REFERENCES PROBLEM (PROB_ID);



-- CURRICULUM_MASTER Table Create SQL
CREATE TABLE PROBLEM_IMAGE(
    PROB_ID    NUMBER           NULL, 
    SRC        VARCHAR2(256)    NOT NULL
);


ALTER TABLE PROBLEM_IMAGE
    ADD CONSTRAINT FK_PROBLEM_IMAGE_PROB_ID_PROBL FOREIGN KEY (PROB_ID)
        REFERENCES PROBLEM (PROB_ID);



-- CURRICULUM_MASTER Table Create SQL
CREATE TABLE PROBLEM_UK_REL(
    PROB_ID    NUMBER    NOT NULL, 
    UK_ID      NUMBER    NOT NULL
);


ALTER TABLE PROBLEM_UK_REL
    ADD CONSTRAINT FK_PROBLEM_UK_REL_PROB_ID_PROB FOREIGN KEY (PROB_ID)
        REFERENCES PROBLEM (PROB_ID);


ALTER TABLE PROBLEM_UK_REL
    ADD CONSTRAINT FK_PROBLEM_UK_REL_UK_ID_UK_MAS FOREIGN KEY (UK_ID)
        REFERENCES UK_MASTER (UK_ID);



-- CURRICULUM_MASTER Table Create SQL
CREATE TABLE DIAGNOSIS_PROBLEM(
    DIAGNOSIS_PROB_ID    NUMBER    NOT NULL, 
    BASIC_PROB_ID        NUMBER    NOT NULL, 
    UPPER_PROB_ID        NUMBER    NOT NULL, 
    LOWER_PROB_ID        NUMBER    NOT NULL, 
    CONSTRAINT PK_DIAGNOSIS_PROBLEM PRIMARY KEY (DIAGNOSIS_PROB_ID)
);


ALTER TABLE DIAGNOSIS_PROBLEM
    ADD CONSTRAINT FK_DIAGNOSIS_PROBLEM_BASIC_PRO FOREIGN KEY (BASIC_PROB_ID)
        REFERENCES PROBLEM (PROB_ID);


ALTER TABLE DIAGNOSIS_PROBLEM
    ADD CONSTRAINT FK_DIAGNOSIS_PROBLEM_UPPER_PRO FOREIGN KEY (UPPER_PROB_ID)
        REFERENCES PROBLEM (PROB_ID);


ALTER TABLE DIAGNOSIS_PROBLEM
    ADD CONSTRAINT FK_DIAGNOSIS_PROBLEM_LOWER_PRO FOREIGN KEY (LOWER_PROB_ID)
        REFERENCES PROBLEM (PROB_ID);



