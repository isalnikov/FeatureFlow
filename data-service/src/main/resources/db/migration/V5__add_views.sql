-- V5__add_views.sql
-- Useful views for dashboard and reporting

-- Team capacity summary
CREATE OR REPLACE VIEW v_team_capacity AS
SELECT
    t.id AS team_id,
    t.name AS team_name,
    t.focus_factor,
    t.bug_reserve_percent,
    t.techdebt_reserve_percent,
    tm.role,
    COUNT(tm.id) AS member_count,
    SUM(tm.availability_percent) AS total_availability
FROM teams t
LEFT JOIN team_members tm ON tm.team_id = t.id
GROUP BY t.id, t.name, t.focus_factor, t.bug_reserve_percent, t.techdebt_reserve_percent, tm.role;

-- Feature summary with aggregated data
CREATE OR REPLACE VIEW v_feature_summary AS
SELECT
    f.id,
    f.title,
    f.business_value,
    f.deadline,
    f.class_of_service,
    (f.effort_estimate->>'backendHours')::DOUBLE PRECISION AS backend_hours,
    (f.effort_estimate->>'frontendHours')::DOUBLE PRECISION AS frontend_hours,
    (f.effort_estimate->>'qaHours')::DOUBLE PRECISION AS qa_hours,
    (f.effort_estimate->>'devopsHours')::DOUBLE PRECISION AS devops_hours,
    COUNT(DISTINCT fp.product_id) AS product_count,
    COUNT(DISTINCT fd.depends_on_id) AS dependency_count,
    COUNT(DISTINCT a.id) AS assignment_count
FROM features f
LEFT JOIN feature_products fp ON fp.feature_id = f.id
LEFT JOIN feature_dependencies fd ON fd.feature_id = f.id
LEFT JOIN assignments a ON a.feature_id = f.id
GROUP BY f.id;

-- Sprint load by team and role
CREATE OR REPLACE VIEW v_sprint_load AS
SELECT
    s.id AS sprint_id,
    s.start_date,
    s.end_date,
    a.team_id,
    t.name AS team_name,
    COUNT(DISTINCT a.feature_id) AS feature_count,
    SUM((a.allocated_effort->>'backendHours')::DOUBLE PRECISION) AS backend_load,
    SUM((a.allocated_effort->>'frontendHours')::DOUBLE PRECISION) AS frontend_load,
    SUM((a.allocated_effort->>'qaHours')::DOUBLE PRECISION) AS qa_load,
    SUM((a.allocated_effort->>'devopsHours')::DOUBLE PRECISION) AS devops_load
FROM sprints s
JOIN assignments a ON a.sprint_id = s.id
JOIN teams t ON t.id = a.team_id
WHERE a.status != 'COMPLETED'
GROUP BY s.id, s.start_date, s.end_date, a.team_id, t.name;
