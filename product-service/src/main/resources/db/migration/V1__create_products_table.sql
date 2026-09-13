CREATE TABLE products (
    id uuid PRIMARY KEY,
    name varchar(150) NOT NULL,
    description text,
    price numeric(12, 2) NOT NULL,
    stock_quantity integer NOT NULL DEFAULT 0,
    active boolean NOT NULL DEFAULT true,
    created_at timestamp NOT NULL DEFAULT now(),
    updated_at timestamp NOT NULL DEFAULT now()
);
