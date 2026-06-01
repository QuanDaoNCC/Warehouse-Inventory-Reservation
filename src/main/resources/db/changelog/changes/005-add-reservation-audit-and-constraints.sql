ALTER TABLE reservations
    ADD COLUMN updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE reservations
    ADD CONSTRAINT uq_reservations_order_id UNIQUE (order_id);

ALTER TABLE inventory
    ADD CONSTRAINT chk_inventory_stock_consistency
        CHECK (available_stock + reserved_stock <= total_stock);
