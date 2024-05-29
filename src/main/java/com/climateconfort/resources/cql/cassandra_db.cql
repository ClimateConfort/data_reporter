
CREATE KEYSPACE climate_confort
WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};

USE climate_confort;

CREATE TABLE Role (
    id              UUID PRIMARY KEY,
    type            INT
);

CREATE TABLE User (
    id              UUID PRIMARY KEY,
    email           TEXT,
    pasahitza       TEXT,
    role_id         UUID,
    FOREIGN KEY (role_id) REFERENCES 'Role' (id)
);


CREATE TABLE Enpresa (
    id              UUID PRIMARY KEY,
    izena           TEXT,
    user_id         UUID,
    FOREIGN KEY (user_id) REFERENCES User (id)
);

CREATE TABLE Eraikina (
    id              UUID PRIMARY KEY,
    lokalizazioa    TEXT,
    enpresa_id      UUID,
    FOREIGN KEY (enpresa_id) REFERENCES Enpresa (id)
);

CREATE TABLE Gela (
    id              UUID PRIMARY KEY,
    eraikina_id     UUID,
    FOREIGN KEY (eraikina_id) REFERENCES Eraikina (id)
);

CREATE TABLE Parametroa (
    id              UUID PRIMARY KEY,
    mota            TEXT,
    balio_min       FLOAT,
    balio_max       FLOAT,
    FOREIGN KEY (gela_id) REFERENCES Gela (id)
);

-- Insert Role
INSERT INTO role (id, type) VALUES (0, 1), (1, 2);

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
INSERT INTO Gela (id, eraikina_id) VALUES (1, 15.0, 25.0, 1);
INSERT INTO Gela (id, eraikina_id) VALUES (2, 16.0, 26.0, 1);
INSERT INTO Gela (id, eraikina_id) VALUES (3, 17.0, 27.0, 2);
INSERT INTO Gela (id, eraikina_id) VALUES (4, 18.0, 28.0, 2);
INSERT INTO Gela (id, eraikina_id) VALUES (5, 19.0, 29.0, 3);
INSERT INTO Gela (id, eraikina_id) VALUES (6, 20.0, 30.0, 3);
INSERT INTO Gela (id, eraikina_id) VALUES (7, 21.0, 31.0, 4);
INSERT INTO Gela (id, eraikina_id) VALUES (8, 22.0, 32.0, 4);

-- Insertar Parametroa
INSERT INTO Parametroa (id, mota, balio_min, balio_max, gela_id) VALUES (uuid(), 'TMP', 15.3, 25.4, 1);
INSERT INTO Parametroa (id, mota, balio_min, balio_max, gela_id) VALUES (uuid(), 'AIR', 12.0, 27.4, 1);
INSERT INTO Parametroa (id, mota, balio_min, balio_max, gela_id) VALUES (uuid(), 'TMP', 19.3, 25.5, 2);
INSERT INTO Parametroa (id, mota, balio_min, balio_max, gela_id) VALUES (uuid(), 'AIR', 11.8, 22.3, 2);
INSERT INTO Parametroa (id, mota, balio_min, balio_max, gela_id) VALUES (uuid(), 'TMP', 19.3, 25.5, 3);
INSERT INTO Parametroa (id, mota, balio_min, balio_max, gela_id) VALUES (uuid(), 'AIR', 11.8, 22.3, 3);
INSERT INTO Parametroa (id, mota, balio_min, balio_max, gela_id) VALUES (uuid(), 'TMP', 19.3, 25.5, 4);
INSERT INTO Parametroa (id, mota, balio_min, balio_max, gela_id) VALUES (uuid(), 'AIR', 11.8, 22.3, 4);
INSERT INTO Parametroa (id, mota, balio_min, balio_max, gela_id) VALUES (uuid(), 'TMP', 15.3, 25.4, 5);
INSERT INTO Parametroa (id, mota, balio_min, balio_max, gela_id) VALUES (uuid(), 'AIR', 12.0, 27.4, 5);
INSERT INTO Parametroa (id, mota, balio_min, balio_max, gela_id) VALUES (uuid(), 'TMP', 19.3, 25.5, 6);
INSERT INTO Parametroa (id, mota, balio_min, balio_max, gela_id) VALUES (uuid(), 'AIR', 11.8, 22.3, 6);
INSERT INTO Parametroa (id, mota, balio_min, balio_max, gela_id) VALUES (uuid(), 'TMP', 19.3, 25.5, 7);
INSERT INTO Parametroa (id, mota, balio_min, balio_max, gela_id) VALUES (uuid(), 'AIR', 11.8, 22.3, 7);
INSERT INTO Parametroa (id, mota, balio_min, balio_max, gela_id) VALUES (uuid(), 'TMP', 19.3, 25.5, 8);
INSERT INTO Parametroa (id, mota, balio_min, balio_max, gela_id) VALUES (uuid(), 'AIR', 11.8, 22.3, 8);