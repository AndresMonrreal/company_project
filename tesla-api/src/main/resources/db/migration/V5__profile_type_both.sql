-- V5: Extend profiles type constraint to allow BOTH (header + lower)
ALTER TABLE profiles DROP CONSTRAINT profiles_type_check;
ALTER TABLE profiles ADD CONSTRAINT profiles_type_check
  CHECK (type IS NULL OR type IN ('HEADER','LOWER','BOTH'));
