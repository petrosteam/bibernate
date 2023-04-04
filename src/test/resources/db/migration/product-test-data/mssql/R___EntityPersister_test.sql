CREATE TABLE products (
                          id BIGINT IDENTITY(1,1) PRIMARY KEY,
                          name varchar(255) NOT NULL,
                          producer varchar(255) NOT NULL,
                          price decimal(10,2) NOT NULL,
                          created_at DATETIME,
                          is_available bit,
                          stock_count integer,
                          weight float,
                          description text,
                          sale_date date,
                          sale_time time
);
CREATE TABLE persons
(
    id         BIGINT IDENTITY(1,1),
    first_name varchar(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE notes
(
    id        BIGINT IDENTITY(1,1),
    body      varchar(255) NOT NULL,
    person_id BIGINT    NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (person_id) REFERENCES persons (id)
);

CREATE TABLE person_info
(
    id         BIGINT IDENTITY(1,1),
    info varchar(255) NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (id) REFERENCES persons (id)
);

INSERT INTO products( name, producer, price, created_at, is_available, stock_count, weight, description, sale_date, sale_time)
VALUES ('Play Station', 'Sony', 249.00, '2023-01-01 12:00:00', 1, 100, 3.0, 'Play Station console', '2023-01-10', '09:00:00'),
       ('XBox', 'Microsoft', 215.00, '2023-01-02 14:00:00', 1, 150, 3.5, 'XBox console', '2023-01-15', '10:00:00'),
       ('Play Station Portable', 'Sony', 150.00, '2023-01-03 16:00:00', 1, 75, 1.0, 'Play Station Portable console', '2023-01-20', '11:00:00');

INSERT INTO persons(first_name)
VALUES ('Oleg'),
       ('Viktor');

INSERT INTO notes(body, person_id)
VALUES ('Body of Note-1', 1),
       ('Body of Note-2', 2);
