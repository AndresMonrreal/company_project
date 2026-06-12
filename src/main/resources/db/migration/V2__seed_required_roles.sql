INSERT INTO roles (name, description, active)
VALUES
    ('ADMIN', 'System administrator with catalog, user, report, export, and full-history access.', TRUE),
    ('SUPERVISOR', 'Shift supervisor with operational catalog read access, movement visibility, reports, history, reception registration, and reception approval or cancel permissions when required.', TRUE),
    ('OPERADOR', 'Production operator allowed to register receptions, cutting records, scrap, molding output, and view own current-shift history.', TRUE),
    ('CONSULTA', 'Read-only user allowed to view reports and history without registering operational movements.', TRUE)
ON CONFLICT (name) DO UPDATE
SET description = EXCLUDED.description,
    active = TRUE,
    updated_at = CURRENT_TIMESTAMP;
