CREATE TABLE products (
                          id bigint auto_increment,
                          name varchar(255) NOT NULL,
                          producer varchar(255) NOT NULL,
                          price decimal(10,2) NOT NULL,
                          created_at datetime,
                          is_available boolean,
                          stock_count integer,
                          weight float,
                          description text,
                          sale_date date,
                          sale_time time,
                          PRIMARY KEY (id)
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
    PRIMARY KEY (id),
    FOREIGN KEY (person_id) REFERENCES persons(id)
);
CREATE TABLE cars
(
    id   bigint auto_increment,
    name varchar(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE eager_wheel_cars
(
    id   bigint auto_increment,
    name varchar(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE person_info
(
    id bigint,
    info      varchar(255) NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (id) REFERENCES persons(id)
)

INSERT INTO products(id, name, producer, price, created_at, is_available, stock_count, weight, description, sale_date, sale_time)
VALUES (1, 'Play Station', 'Sony', 249.00, '2023-01-01 12:00:00', true, 100, 3.0, 'Play Station console', '2023-01-10', '09:00:00'),
       (2, 'XBox', 'Microsoft', 215.00, '2023-01-02 14:00:00', true, 150, 3.5, 'XBox console', '2023-01-15', '10:00:00'),
       (3, 'Play Station Portable', 'Sony', 150.00, '2023-01-03 16:00:00', true, 75, 1.0, 'Play Station Portable console', '2023-01-20', '11:00:00');
CREATE TABLE wheels
(
    id       bigint auto_increment,
    position varchar(255) NOT NULL,
    side     varchar(255) NOT NULL,
    car_id   bigint       NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (car_id) REFERENCES cars (id)
);

INSERT INTO products(id, name, producer, price, created_at, is_available, stock_count, weight, description, sale_date,
                     sale_time)
VALUES (1, 'Play Station', 'Sony', 249.00, '2023-01-01 12:00:00', true, 100, 3.0, 'Play Station console', '2023-01-10',
        '09:00:00'),
       (2, 'XBox', 'Microsoft', 215.00, '2023-01-02 14:00:00', true, 150, 3.5, 'XBox console', '2023-01-15',
        '10:00:00'),
       (3, 'Play Station Portable', 'Sony', 150.00, '2023-01-03 16:00:00', true, 75, 1.0,
        'Play Station Portable console', '2023-01-20', '11:00:00');

INSERT INTO persons(id, first_name)
VALUES (1, 'Oleg'),
       (2, 'Viktor');

INSERT INTO notes(id, body, person_id)
VALUES (1, 'Body of Note-1', 1),
       (2, 'Body of Note-2', 2);

INSERT INTO cars(id, name)
VALUES (1, 'Tavriya');

INSERT INTO eager_wheel_cars(id, name)
VALUES (1, 'Eager-Tavriya');

INSERT INTO wheels(id, side, position, car_id)
VALUES (1, 'Left', 'Front', 1),
       (2, 'Left', 'Rear', 1),
       (3, 'Right', 'Front', 1);
