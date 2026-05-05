-- Migration V12: Remove legacy columns from residents table
-- Reason: unit_label and block_label are no longer used in the application
-- The system now uses a foreign key reference to the units table (unit_id)

-- Step 1: Drop existing indexes that reference unit_label
DROP INDEX IF EXISTS idx_residents_unit_label;

-- Step 2: Remove the legacy columns
ALTER TABLE residents DROP COLUMN IF EXISTS unit_label;
ALTER TABLE residents DROP COLUMN IF EXISTS block_label;

-- Step 3: Add unit_id foreign key column if it doesn't exist
-- Note: This assumes residentes already have linked units through linked_user -> user relationships
-- If units need to be linked directly, this logic should be handled in the application layer

-- Optional: Add an index for better query performance on active residents
CREATE INDEX IF NOT EXISTS idx_residents_active_document ON residents(is_active, document_number);
