-- V2__add_external_ids.sql
-- External tracker IDs for Jira/ADO/Linear integration

ALTER TABLE features ADD COLUMN IF NOT EXISTS external_id VARCHAR(255);
ALTER TABLE features ADD COLUMN IF NOT EXISTS external_source VARCHAR(50);
ALTER TABLE teams ADD COLUMN IF NOT EXISTS external_id VARCHAR(255);
ALTER TABLE sprints ADD COLUMN IF NOT EXISTS external_id VARCHAR(255);

CREATE INDEX IF NOT EXISTS idx_features_external_id ON features(external_id) WHERE external_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_teams_external_id ON teams(external_id) WHERE external_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_sprints_external_id ON sprints(external_id) WHERE external_id IS NOT NULL;
