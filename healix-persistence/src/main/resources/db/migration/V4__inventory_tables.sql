-- 1. Inventory Batches Table
CREATE TABLE inventory_batches (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    warehouse_id UUID NOT NULL REFERENCES warehouses(id) ON DELETE CASCADE,
    batch_no VARCHAR(50) NOT NULL,
    mfg_date DATE NOT NULL,
    expiry_date DATE NOT NULL,
    purchase_rate DECIMAL(15, 2) NOT NULL,
    mrp DECIMAL(15, 2) NOT NULL,
    quantity INT NOT NULL DEFAULT 0,
    blocked_quantity INT NOT NULL DEFAULT 0,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    version INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID,
    updated_by UUID,
    CONSTRAINT uq_tenant_warehouse_product_batch UNIQUE (tenant_id, warehouse_id, product_id, batch_no),
    CONSTRAINT chk_batch_quantity CHECK (quantity >= 0),
    CONSTRAINT chk_batch_blocked CHECK (blocked_quantity >= 0),
    CONSTRAINT chk_batch_pricing CHECK (mrp >= purchase_rate),
    CONSTRAINT chk_batch_status CHECK (status IN ('ACTIVE', 'QUARANTINED', 'EXPIRED', 'DESTRUCTED'))
);

CREATE INDEX idx_inventory_batches_tenant ON inventory_batches(tenant_id);
CREATE INDEX idx_inventory_batches_product ON inventory_batches(product_id);
CREATE INDEX idx_inventory_batches_warehouse ON inventory_batches(warehouse_id);

-- 2. Stock Ledgers Table
CREATE TABLE stock_ledgers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    batch_id UUID NOT NULL REFERENCES inventory_batches(id) ON DELETE CASCADE,
    transaction_type VARCHAR(50) NOT NULL,
    quantity INT NOT NULL,
    reference_id UUID NOT NULL,
    notes VARCHAR(255),
    version INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID,
    updated_by UUID,
    CONSTRAINT chk_ledger_transaction_type CHECK (transaction_type IN ('PURCHASE', 'SALE', 'RETURN', 'ADJUSTMENT', 'TRANSFER'))
);

CREATE INDEX idx_stock_ledgers_tenant ON stock_ledgers(tenant_id);
CREATE INDEX idx_stock_ledgers_batch ON stock_ledgers(batch_id);

-- 3. Stock Adjustments Table
CREATE TABLE stock_adjustments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    batch_id UUID NOT NULL REFERENCES inventory_batches(id) ON DELETE CASCADE,
    quantity_before INT NOT NULL,
    quantity_after INT NOT NULL,
    reason VARCHAR(50) NOT NULL,
    approved_by UUID,
    version INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID,
    updated_by UUID,
    CONSTRAINT chk_adjustment_reason CHECK (reason IN ('DAMAGED', 'EXPIRED', 'THEFT', 'RECONCILIATION'))
);

CREATE INDEX idx_stock_adjustments_tenant ON stock_adjustments(tenant_id);
CREATE INDEX idx_stock_adjustments_batch ON stock_adjustments(batch_id);

-- 4. Stock Transfers Table
CREATE TABLE stock_transfers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    source_warehouse_id UUID NOT NULL REFERENCES warehouses(id) ON DELETE CASCADE,
    target_warehouse_id UUID NOT NULL REFERENCES warehouses(id) ON DELETE CASCADE,
    batch_id UUID NOT NULL REFERENCES inventory_batches(id) ON DELETE CASCADE,
    quantity INT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'REQUESTED',
    version INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID,
    updated_by UUID,
    CONSTRAINT chk_transfer_quantity CHECK (quantity > 0),
    CONSTRAINT chk_transfer_status CHECK (status IN ('REQUESTED', 'SHIPPED', 'RECEIVED', 'CANCELLED'))
);

CREATE INDEX idx_stock_transfers_tenant ON stock_transfers(tenant_id);
CREATE INDEX idx_stock_transfers_batch ON stock_transfers(batch_id);

-- Enable Row-Level Security (RLS) on newly created tables
ALTER TABLE inventory_batches ENABLE ROW LEVEL SECURITY;
ALTER TABLE stock_ledgers ENABLE ROW LEVEL SECURITY;
ALTER TABLE stock_adjustments ENABLE ROW LEVEL SECURITY;
ALTER TABLE stock_transfers ENABLE ROW LEVEL SECURITY;

-- Define RLS Isolation Policies
CREATE POLICY tenant_isolation_policy ON inventory_batches
    FOR ALL
    USING (tenant_id = NULLIF(current_setting('app.current_tenant_id', true), '')::UUID);

CREATE POLICY tenant_isolation_policy ON stock_ledgers
    FOR ALL
    USING (tenant_id = NULLIF(current_setting('app.current_tenant_id', true), '')::UUID);

CREATE POLICY tenant_isolation_policy ON stock_adjustments
    FOR ALL
    USING (tenant_id = NULLIF(current_setting('app.current_tenant_id', true), '')::UUID);

CREATE POLICY tenant_isolation_policy ON stock_transfers
    FOR ALL
    USING (tenant_id = NULLIF(current_setting('app.current_tenant_id', true), '')::UUID);
