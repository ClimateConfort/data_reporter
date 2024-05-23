
CREATE KEYSPACE climate_confort
WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};

USE climate_confort;

CREATE TABLE Role (
    id UUID,
    type TEXT
);

CREATE TABLE User (
    id UUID PRIMARY KEY,
    email TEXT,
    pasahitza TEXT,
    role_id UUID,
    FOREIGN KEY (role_id) REFERENCES Role (id)
);

CREATE TABLE Enpresa (
    id UUID PRIMARY KEY,
    izena TEXT,
    user_id UUID,
    FOREIGN KEY (user_id) REFERENCES User (id)
);

CREATE TABLE Eraikina (
    id UUID PRIMARY KEY,
    lokalizazioa TEXT,
    enpresa_id UUID,
    FOREIGN KEY (enpresa_id) REFERENCES Enpresa (id)
);

CREATE TABLE Gela (
    id UUID PRIMARY KEY,
    parametro_min FLOAT,
    parametro_max FLOAT,
    eraikina_id UUID,
    FOREIGN KEY (eraikina_id) REFERENCES Eraikina (id)
);

-- Insertar datos en la tabla Role
INSERT INTO role (id, type) VALUES (0, 1), (1, 2);

-- Insertar datos en la tabla User
INSERT INTO User (id, email, pasahitza, role_id) VALUES (1, 'user@climateconfort.com', '$2a$12$KiMkin9zRzt83Y4AHTxiVOxVQu5PHPOVBKdfUaNBdVcD6hF8TQx6K', 2);
INSERT INTO User (id, email, pasahitza, role_id) VALUES (2, 'user2@climateconfort.com', '$2a$12$KiMkin9zRzt83Y4AHTxiVOxVQu5PHPOVBKdfUaNBdVcD6hF8TQx6K', 2);
INSERT INTO User (id, email, pasahitza, role_id) VALUES (3, 'admin@climateconfort.com', '$2a$12$/B1jwyTObAv4aBRKLhCVvevvWpE819gwrzjxkuk8uvyLcp2PJjQVO', 1);

-- Insertar datos en la tabla Enpresa
INSERT INTO Enpresa (id, izena, user_id) VALUES (1, 'Empresa1', 1);
INSERT INTO Enpresa (id, izena, user_id) VALUES (2, 'Empresa2', 2);

-- Insertar datos en la tabla Eraikina
INSERT INTO Eraikina (id, lokalizazioa, enpresa_id) VALUES (1, 'Ubicación1', 1);
INSERT INTO Eraikina (id, lokalizazioa, enpresa_id) VALUES (2, 'Ubicación2', 1);
INSERT INTO Eraikina (id, lokalizazioa, enpresa_id) VALUES (3, 'Ubicación3', 2);
INSERT INTO Eraikina (id, lokalizazioa, enpresa_id) VALUES (4, 'Ubicación4', 2);

-- Insertar datos en la tabla Gela
INSERT INTO Gela (id, parametro_min, parametro_max, eraikina_id) VALUES (uuid(), 15.0, 25.0, 1);
INSERT INTO Gela (id, parametro_min, parametro_max, eraikina_id) VALUES (uuid(), 16.0, 26.0, 1);
INSERT INTO Gela (id, parametro_min, parametro_max, eraikina_id) VALUES (uuid(), 17.0, 27.0, 2);
INSERT INTO Gela (id, parametro_min, parametro_max, eraikina_id) VALUES (uuid(), 18.0, 28.0, 2);
INSERT INTO Gela (id, parametro_min, parametro_max, eraikina_id) VALUES (uuid(), 19.0, 29.0, 3);
INSERT INTO Gela (id, parametro_min, parametro_max, eraikina_id) VALUES (uuid(), 20.0, 30.0, 3);
INSERT INTO Gela (id, parametro_min, parametro_max, eraikina_id) VALUES (uuid(), 21.0, 31.0, 4);
INSERT INTO Gela (id, parametro_min, parametro_max, eraikina_id) VALUES (uuid(), 22.0, 32.0, 4);