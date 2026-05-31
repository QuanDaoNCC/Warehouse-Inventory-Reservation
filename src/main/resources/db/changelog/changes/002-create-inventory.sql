CREATE TABLE inventory (
    sku              VARCHAR(50) PRIMARY KEY REFERENCES products (sku),
    total_stock      INT         NOT NULL CHECK (total_stock >= 0),
    available_stock  INT         NOT NULL CHECK (available_stock >= 0),
    reserved_stock   INT         NOT NULL DEFAULT 0 CHECK (reserved_stock >= 0),
    version          BIGINT      NOT NULL DEFAULT 0
);
