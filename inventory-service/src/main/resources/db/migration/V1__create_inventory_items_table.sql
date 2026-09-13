CREATE TABLE inventory_items (
    id                 UUID PRIMARY KEY,
    product_id         UUID NOT NULL,
    quantity_available INTEGER NOT NULL DEFAULT 0,
    quantity_reserved  INTEGER NOT NULL DEFAULT 0,
    warehouse_location VARCHAR(150) NOT NULL,
    updated_at         TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_inventory_items_product_id ON inventory_items (product_id);
