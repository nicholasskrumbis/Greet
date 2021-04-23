DROP DATABASE IF EXISTS greet;
CREATE DATABASE greet;
USE greet;

CREATE TABLE user
(
    id       INT PRIMARY KEY NOT NULL UNIQUE AUTO_INCREMENT,
    email    VARCHAR(50)     NOT NULL UNIQUE,
    fname    VARCHAR(50)     NOT NULL,
    lname    VARCHAR(50)     NOT NULL,
    password VARCHAR(50)
);

CREATE TABLE event
(
    id          INT PRIMARY KEY NOT NULL UNIQUE AUTO_INCREMENT,
    host_id     INT             NOT NULL,
    date        VARCHAR(10)     NOT NULL,
    time        VARCHAR(10)     NOT NULL,
    location    VARCHAR(10)     NOT NULL,
    description VARCHAR(10)     NOT NULL,
    FOREIGN KEY (host_id) REFERENCES user (id)
);

CREATE TABLE attendance
(
    id         INT PRIMARY KEY NOT NULL UNIQUE AUTO_INCREMENT,
    event_id   INT             NOT NULL,
    qr_id      INT             NULL,
    fname      VARCHAR(50)     NULL,
    lname      VARCHAR(50)     NULL,
    email      VARCHAR(50)     NOT NULL,
    status     TINYINT         NOT NULL DEFAULT 0,
    attendance TINYINT         NOT NULL DEFAULT 0,
    FOREIGN KEY (event_id) REFERENCES event (id)
);