-- Seed Demo Tenant
INSERT INTO tenants (id, name, subdomain, pan, status, created_at, updated_at)
VALUES ('88888888-8888-8888-8888-888888888888', 'Healix Demo Corp', 'demo', 'ABCDE1234F', 'ACTIVE', NOW(), NOW())
ON CONFLICT (subdomain) DO NOTHING;

-- Seed Admin Role
INSERT INTO roles (id, tenant_id, name, description, version, created_at, updated_at)
VALUES ('55555555-5555-5555-5555-555555555555', '88888888-8888-8888-8888-888888888888', 'ROLE_TENANT_ADMIN', 'Tenant Administrator Role', 0, NOW(), NOW())
ON CONFLICT (tenant_id, name) DO NOTHING;

-- Seed User Role
INSERT INTO roles (id, tenant_id, name, description, version, created_at, updated_at)
VALUES ('66666666-6666-6666-6666-666666666666', '88888888-8888-8888-8888-888888888888', 'ROLE_USER', 'Standard User Role', 0, NOW(), NOW())
ON CONFLICT (tenant_id, name) DO NOTHING;

-- Seed Admin User (Password is 'admin123')
INSERT INTO users (id, tenant_id, email, password_hash, first_name, last_name, status, version, created_at, updated_at)
VALUES ('11111111-1111-1111-1111-111111111111', '88888888-8888-8888-8888-888888888888', 'admin@healix.in', 'admin123', 'Demo', 'Admin', 'ACTIVE', 0, NOW(), NOW())
ON CONFLICT (tenant_id, email) DO NOTHING;

-- Seed Regular User (Password is 'user123')
INSERT INTO users (id, tenant_id, email, password_hash, first_name, last_name, status, version, created_at, updated_at)
VALUES ('22222222-2222-2222-2222-222222222222', '88888888-8888-8888-8888-888888888888', 'user@healix.in', 'user123', 'Demo', 'User', 'ACTIVE', 0, NOW(), NOW())
ON CONFLICT (tenant_id, email) DO NOTHING;

-- Assign Admin Role and User Role to Admin User
INSERT INTO user_roles (user_id, role_id)
VALUES ('11111111-1111-1111-1111-111111111111', '55555555-5555-5555-5555-555555555555')
ON CONFLICT DO NOTHING;
INSERT INTO user_roles (user_id, role_id)
VALUES ('11111111-1111-1111-1111-111111111111', '66666666-6666-6666-6666-666666666666')
ON CONFLICT DO NOTHING;

-- Assign User Role to Regular User
INSERT INTO user_roles (user_id, role_id)
VALUES ('22222222-2222-2222-2222-222222222222', '66666666-6666-6666-6666-666666666666')
ON CONFLICT DO NOTHING;

-- Seed Branch
INSERT INTO branches (id, tenant_id, name, gstin, address, state_code, created_at, status, version, updated_at)
VALUES ('33333333-3333-3333-3333-333333333333', '88888888-8888-8888-8888-888888888888', 'Main Branch', '29ABCDE1234F1Z5', '123 Healix Road, Bangalore', '29', NOW(), 'ACTIVE', 0, NOW())
ON CONFLICT DO NOTHING;

-- Seed Warehouse
INSERT INTO warehouses (id, branch_id, tenant_id, name, type, created_at, status, version, updated_at)
VALUES ('44444444-4444-4444-4444-444444444444', '33333333-3333-3333-3333-333333333333', '88888888-8888-8888-8888-888888888888', 'Main Warehouse', 'NORMAL', NOW(), 'ACTIVE', 0, NOW())
ON CONFLICT DO NOTHING;
