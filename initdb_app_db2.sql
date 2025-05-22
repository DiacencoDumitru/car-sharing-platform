create DATABASE app_db2;

\c app_db2

create TABLE Continent(
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL
);

create TABLE Country (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    continent_id INT REFERENCES Continent(id),
    area FLOAT NOT NULL
);

create TABLE Person (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

create TABLE Person_Country (
    person_id INT REFERENCES Person(id),
    country_id INT REFERENCES Country(id),
    PRIMARY KEY (person_id, country_id)
);

INSERT INTO Continent (name) VALUES ('Europe'), ('Asia'), ('Africa'), ('North America'), ('South America'), ('Australia');

INSERT INTO Country (name, continent_id, area) VALUES
('United States', 4, 9833517),
('Sri Lanka', 2, 65610),
('Argentina', 5, 2780400),
('Germany', 1, 357582),
('Monaco', 1, 2),
('Russia', 2, 17098242),
('Brazil', 5, 8515767),
('Vatican City', 1, 0.44),
('Canada', 4, 9984670),
('Australia', 6, 7692024),
('India', 2, 3287263),
('France', 1, 643801),
('Japan', 2, 377975),
('Nigeria', 3, 923768);

INSERT INTO Person (name) VALUES ('Dumitru'), ('Rostislav'), ('Vitalii'), ('Charlie'), ('Diana'), ('Welson'),
('Raul'), ('Frank'), ('Charlie'), ('Peter'), ('Eve'), ('Dumitru'), ('Grace'), ('Alice'), ('Marius'), ('John'),
('Sarah'), ('Michael'), ('Emma'), ('David'), ('Lisa'), ('Thomas'), ('Anna'), ('Robert'), ('Julia'), ('William'),
('Sophie'), ('James'), ('Olivia'), ('Daniel'), ('Isabella'), ('Matthew'), ('Emily'), ('Andrew'), ('Charlotte'),
('Joseph'), ('Amelia'), ('Benjamin'), ('Mia'), ('Samuel'), ('Harper'), ('Alexander'), ('Evelyn'), ('Henry'),
('Abigail'), ('Edward'), ('Ella'), ('George'), ('Scarlett'), ('Charles'), ('Victoria'), ('Thomas'),
('Grace'), ('Paul'), ('Hannah'), ('Mark'), ('Lily'), ('Steven'), ('Zoe'), ('Richard'), ('Chloe');

INSERT INTO Person_Country (person_id, country_id) VALUES  (1, 1), (1, 2), (2, 3), (3, 4), (4, 1),
(4, 3), (5, 4), (5, 3), (5, 2), (6, 3), (7, 4), (8, 4), (9,4), (10,4), (16, 5), (17, 5), (18, 5),
(19, 6), (20, 6), (21, 6), (22, 7), (23, 7), (24, 7), (25, 8), (26, 8), (27, 8), (28, 9), (29, 9),
(30, 9), (31, 10), (32, 10), (33, 10), (34, 11), (35, 11), (36, 11), (37, 12), (38, 12), (39, 12),
(40, 13), (41, 13), (42, 13), (43, 14), (44, 14), (45, 14), (46, 1), (47, 2), (48, 3), (49, 4),
(50, 1), (16, 2), (17, 3), (18, 4), (19, 1), (20, 2);