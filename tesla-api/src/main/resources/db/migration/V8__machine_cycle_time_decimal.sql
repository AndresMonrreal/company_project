-- ============================================================
-- V8: Allow decimal cycle time on machines
-- ============================================================
ALTER TABLE machines
  ALTER COLUMN cycle_time_seconds TYPE DOUBLE PRECISION
  USING cycle_time_seconds::double precision;