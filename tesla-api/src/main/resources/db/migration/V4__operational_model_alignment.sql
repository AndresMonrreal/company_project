-- ============================================================
-- V4: Operational Model Alignment
-- ============================================================

-- 1. scrap_records restructure
ALTER TABLE scrap_records DROP CONSTRAINT scrap_records_cutting_record_id_fkey;
ALTER TABLE scrap_records DROP CONSTRAINT scrap_records_quantity_check;
ALTER TABLE scrap_records DROP COLUMN cutting_record_id;
ALTER TABLE scrap_records ADD COLUMN shift_id BIGINT NOT NULL REFERENCES shifts(id);
ALTER TABLE scrap_records ADD COLUMN profile_id BIGINT NOT NULL REFERENCES profiles(id);
ALTER TABLE scrap_records ADD COLUMN operator_id BIGINT NOT NULL REFERENCES users(id);
ALTER TABLE scrap_records ADD CONSTRAINT scrap_quantity_check CHECK (quantity > 0);

-- 2. profiles: add new columns (nullable, no DEFAULT)
ALTER TABLE profiles ADD COLUMN type VARCHAR(10);
ALTER TABLE profiles ADD COLUMN profile_position VARCHAR(10);

-- 3. machines: add new columns
ALTER TABLE machines ADD COLUMN code VARCHAR(20) UNIQUE;
ALTER TABLE machines ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'OPERATIONAL';
ALTER TABLE machines ADD CONSTRAINT machines_status_check CHECK (status IN ('OPERATIONAL', 'MAINTENANCE', 'OUT_OF_SERVICE'));
ALTER TABLE machines ADD COLUMN processes_type VARCHAR(10);
ALTER TABLE machines ADD CONSTRAINT machines_processes_type_check CHECK (processes_type IN ('HEADER', 'LOWER'));
ALTER TABLE machines ADD COLUMN cycle_time_seconds INTEGER;
ALTER TABLE machines ADD COLUMN last_maintenance_date DATE;
ALTER TABLE machines ADD COLUMN observations VARCHAR(500);

-- 4. Seed canonical profiles (UPSERT on code)
INSERT INTO profiles (code, name, type, profile_position, active)
VALUES
  ('EXTE00036', 'Header-frontal', 'HEADER', 'FRONT', true),
  ('EXTE00037', 'Header-REAR',    'HEADER', 'REAR',  true),
  ('EXTE00038', 'LOWER-REAR',     'LOWER',  'REAR',  true),
  ('EXTE00039', 'LOWER-FRONT',    'LOWER',  'FRONT', true)
ON CONFLICT (code) DO UPDATE SET
  name             = EXCLUDED.name,
  type             = EXCLUDED.type,
  profile_position = EXCLUDED.profile_position,
  active           = true;

-- 5. Seed canonical shifts (UPSERT on name)
INSERT INTO shifts (name, start_time, end_time, active)
VALUES
  ('Matutino', '07:30', '17:00', true),
  ('Nocturno', '19:30', '07:30', true)
ON CONFLICT (name) DO UPDATE SET
  start_time = EXCLUDED.start_time,
  end_time   = EXCLUDED.end_time;

-- 6. Soft-delete non-canonical shifts
UPDATE shifts SET active = false
WHERE name NOT IN ('Matutino', 'Nocturno');

-- 7. Seed canonical machines (UPSERT on name)
INSERT INTO machines (name, code, processes_type, status, active)
VALUES
  ('Tecma', 'TECMA004', 'HEADER', 'OPERATIONAL', true),
  ('Milan', 'MILAN',    'LOWER',  'OPERATIONAL', true)
ON CONFLICT (name) DO UPDATE SET
  code           = EXCLUDED.code,
  processes_type = EXCLUDED.processes_type,
  status         = EXCLUDED.status,
  active         = true;

-- 8. Soft-delete non-canonical profiles
UPDATE profiles SET active = false
WHERE code NOT IN ('EXTE00036','EXTE00037','EXTE00038','EXTE00039');

-- 9. Soft-delete unreferenced legacy machines
UPDATE machines SET active = false
WHERE name NOT IN ('Tecma', 'Milan')
  AND id NOT IN (SELECT DISTINCT machine_id FROM cutting_records);

-- 10. profiles CHECK constraints (after seed + soft-delete so all rows satisfy them)
ALTER TABLE profiles ADD CONSTRAINT profiles_type_check
  CHECK (type IS NULL OR type IN ('HEADER','LOWER'));
ALTER TABLE profiles ADD CONSTRAINT profiles_position_check
  CHECK (profile_position IS NULL OR profile_position IN ('FRONT','REAR'));
ALTER TABLE profiles ADD CONSTRAINT profiles_active_requires_type_position
  CHECK (active = false OR (type IS NOT NULL AND profile_position IS NOT NULL));

-- 11. Indexes on scrap_records
CREATE INDEX IF NOT EXISTS idx_scrap_records_operator_shift
  ON scrap_records (operator_id, shift_id);
CREATE INDEX IF NOT EXISTS idx_scrap_records_profile_id
  ON scrap_records (profile_id);
