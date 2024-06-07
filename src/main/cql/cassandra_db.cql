CREATE KEYSPACE climate_confort WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};

USE climate_confort;

CREATE TABLE Role (
    id              BIGINT PRIMARY KEY,
    type            INT
);

CREATE TABLE User (
    id              BIGINT PRIMARY KEY,
    email           TEXT,
    pasahitza       TEXT,
    role_id         BIGINT
);

CREATE TABLE Enpresa (
    id              BIGINT PRIMARY KEY,
    izena           TEXT,
    user_id         BIGINT,
);

CREATE TABLE Eraikina (
    id              BIGINT PRIMARY KEY,
    lokalizazioa    TEXT,
    enpresa_id      BIGINT
);

CREATE TABLE Gela (
    id              BIGINT PRIMARY KEY,
    eraikina_id     BIGINT
);

CREATE TABLE Parametroa (
    id              BIGINT PRIMARY KEY,
    mota            TEXT,
    balio_min       FLOAT,
    balio_max       FLOAT,
    gela_id         BIGINT
);

-- Insert Role
INSERT INTO role (id, type) VALUES (0, 1); 
INSERT INTO role (id, type) VALUES (1, 2);

-- Insert User
INSERT INTO User (id, email, pasahitza, role_id) VALUES (1, 'admin@climateconfort.com', '$2a$12$/B1jwyTObAv4aBRKLhCVvevvWpE819gwrzjxkuk8uvyLcp2PJjQVO', 1);
INSERT INTO User (id, email, pasahitza, role_id) VALUES (2, 'user@climateconfort.com', '$2a$12$KiMkin9zRzt83Y4AHTxiVOxVQu5PHPOVBKdfUaNBdVcD6hF8TQx6K', 2);
INSERT INTO User (id, email, pasahitza, role_id) VALUES (3, 'user2@climateconfort.com', '$2a$12$KiMkin9zRzt83Y4AHTxiVOxVQu5PHPOVBKdfUaNBdVcD6hF8TQx6K', 2);

-- Insert Enpresa
INSERT INTO Enpresa (id, izena, user_id) VALUES (1, 'ULMA', 2);
INSERT INTO Enpresa (id, izena, user_id) VALUES (2, 'FAGOR', 3);

-- Insert Eraikina
INSERT INTO Eraikina (id, lokalizazioa, enpresa_id) VALUES (1, 'Ubicación1', 1);
INSERT INTO Eraikina (id, lokalizazioa, enpresa_id) VALUES (2, 'Ubicación2', 1);
INSERT INTO Eraikina (id, lokalizazioa, enpresa_id) VALUES (3, 'Ubicación3', 2);
INSERT INTO Eraikina (id, lokalizazioa, enpresa_id) VALUES (4, 'Ubicación4', 2);

-- Insertar Gela
INSERT INTO Gela (id, eraikina_id) VALUES (1, 1);
INSERT INTO Gela (id, eraikina_id) VALUES (2, 1);
INSERT INTO Gela (id, eraikina_id) VALUES (3, 2);
INSERT INTO Gela (id, eraikina_id) VALUES (4, 2);
INSERT INTO Gela (id, eraikina_id) VALUES (5, 3);
INSERT INTO Gela (id, eraikina_id) VALUES (6, 3);
INSERT INTO Gela (id, eraikina_id) VALUES (7, 4);
INSERT INTO Gela (id, eraikina_id) VALUES (8, 4);

-- Insertar Parametroa
INSERT INTO Parametroa (id, mota, balio_min, balio_max, gela_id) VALUES (1, 'TMP', 15.3, 25.4, 1);
INSERT INTO Parametroa (id, mota, balio_min, balio_max, gela_id) VALUES (2, 'SND', 12.0, 27.4, 1);
INSERT INTO Parametroa (id, mota, balio_min, balio_max, gela_id) VALUES (3, 'TMP', 19.3, 25.5, 2);
INSERT INTO Parametroa (id, mota, balio_min, balio_max, gela_id) VALUES (4, 'HMD', 11.8, 22.3, 2);
INSERT INTO Parametroa (id, mota, balio_min, balio_max, gela_id) VALUES (5, 'TMP', 19.3, 25.5, 3);
INSERT INTO Parametroa (id, mota, balio_min, balio_max, gela_id) VALUES (6, 'PRS', 11.8, 22.3, 3);
INSERT INTO Parametroa (id, mota, balio_min, balio_max, gela_id) VALUES (7, 'TMP', 19.3, 25.5, 4);
INSERT INTO Parametroa (id, mota, balio_min, balio_max, gela_id) VALUES (8, 'PRS', 11.8, 22.3, 4);
INSERT INTO Parametroa (id, mota, balio_min, balio_max, gela_id) VALUES (9, 'TMP', 15.3, 25.4, 5);
INSERT INTO Parametroa (id, mota, balio_min, balio_max, gela_id) VALUES (10, 'SND', 12.0, 27.4, 5);
INSERT INTO Parametroa (id, mota, balio_min, balio_max, gela_id) VALUES (11, 'TMP', 19.3, 25.5, 6);
INSERT INTO Parametroa (id, mota, balio_min, balio_max, gela_id) VALUES (12, 'SND', 11.8, 22.3, 6);
INSERT INTO Parametroa (id, mota, balio_min, balio_max, gela_id) VALUES (13, 'TMP', 19.3, 25.5, 7);
INSERT INTO Parametroa (id, mota, balio_min, balio_max, gela_id) VALUES (14, 'TMP', 11.8, 22.3, 7);
INSERT INTO Parametroa (id, mota, balio_min, balio_max, gela_id) VALUES (15, 'TMP', 19.3, 25.5, 8);
INSERT INTO Parametroa (id, mota, balio_min, balio_max, gela_id) VALUES (16, 'PRS', 11.8, 22.3, 8);