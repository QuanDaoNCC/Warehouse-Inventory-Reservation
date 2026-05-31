INSERT INTO products (sku, name, description) VALUES
    ('A100', 'Widget Alpha', 'Standard alpha widget'),
    ('B200', 'Widget Beta', 'Premium beta widget'),
    ('C300', 'Widget Gamma', 'Economy gamma widget');

INSERT INTO inventory (sku, total_stock, available_stock, reserved_stock, version) VALUES
    ('A100', 100, 100, 0, 0),
    ('B200', 50, 50, 0, 0),
    ('C300', 200, 200, 0, 0);
