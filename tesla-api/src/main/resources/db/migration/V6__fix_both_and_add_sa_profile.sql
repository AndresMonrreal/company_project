-- V6: Revert BOTH from profiles.type, add BOTH to machines.processes_type, seed SAEXTE00036

-- 1. Remove test row
DELETE FROM profiles WHERE code = 'TESTBOTH1';

-- 2. Fail loudly if any remaining profile still holds type = 'BOTH'
DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM profiles WHERE type = 'BOTH') THEN
    RAISE EXCEPTION 'Cannot revert profiles_type_check: rows with type=BOTH still exist';
  END IF;
END $$;

-- 3. Revert profiles type constraint (V5 widened it — restore V4 version)
ALTER TABLE profiles DROP CONSTRAINT profiles_type_check;
ALTER TABLE profiles ADD CONSTRAINT profiles_type_check
  CHECK (type IS NULL OR type IN ('HEADER','LOWER'));

-- 4. Widen profiles.code to hold 11-char codes like SAEXTE00036
ALTER TABLE profiles ALTER COLUMN code TYPE VARCHAR(20);

-- 5. Allow BOTH on machines (V4 had no NULL guard — add it for legacy NULLs)
ALTER TABLE machines DROP CONSTRAINT machines_processes_type_check;
ALTER TABLE machines ADD CONSTRAINT machines_processes_type_check
  CHECK (processes_type IS NULL OR processes_type IN ('HEADER','LOWER','BOTH'));

-- 6. Seed new profile
INSERT INTO profiles (code, name, type, profile_position, active)
VALUES ('SAEXTE00036', 'Header-frontal SA', 'HEADER', 'FRONT', true)
ON CONFLICT (code) DO UPDATE SET
  name             = EXCLUDED.name,
  type             = EXCLUDED.type,
  profile_position = EXCLUDED.profile_position,
  active           = true;
