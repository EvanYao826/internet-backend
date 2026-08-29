-- H2（MySQL 模式）测试建表脚本，结构与 sql/init.sql 对齐
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    username    VARCHAR(50)  NOT NULL,
    nickname    VARCHAR(50)  NOT NULL,
    avatar      VARCHAR(255) NULL,
    email       VARCHAR(100) NULL DEFAULT '',
    phone       VARCHAR(20)  NULL DEFAULT '',
    password    VARCHAR(100) NOT NULL,
    dept_id     BIGINT       NULL,
    remark      VARCHAR(255) NULL DEFAULT '',
    status      TINYINT      NOT NULL DEFAULT 1,
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_username UNIQUE (username)
);

DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    role_name   VARCHAR(50)  NOT NULL,
    role_code   VARCHAR(50)  NOT NULL,
    sort        INT          NOT NULL DEFAULT 99,
    status      TINYINT      NOT NULL DEFAULT 1,
    remark      VARCHAR(255) NULL DEFAULT '',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_role_code UNIQUE (role_code)
);

DROP TABLE IF EXISTS sys_menu;
CREATE TABLE sys_menu (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    parent_id   BIGINT       NOT NULL DEFAULT 0,
    menu_name   VARCHAR(50)  NOT NULL,
    menu_type   TINYINT      NOT NULL,
    path        VARCHAR(200) NULL,
    component   VARCHAR(200) NULL,
    icon        VARCHAR(50)  NULL,
    permission  VARCHAR(100) NULL,
    sort        INT          NOT NULL DEFAULT 99,
    status      TINYINT      NOT NULL DEFAULT 1,
    visible     TINYINT      NOT NULL DEFAULT 1,
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

DROP TABLE IF EXISTS sys_user_role;
CREATE TABLE sys_user_role (
    id      BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_user_role UNIQUE (user_id, role_id)
);

DROP TABLE IF EXISTS sys_role_menu;
CREATE TABLE sys_role_menu (
    id      BIGINT NOT NULL AUTO_INCREMENT,
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_role_menu UNIQUE (role_id, menu_id)
);

DROP TABLE IF EXISTS sys_operation_log;
CREATE TABLE sys_operation_log (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    type        VARCHAR(20)  NOT NULL,
    content     VARCHAR(255) NOT NULL,
    operator    VARCHAR(50)  NOT NULL,
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);
