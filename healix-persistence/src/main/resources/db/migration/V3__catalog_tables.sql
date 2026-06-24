-- 1. Suppliers Table
CREATE TABLE suppliers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    gstin VARCHAR(15),
    pan VARCHAR(10),
    drug_license_no VARCHAR(100),
    address TEXT,
    contact_phone VARCHAR(20),
    contact_email VARCHAR(255),
    credit_days INT NOT NULL DEFAULT 30,
    credit_limit DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    payment_mode VARCHAR(50) NOT NULL DEFAULT 'BANK_TRANSFER',
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    version INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID,
    updated_by UUID,
    CONSTRAINT uq_tenant_supplier_name UNIQUE (tenant_id, name),
    CONSTRAINT chk_supplier_status CHECK (status IN ('ACTIVE', 'SUSPENDED', 'INACTIVE')),
    CONSTRAINT chk_supplier_payment_mode CHECK (payment_mode IN ('BANK_TRANSFER', 'UPI', 'CHEQUE', 'CASH'))
);

-- Create index on tenant_id for RLS performance
CREATE INDEX idx_suppliers_tenant ON suppliers(tenant_id);

-- 2. Products (Drug Master) Table
CREATE TABLE products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    brand_name VARCHAR(255) NOT NULL,
    generic_name VARCHAR(255) NOT NULL,
    manufacturer VARCHAR(255) NOT NULL,
    hsn_code VARCHAR(8) NOT NULL,
    schedule VARCHAR(10) NOT NULL DEFAULT 'OTC',
    dosage_form VARCHAR(50) NOT NULL DEFAULT 'TABLET',
    strength VARCHAR(50) NOT NULL,
    pack_size VARCHAR(50) NOT NULL,
    mrp DECIMAL(15, 2) NOT NULL,
    ptr DECIMAL(15, 2) NOT NULL,
    purchase_rate DECIMAL(15, 2) NOT NULL,
    gst_rate DECIMAL(5, 2) NOT NULL DEFAULT 12.00,
    is_narcotic BOOLEAN NOT NULL DEFAULT FALSE,
    is_cold_chain BOOLEAN NOT NULL DEFAULT FALSE,
    rack_location VARCHAR(100),
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    version INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID,
    updated_by UUID,
    CONSTRAINT uq_tenant_product_brand_generic UNIQUE (tenant_id, brand_name, generic_name, strength),
    CONSTRAINT chk_product_schedule CHECK (schedule IN ('OTC', 'H', 'H1', 'X')),
    CONSTRAINT chk_product_gst_rate CHECK (gst_rate IN (0.00, 5.00, 12.00, 18.00)),
    CONSTRAINT chk_product_status CHECK (status IN ('ACTIVE', 'SUSPENDED', 'INACTIVE'))
);

-- Create index on tenant_id for RLS performance
CREATE INDEX idx_products_tenant ON products(tenant_id);

-- 3. Product Approved Suppliers Mapping Table
CREATE TABLE product_suppliers (
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    supplier_id UUID NOT NULL REFERENCES suppliers(id) ON DELETE CASCADE,
    PRIMARY KEY (product_id, supplier_id)
);

-- Enable Row-Level Security (RLS) on newly created tables
ALTER TABLE suppliers ENABLE ROW LEVEL SECURITY;
ALTER TABLE products ENABLE ROW LEVEL SECURITY;
ALTER TABLE product_suppliers ENABLE ROW LEVEL SECURITY;

-- Define RLS Isolation Policies using context parameter
CREATE POLICY tenant_isolation_policy ON suppliers
    FOR ALL
    USING (tenant_id = NULLIF(current_setting('app.current_tenant_id', true), '')::UUID);

CREATE POLICY tenant_isolation_policy ON products
    FOR ALL
    USING (tenant_id = NULLIF(current_setting('app.current_tenant_id', true), '')::UUID);

CREATE POLICY tenant_isolation_policy ON product_suppliers
    FOR ALL
    USING (product_id IN (
        SELECT p.id FROM products p WHERE p.tenant_id = NULLIF(current_setting('app.current_tenant_id', true), '')::UUID
    ));
