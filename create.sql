DROP DATABASE IF EXISTS greet;
CREATE DATABASE greet;
USE greet;

CREATE TABLE user
(
    id       INT PRIMARY KEY NOT NULL UNIQUE AUTO_INCREMENT,
    email    VARCHAR(50)     NOT NULL UNIQUE,
    username VARCHAR(50)              UNIQUE,
    password VARCHAR(50)
);
