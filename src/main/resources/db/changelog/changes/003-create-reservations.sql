CREATE TABLE reservations (
    id         UUID                     PRIMARY KEY,
    order_id   VARCHAR(100)             NOT NULL,
    status     VARCHAR(20)              NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE reservation_items (
    id              BIGSERIAL PRIMARY KEY,
    reservation_id  UUID        NOT NULL REFERENCES reservations (id) ON DELETE CASCADE,
    sku             VARCHAR(50) NOT NULL REFERENCES products (sku),
    quantity        INT         NOT NULL CHECK (quantity > 0)
);

CREATE INDEX idx_reservations_order_id ON reservations (order_id);
CREATE INDEX idx_reservation_items_reservation_id ON reservation_items (reservation_id);
