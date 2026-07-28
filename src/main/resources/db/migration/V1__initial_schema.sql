CREATE TABLE products
(
    id     UUID PRIMARY KEY,
    sku    VARCHAR(50)    NOT NULL,
    name   VARCHAR(150)   NOT NULL,
    price  NUMERIC(12, 2) NOT NULL,
    active BOOLEAN        NOT NULL DEFAULT TRUE,

    CONSTRAINT uk_product_sku UNIQUE (sku)
);

CREATE TABLE inventories
(
    id                 UUID PRIMARY KEY,
    product_id         UUID    NOT NULL,
    available_quantity INTEGER NOT NULL DEFAULT 0,
    reserved_quantity  INTEGER NOT NULL DEFAULT 0,

    CONSTRAINT uk_inventory_product UNIQUE (product_id),
    CONSTRAINT fk_inventory_product
        FOREIGN KEY (product_id)
            REFERENCES products (id)

);

CREATE TABLE inventory_transactions
(
    id         UUID PRIMARY KEY,
    product_id UUID        NOT NULL,
    type       VARCHAR(40) NOT NULL,
    quantity   INTEGER     NOT NULL,
    created_at TIMESTAMP   NOT NULL,

    CONSTRAINT fk_inventory_transaction_product
        FOREIGN KEY (product_id)
            REFERENCES products (id)
);

CREATE TABLE orders
(
    id             UUID PRIMARY KEY,
    status         VARCHAR(20)  NOT NULL,
    customer_name  VARCHAR(255) NOT NULL,
    customer_email VARCHAR(255) NOT NULL,
    country        VARCHAR(255),
    city           VARCHAR(255),
    postal_code    VARCHAR(255),
    street         VARCHAR(255),
    address_line   VARCHAR(255),
    created_at     TIMESTAMP    NOT NULL,
    updated_at     TIMESTAMP    NOT NULL
);

CREATE TABLE order_items
(
    id           UUID PRIMARY KEY,
    order_id     UUID           NOT NULL,
    product_id   UUID           NOT NULL,
    product_sku  VARCHAR(50)    NOT NULL,
    product_name VARCHAR(150)   NOT NULL,
    quantity     INTEGER        NOT NULL,
    unit_price   NUMERIC(19, 2) NOT NULL,

    CONSTRAINT fk_order_item_order
        FOREIGN KEY (order_id)
            REFERENCES orders (id),

    CONSTRAINT fk_order_item_product
        FOREIGN KEY (product_id)
            REFERENCES products(id)
);