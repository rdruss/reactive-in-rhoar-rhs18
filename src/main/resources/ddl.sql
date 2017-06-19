-- Create the products table if not present
CREATE TABLE IF NOT EXISTS products (
  id        INTEGER IDENTITY,
  name      VARCHAR(40) NOT NULL
);
INSERT INTO products (name) values ('Drinks');
INSERT INTO products (name) values ('Food');
INSERT INTO products (name) values ('Coffee');