\c market

INSERT INTO users (login, password)
VALUES
    ('user1', '$2a$10$IlsY2MncOm8ZgHSx6CU3CugGxzRmH2FzBfBoXsupXc8QPm8eKkegq'),
    ('user', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi');

INSERT INTO items (title, description, count, price)
VALUES
    ('Title 1 first', 'Text first text text 1', 9, 10),
    ('Title 2 second', 'Text second text text 2', 10, 100),
    ('Title 3 third', 'Text third text text 3', 11, 399),
    ('Title 4 forth', 'Text forth text text 4', 1, 2000);

INSERT INTO carts (user_id)
VALUES
    (1);

INSERT INTO cart_items (item_id, cart_id, count)
VALUES
    (1, 1, 3),
    (2, 1, 2);

INSERT INTO orders (user_id)
VALUES
    (1),
    (1),
    (1);

INSERT INTO order_items (item_id, order_id, title, price, count)
VALUES
    (1, 1, 'Title 1 first old', 9, 2),
    (2, 1, 'Title 2 first old', 6, 1),
    (3, 1, 'Title 3 first old', 8, 3),
    (3, 2, 'Title 3 first old', 9, 2),
    (4, 2, 'Title 4 first old', 6, 1),
    (1, 3, 'Title 1 first old', 8, 3),
    (2, 3, 'Title 2 first old', 9, 2),
    (3, 3, 'Title 3 first old', 6, 1),
    (4, 3, 'Title 4 first old', 8, 3);