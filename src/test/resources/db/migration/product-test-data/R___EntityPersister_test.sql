CREATE TABLE products (
                          id bigint auto_increment,
                          name varchar(255) NOT NULL,
                          producer varchar(255) NOT NULL,
                          price decimal(10,2) NOT NULL,
                          PRIMARY KEY (id)
);

INSERT INTO products(id, name, producer, price)
VALUES (1, 'Play Station', 'Sony', 249.00),
       (2, 'XBox', 'Microsoft', 215.00),
       (3, 'Play Station Portable', 'Sony', 150.00);
