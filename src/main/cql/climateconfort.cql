CREATE KEYSPACE climateconfort WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};

USE climateconfort;

CREATE TABLE Enpresa (
    enpresa_id      BIGINT,
    izena           TEXT,
    PRIMARY KEY (enpresa_id)
);

CREATE TABLE Eraikina (
    enpresa_id      BIGINT,
    eraikina_id     BIGINT,
    lokalizazioa    TEXT,
    PRIMARY KEY ((enpresa_id, eraikina_id))
);

CREATE TABLE Gela (
    enpresa_id      BIGINT,
    eraikina_id     BIGINT,
    gela_id         BIGINT,
    PRIMARY KEY ((enpresa_id, eraikina_id, gela_id))
);

CREATE TABLE Parametroa (
    enpresa_id      BIGINT,
    eraikina_id     BIGINT,
    gela_id         BIGINT,
    parametroa_id   BIGINT,
    mota            TEXT,
    minimoa_du      BOOLEAN,
    balio_min       FLOAT,
    balio_max       FLOAT,
    PRIMARY KEY ((enpresa_id, eraikina_id, gela_id, parametroa_id))
);

CREATE TABLE Erreportea (
    enpresa_id          BIGINT,
    eraikina_id         BIGINT,
    gela_id             BIGINT,
    data                DATE,
    tenperatura_media   FLOAT,
    soinu_media         FLOAT,
    hezetasuna_media    FLOAT,
    presio_media        FLOAT,
    PRIMARY KEY ((enpresa_id, eraikina_id, gela_id))
);

-- Eraikin bakoitzak duen eguraldia
CREATE TABLE Eguraldia (
    enpresa_id          BIGINT,
    eraikina_id         BIGINT,
    denbora_zigilua     TIMESTAMP,  -- Adibidez '2024-06-05 10:00:00'
    tenperatura         FLOAT,
    hezetasuna          FLOAT,
    PRIMARY KEY ((enpresa_id, eraikina_id, denbora_zigilua))
);

-- Insert Enpresa
INSERT INTO Enpresa (enpresa_id, izena) VALUES (1, 'Ulma');
INSERT INTO Enpresa (enpresa_id, izena) VALUES (2, 'Fagor');

-- Insert Eraikina
INSERT INTO Eraikina (enpresa_id, eraikina_id, lokalizazioa) VALUES (1, 1, 'Oñati');
INSERT INTO Eraikina (enpresa_id, eraikina_id, lokalizazioa) VALUES (1, 2, 'Bilbo');
INSERT INTO Eraikina (enpresa_id, eraikina_id, lokalizazioa) VALUES (2, 1, 'Arrasate');

-- Insert Gela
INSERT INTO Gela (enpresa_id, eraikina_id, gela_id) VALUES (1, 1, 1);
INSERT INTO Gela (enpresa_id, eraikina_id, gela_id) VALUES (1, 1, 2);
INSERT INTO Gela (enpresa_id, eraikina_id, gela_id) VALUES (1, 2, 1);
INSERT INTO Gela (enpresa_id, eraikina_id, gela_id) VALUES (1, 2, 2);
INSERT INTO Gela (enpresa_id, eraikina_id, gela_id) VALUES (2, 1, 1);
INSERT INTO Gela (enpresa_id, eraikina_id, gela_id) VALUES (2, 1, 2);

-- Insert Parametroa
INSERT INTO Parametroa (enpresa_id, eraikina_id, gela_id, parametroa_id, mota, minimoa_du, balio_min, balio_max) VALUES (1, 1, 1, 1, 'TMP', TRUE, 18.5, 20.5);
INSERT INTO Parametroa (enpresa_id, eraikina_id, gela_id, parametroa_id, mota, minimoa_du, balio_min, balio_max) VALUES (1, 1, 1, 1, 'SND', FALSE, NULL, 20.5);
INSERT INTO Parametroa (enpresa_id, eraikina_id, gela_id, parametroa_id, mota, minimoa_du, balio_min, balio_max) VALUES (1, 1, 1, 1, 'HMD', TRUE, 18.5, 20.5);
INSERT INTO Parametroa (enpresa_id, eraikina_id, gela_id, parametroa_id, mota, minimoa_du, balio_min, balio_max) VALUES (1, 1, 1, 1, 'PRS', TRUE, 18.5, 20.5);

