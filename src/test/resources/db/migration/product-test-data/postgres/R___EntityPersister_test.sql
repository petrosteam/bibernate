CREATE TABLE products
(
    id           BIGSERIAL,
    name         varchar(255)   NOT NULL,
    producer     varchar(255)   NOT NULL,
    price        decimal(10, 2) NOT NULL,
    created_at   TIMESTAMP,
    is_available boolean,
    stock_count  integer,
    weight       float,
    description  text,
    sale_date    date,
    sale_time    time,
    PRIMARY KEY (id)
);

CREATE TABLE persons
(
    id         BIGSERIAL,
    first_name varchar(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE notes
(
    id        BIGSERIAL,
    body      varchar(255) NOT NULL,
    person_id BIGSERIAL    NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (person_id) REFERENCES persons (id)
);

CREATE TABLE cars
(
    id         BIGSERIAL,
    name varchar(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE eager_wheel_cars
(
    id         BIGSERIAL,
    name varchar(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE wheels
(
    id        BIGSERIAL,
    side      varchar(255) NOT NULL,
    position      varchar(255) NOT NULL,
    car_id BIGSERIAL    NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (car_id) REFERENCES cars (id)
);

INSERT INTO products(name, producer, price, created_at, is_available, stock_count, weight, description, sale_date,
                     sale_time)
VALUES ('Play Station', 'Sony', 249.00, '2023-01-01 12:00:00', true, 100, 3.0, 'Play Station console', '2023-01-10',
        '09:00:00'),
       ('XBox', 'Microsoft', 215.00, '2023-01-02 14:00:00', true, 150, 3.5, 'XBox console', '2023-01-15', '10:00:00'),
       ('Play Station Portable', 'Sony', 150.00, '2023-01-03 16:00:00', true, 75, 1.0, 'Play Station Portable console',
        '2023-01-20', '11:00:00');

INSERT INTO persons(first_name)
VALUES ('Oleg'),
       ('Viktor');

INSERT INTO notes(body, person_id)
VALUES ('Body of Note-1', 1),
       ('Body of Note-2', 2);

INSERT INTO cars(name)
VALUES ('Tavriya');

INSERT INTO eager_wheel_cars(name)
VALUES ('Eager-Tavriya');

INSERT INTO wheels(side, position, car_id)
VALUES ('Left', 'Front', 1),
       ('Left', 'Rear', 1),
       ('Right', 'Front', 1);
