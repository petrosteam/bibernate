CREATE TABLE products
(
    id       bigint auto_increment,
    name     varchar(255)   NOT NULL,
    producer varchar(255)   NOT NULL,
    price    decimal(10, 2) NOT NULL,
    PRIMARY KEY (id)
    -- todo add data with all SQL types and test it
);
CREATE TABLE persons
(
    id         bigint auto_increment,
    first_name varchar(255) NOT NULL,
    PRIMARY KEY (id)
);
CREATE TABLE notes
(
    id        bigint auto_increment,
    body      varchar(255) NOT NULL,
    person_id bigint NOT NULL,
    PRIMARY KEY (id)
);

INSERT INTO products(id, name, producer, price)
VALUES (1, 'Play Station', 'Sony', 249.00),
       (2, 'XBox', 'Microsoft', 215.00),
       (3, 'Play Station Portable', 'Sony', 150.00);

INSERT INTO persons(id, first_name)
VALUES (1, 'Oleg'),
       (2, 'Stas'),
       (3, 'Viktor');

INSERT INTO notes(id, body, person_id)
VALUES (1, 'Body of Note-1', 1),
       (2, 'Body of Note-2', 1),
       (3, 'Body of Note-3',2);