INSERT INTO Parametroa (enpresa_id, eraikina_id, gela_id, parametroa_id, mota, minimoa_du, balio_min, balio_max) VALUES (1, 1, 2, 2, 'TMP', TRUE, 18.5, 20.5);
INSERT INTO Parametroa (enpresa_id, eraikina_id, gela_id, parametroa_id, mota, minimoa_du, balio_min, balio_max) VALUES (1, 1, 2, 2, 'SND', FALSE, NULL, 20.5);
INSERT INTO Parametroa (enpresa_id, eraikina_id, gela_id, parametroa_id, mota, minimoa_du, balio_min, balio_max) VALUES (1, 1, 2, 2, 'HMD', TRUE, 18.5, 20.5);
INSERT INTO Parametroa (enpresa_id, eraikina_id, gela_id, parametroa_id, mota, minimoa_du, balio_min, balio_max) VALUES (1, 1, 2, 2, 'PRS', TRUE, 18.5, 20.5);

INSERT INTO Parametroa (enpresa_id, eraikina_id, gela_id, parametroa_id, mota, minimoa_du, balio_min, balio_max) VALUES (1, 2, 1, 3, 'TMP', TRUE, 18.5, 20.5);
INSERT INTO Parametroa (enpresa_id, eraikina_id, gela_id, parametroa_id, mota, minimoa_du, balio_min, balio_max) VALUES (1, 2, 1, 3, 'SND', FALSE, NULL, 20.5);
INSERT INTO Parametroa (enpresa_id, eraikina_id, gela_id, parametroa_id, mota, minimoa_du, balio_min, balio_max) VALUES (1, 2, 1, 3, 'HMD', TRUE, 18.5, 20.5);
INSERT INTO Parametroa (enpresa_id, eraikina_id, gela_id, parametroa_id, mota, minimoa_du, balio_min, balio_max) VALUES (1, 2, 1, 3, 'PRS', TRUE, 18.5, 20.5);

INSERT INTO Parametroa (enpresa_id, eraikina_id, gela_id, parametroa_id, mota, minimoa_du, balio_min, balio_max) VALUES (1, 2, 2, 4, 'TMP', TRUE, 18.5, 20.5);
INSERT INTO Parametroa (enpresa_id, eraikina_id, gela_id, parametroa_id, mota, minimoa_du, balio_min, balio_max) VALUES (1, 2, 2, 4, 'SND', FALSE, NULL, 20.5);
INSERT INTO Parametroa (enpresa_id, eraikina_id, gela_id, parametroa_id, mota, minimoa_du, balio_min, balio_max) VALUES (1, 2, 2, 4, 'HMD', TRUE, 18.5, 20.5);
INSERT INTO Parametroa (enpresa_id, eraikina_id, gela_id, parametroa_id, mota, minimoa_du, balio_min, balio_max) VALUES (1, 2, 2, 4, 'PRS', TRUE, 18.5, 20.5);

INSERT INTO Parametroa (enpresa_id, eraikina_id, gela_id, parametroa_id, mota, minimoa_du, balio_min, balio_max) VALUES (2, 1, 1, 5, 'TMP', TRUE, 18.5, 20.5);
INSERT INTO Parametroa (enpresa_id, eraikina_id, gela_id, parametroa_id, mota, minimoa_du, balio_min, balio_max) VALUES (2, 1, 1, 5, 'SND', FALSE, NULL, 20.5);
INSERT INTO Parametroa (enpresa_id, eraikina_id, gela_id, parametroa_id, mota, minimoa_du, balio_min, balio_max) VALUES (2, 1, 1, 5, 'HMD', TRUE, 18.5, 20.5);
INSERT INTO Parametroa (enpresa_id, eraikina_id, gela_id, parametroa_id, mota, minimoa_du, balio_min, balio_max) VALUES (2, 1, 1, 5, 'PRS', TRUE, 18.5, 20.5);

INSERT INTO Parametroa (enpresa_id, eraikina_id, gela_id, parametroa_id, mota, minimoa_du, balio_min, balio_max) VALUES (2, 1, 2, 6, 'TMP', TRUE, 18.5, 20.5);
INSERT INTO Parametroa (enpresa_id, eraikina_id, gela_id, parametroa_id, mota, minimoa_du, balio_min, balio_max) VALUES (2, 1, 2, 6, 'SND', FALSE, NULL, 20.5);
INSERT INTO Parametroa (enpresa_id, eraikina_id, gela_id, parametroa_id, mota, minimoa_du, balio_min, balio_max) VALUES (2, 1, 2, 6, 'HMD', TRUE, 18.5, 20.5);
INSERT INTO Parametroa (enpresa_id, eraikina_id, gela_id, parametroa_id, mota, minimoa_du, balio_min, balio_max) VALUES (2, 1, 2, 6, 'PRS', TRUE, 18.5, 20.5);