CREATE TABLE product
(
    id           INTEGER PRIMARY KEY AUTO_INCREMENT,
    name_product VARCHAR(255)   NOT NULL,
    price        DECIMAL(10, 2) NOT NULL,
    quantity     INTEGER        NOT NULL DEFAULT 0
);
INSERT INTO product (name_product, price, quantity)
VALUES ('Laptop', 999.99, 10),
       ('Smartphone', 499.49, 25),
       ('Headphones', 79.99, 50),
       ('Monitor', 149.99, 15),
       ('Keyboard', 39.99, 40),
       ('Mouse', 29.99, 30),
       ('USB Drive', 19.99, 100),
       ('External HDD', 89.99, 20),
       ('Smartwatch', 199.99, 18),
       ('Gaming Chair', 299.99, 5);

