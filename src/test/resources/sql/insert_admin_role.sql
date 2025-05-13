-- Insert admin role if not exists
INSERT INTO role (role_id, role_name, is_active)
SELECT 4, 'System Admin', true
WHERE NOT EXISTS (
    SELECT 1 FROM role WHERE role_id = 4
); 