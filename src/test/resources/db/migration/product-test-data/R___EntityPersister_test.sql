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

INSERT INTO products(id, name, producer, price, created_at, is_available, stock_count, weight, description, sale_date, sale_time)
VALUES (1, 'Play Station', 'Sony', 249.00, '2023-01-01 12:00:00', true, 100, 3.0, 'Play Station console', '2023-01-10', '09:00:00'),
       (2, 'XBox', 'Microsoft', 215.00, '2023-01-02 14:00:00', true, 150, 3.5, 'XBox console', '2023-01-15', '10:00:00'),
       (3, 'Play Station Portable', 'Sony', 150.00, '2023-01-03 16:00:00', true, 75, 1.0, 'Play Station Portable console', '2023-01-20', '11:00:00');
