create table if not exists products(
    id BIGINT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    producer VARCHAR(255) NOT NULL,
    price NUMERIC(10,2) NOT NULL
);
