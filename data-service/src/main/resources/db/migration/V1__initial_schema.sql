-- V1__initial_schema.sql
-- FeatureFlow initial database schema

-- Products
CREATE TABLE products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    version INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_products_name ON products(name);

CREATE TABLE product_tech_stack (
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    technology VARCHAR(100) NOT NULL,
    PRIMARY KEY (product_id, technology)
);

-- Teams
CREATE TABLE teams (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    focus_factor DOUBLE PRECISION NOT NULL DEFAULT 0.7,
    bug_reserve_percent DOUBLE PRECISION NOT NULL DEFAULT 0.20,
    techdebt_reserve_percent DOUBLE PRECISION NOT NULL DEFAULT 0.10,
    velocity DOUBLE PRECISION,
    version INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_teams_name ON teams(name);

CREATE TABLE team_members (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    team_id UUID NOT NULL REFERENCES teams(id) ON DELETE CASCADE,
    person_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('BACKEND', 'FRONTEND', 'QA', 'DEVOPS')),
    availability_percent DOUBLE PRECISION NOT NULL DEFAULT 1.0,
    UNIQUE(team_id, person_id)
);

CREATE INDEX idx_team_members_team ON team_members(team_id);
CREATE INDEX idx_team_members_role ON team_members(role);

CREATE TABLE team_expertise (
    team_id UUID NOT NULL REFERENCES teams(id) ON DELETE CASCADE,
    expertise_tag VARCHAR(100) NOT NULL,
    PRIMARY KEY (team_id, expertise_tag)
);

-- Product-Team ownership (many-to-many)
CREATE TABLE product_teams (
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    team_id UUID NOT NULL REFERENCES teams(id) ON DELETE CASCADE,
    PRIMARY KEY (product_id, team_id)
);

CREATE INDEX idx_product_teams_team ON product_teams(team_id);

-- Features
CREATE TABLE features (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(500) NOT NULL,
    description TEXT,
    business_value DOUBLE PRECISION NOT NULL DEFAULT 0,
    requestor_id UUID,
    deadline DATE,
    class_of_service VARCHAR(20) NOT NULL DEFAULT 'STANDARD'
        CHECK (class_of_service IN ('EXPEDITE', 'FIXED_DATE', 'STANDARD', 'FILLER')),
    effort_estimate JSONB NOT NULL DEFAULT '{"backendHours":0,"frontendHours":0,"qaHours":0,"devopsHours":0}',
    stochastic_estimate JSONB,
    can_split BOOLEAN NOT NULL DEFAULT false,
    version INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_features_deadline ON features(deadline) WHERE deadline IS NOT NULL;
CREATE INDEX idx_features_class_of_service ON features(class_of_service);
CREATE INDEX idx_features_business_value ON features(business_value DESC);
CREATE INDEX idx_features_effort ON features USING GIN(effort_estimate);
CREATE INDEX idx_features_title_search ON features USING GIN(to_tsvector('english', title));

-- Feature-Product (many-to-many)
CREATE TABLE feature_products (
    feature_id UUID NOT NULL REFERENCES features(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    PRIMARY KEY (feature_id, product_id)
);

CREATE INDEX idx_feature_products_product ON feature_products(product_id);

-- Feature dependencies (self-referencing many-to-many)
CREATE TABLE feature_dependencies (
    feature_id UUID NOT NULL REFERENCES features(id) ON DELETE CASCADE,
    depends_on_id UUID NOT NULL REFERENCES features(id) ON DELETE CASCADE,
    PRIMARY KEY (feature_id, depends_on_id),
    CHECK (feature_id != depends_on_id)
);

CREATE INDEX idx_feature_dependencies_depends_on ON feature_dependencies(depends_on_id);

-- Feature required expertise
CREATE TABLE feature_required_expertise (
    feature_id UUID NOT NULL REFERENCES features(id) ON DELETE CASCADE,
    expertise_tag VARCHAR(100) NOT NULL,
    PRIMARY KEY (feature_id, expertise_tag)
);

-- Planning Windows
CREATE TABLE planning_windows (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    version INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CHECK (end_date >= start_date)
);

-- Sprints
CREATE TABLE sprints (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    planning_window_id UUID NOT NULL REFERENCES planning_windows(id) ON DELETE CASCADE,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    capacity_overrides JSONB,
    CHECK (end_date >= start_date)
);

CREATE INDEX idx_sprints_window ON sprints(planning_window_id);
CREATE INDEX idx_sprints_dates ON sprints(start_date, end_date);

-- Assignments
CREATE TABLE assignments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    feature_id UUID NOT NULL REFERENCES features(id) ON DELETE CASCADE,
    team_id UUID NOT NULL REFERENCES teams(id) ON DELETE RESTRICT,
    sprint_id UUID NOT NULL REFERENCES sprints(id) ON DELETE RESTRICT,
    allocated_effort JSONB NOT NULL DEFAULT '{"backendHours":0,"frontendHours":0,"qaHours":0,"devopsHours":0}',
    status VARCHAR(20) NOT NULL DEFAULT 'PLANNED'
        CHECK (status IN ('PLANNED', 'IN_PROGRESS', 'COMPLETED', 'LOCKED')),
    version INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_assignments_feature ON assignments(feature_id);
CREATE INDEX idx_assignments_team ON assignments(team_id);
CREATE INDEX idx_assignments_sprint ON assignments(sprint_id);
CREATE INDEX idx_assignments_status ON assignments(status);
CREATE INDEX idx_assignments_team_sprint ON assignments(team_id, sprint_id);
CREATE INDEX idx_assignments_locked ON assignments(sprint_id) WHERE status = 'LOCKED';

-- Updated_at trigger function
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Apply triggers
CREATE TRIGGER update_products_updated_at BEFORE UPDATE ON products
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_teams_updated_at BEFORE UPDATE ON teams
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_features_updated_at BEFORE UPDATE ON features
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_assignments_updated_at BEFORE UPDATE ON assignments
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
