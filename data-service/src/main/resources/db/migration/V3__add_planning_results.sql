-- V3__add_planning_results.sql
-- Planning result snapshots for history and comparison

CREATE TABLE IF NOT EXISTS planning_results (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    planning_window_id UUID REFERENCES planning_windows(id) ON DELETE SET NULL,
    algorithm VARCHAR(30) NOT NULL CHECK (algorithm IN ('GREEDY', 'SIMULATED_ANNEALING', 'MONTE_CARLO')),
    total_cost DOUBLE PRECISION NOT NULL,
    computation_time_ms BIGINT NOT NULL,
    result_data JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_planning_results_window ON planning_results(planning_window_id);
CREATE INDEX idx_planning_results_created ON planning_results(created_at DESC);
CREATE INDEX idx_planning_results_algorithm ON planning_results(algorithm);
