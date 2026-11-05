-- Grid Sentinel Baseline Schema
-- PostgreSQL 18 + PostGIS 3.6
-- Flyway V1 - Initial schema for all bounded contexts

-- Enable PostGIS extension
CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- =========================================================================
-- AUTH & USER
-- =========================================================================

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT true,
    mfa_enabled BOOLEAN NOT NULL DEFAULT false,
    mfa_secret VARCHAR(255),
    last_login_at TIMESTAMPTZ,
    password_changed_at TIMESTAMPTZ,
    failed_login_attempts INTEGER NOT NULL DEFAULT 0,
    locked_until TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    version INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);

CREATE TABLE user_roles (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, role)
);

CREATE TABLE user_profiles (
    id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    phone_number VARCHAR(50),
    radio_call_sign VARCHAR(50),
    employee_id VARCHAR(100),
    department VARCHAR(100),
    title VARCHAR(100),
    supervisor_id UUID REFERENCES users(id),
    timezone VARCHAR(50) NOT NULL DEFAULT 'UTC',
    locale VARCHAR(10) NOT NULL DEFAULT 'en',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    version INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE user_certifications (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    certification VARCHAR(255) NOT NULL,
    PRIMARY KEY (user_id, certification)
);

CREATE TABLE user_qualifications (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    qualification VARCHAR(255) NOT NULL,
    PRIMARY KEY (user_id, qualification)
);

CREATE TABLE user_preferences (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    pref_key VARCHAR(100) NOT NULL,
    pref_value TEXT,
    PRIMARY KEY (user_id, pref_key)
);

CREATE TABLE operator_assignments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    assignment_type VARCHAR(50) NOT NULL,
    asset_id UUID REFERENCES assets(id) ON DELETE SET NULL,
    grid_node_id UUID REFERENCES grid_nodes(id) ON DELETE SET NULL,
    region_id VARCHAR(100),
    shift_id VARCHAR(100),
    valid_from TIMESTAMPTZ NOT NULL DEFAULT now(),
    valid_until TIMESTAMPTZ,
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_operator_assignments_user ON operator_assignments(user_id);
CREATE INDEX idx_operator_assignments_asset ON operator_assignments(asset_id);
CREATE INDEX idx_operator_assignments_node ON operator_assignments(grid_node_id);
CREATE INDEX idx_operator_assignments_active ON operator_assignments(valid_from, valid_until) WHERE valid_until IS NULL OR valid_until > now();

-- =========================================================================
-- ASSET
-- =========================================================================

CREATE TABLE assets (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    criticality VARCHAR(20) NOT NULL,
    health_index DOUBLE PRECISION NOT NULL DEFAULT 1.0 CHECK (health_index >= 0.0 AND health_index <= 1.0),
    redundancy_level INTEGER NOT NULL DEFAULT 0 CHECK (redundancy_level >= 0),
    geometry GEOMETRY(GEOMETRY, 4326) NOT NULL,
    grid_node_id UUID, -- FK to grid_nodes, added after grid_nodes creation
    description TEXT,
    installed_at TIMESTAMPTZ,
    last_inspected_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    version INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX idx_assets_type ON assets(type);
CREATE INDEX idx_assets_criticality ON assets(criticality);
CREATE INDEX idx_assets_geometry ON assets USING GIST (geometry);
CREATE INDEX idx_assets_grid_node ON assets(grid_node_id);
CREATE INDEX idx_assets_health ON assets(health_index);

-- =========================================================================
-- GRID TOPOLOGY
-- =========================================================================

CREATE TABLE grid_nodes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    voltage_level_kv DOUBLE PRECISION NOT NULL CHECK (voltage_level_kv > 0),
    geometry GEOMETRY(POINT, 4326) NOT NULL,
    primary_asset_id UUID REFERENCES assets(id) ON DELETE SET NULL,
    operator VARCHAR(100),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    version INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX idx_grid_nodes_type ON grid_nodes(type);
CREATE INDEX idx_grid_nodes_voltage ON grid_nodes(voltage_level_kv);
CREATE INDEX idx_grid_nodes_geometry ON grid_nodes USING GIST (geometry);
CREATE INDEX idx_grid_nodes_asset ON grid_nodes(primary_asset_id);

CREATE TABLE grid_edges (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    from_node_id UUID NOT NULL REFERENCES grid_nodes(id) ON DELETE RESTRICT,
    to_node_id UUID NOT NULL REFERENCES grid_nodes(id) ON DELETE RESTRICT,
    voltage_level_kv DOUBLE PRECISION NOT NULL CHECK (voltage_level_kv > 0),
    thermal_rating_amps DOUBLE PRECISION NOT NULL CHECK (thermal_rating_amps > 0),
    length_km DOUBLE PRECISION NOT NULL DEFAULT 0 CHECK (length_km >= 0),
    geometry GEOMETRY(LINESTRING, 4326) NOT NULL,
    circuit_count INTEGER NOT NULL DEFAULT 1 CHECK (circuit_count >= 1),
    conductor_type VARCHAR(100),
    year_installed INTEGER,
    primary_asset_id UUID REFERENCES assets(id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    version INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT chk_grid_edges_distinct_nodes CHECK (from_node_id <> to_node_id)
);

CREATE INDEX idx_grid_edges_from_node ON grid_edges(from_node_id);
CREATE INDEX idx_grid_edges_to_node ON grid_edges(to_node_id);
CREATE INDEX idx_grid_edges_type ON grid_edges(type);
CREATE INDEX idx_grid_edges_voltage ON grid_edges(voltage_level_kv);
CREATE INDEX idx_grid_edges_geometry ON grid_edges USING GIST (geometry);
CREATE INDEX idx_grid_edges_asset ON grid_edges(primary_asset_id);

-- Add FK from assets to grid_nodes after both tables exist
ALTER TABLE assets ADD CONSTRAINT fk_assets_grid_node FOREIGN KEY (grid_node_id) REFERENCES grid_nodes(id) ON DELETE SET NULL;

-- =========================================================================
-- OBSERVATION
-- =========================================================================

CREATE TABLE observations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    source_type VARCHAR(50) NOT NULL,
    source_identifier VARCHAR(255) NOT NULL,
    timestamp TIMESTAMPTZ NOT NULL,
    received_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    payload JSONB NOT NULL,
    content_hash VARCHAR(64) NOT NULL,
    associated_asset_id UUID REFERENCES assets(id) ON DELETE SET NULL,
    nearby_grid_node_id UUID REFERENCES grid_nodes(id) ON DELETE SET NULL,
    geometry GEOMETRY(GEOMETRY, 4326),
    validation_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    validation_notes TEXT,
    validated_at TIMESTAMPTZ,
    validated_by UUID REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    version INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX idx_observations_source ON observations(source_type, source_identifier);
CREATE INDEX idx_observations_timestamp ON observations(timestamp);
CREATE INDEX idx_observations_asset ON observations(associated_asset_id);
CREATE INDEX idx_observations_node ON observations(nearby_grid_node_id);
CREATE INDEX idx_observations_geometry ON observations USING GIST (geometry);
CREATE INDEX idx_observations_validation ON observations(validation_status);
CREATE INDEX idx_observations_received ON observations(received_at);

-- =========================================================================
-- RISK
-- =========================================================================

CREATE TABLE risk_scores (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    asset_id UUID NOT NULL REFERENCES assets(id) ON DELETE CASCADE,
    score DOUBLE PRECISION NOT NULL CHECK (score >= 0.0 AND score <= 1.0),
    band VARCHAR(20) NOT NULL,
    model_name VARCHAR(100) NOT NULL,
    model_version VARCHAR(50) NOT NULL,
    strategy_name VARCHAR(100),
    factor_breakdown JSONB NOT NULL,
    computed_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    correlation_id UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_risk_scores_asset ON risk_scores(asset_id);
CREATE INDEX idx_risk_scores_score ON risk_scores(score);
CREATE INDEX idx_risk_scores_band ON risk_scores(band);
CREATE INDEX idx_risk_scores_computed ON risk_scores(computed_at);
CREATE INDEX idx_risk_scores_model ON risk_scores(model_name, model_version);

-- =========================================================================
-- DETECTION / THREAT
-- =========================================================================

CREATE TABLE threat_events (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    type VARCHAR(50) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    confidence DOUBLE PRECISION NOT NULL CHECK (confidence >= 0.0 AND confidence <= 1.0),
    detected_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    first_observed_at TIMESTAMPTZ,
    affected_asset_id UUID REFERENCES assets(id) ON DELETE SET NULL,
    nearby_grid_node_id UUID REFERENCES grid_nodes(id) ON DELETE SET NULL,
    location GEOMETRY(GEOMETRY, 4326),
    description TEXT NOT NULL,
    rationale TEXT,
    detection_strategy VARCHAR(100),
    strategy_version VARCHAR(50),
    status VARCHAR(20) NOT NULL DEFAULT 'NEW',
    correlation_id UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    version INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX idx_threat_events_type ON threat_events(type);
CREATE INDEX idx_threat_events_severity ON threat_events(severity);
CREATE INDEX idx_threat_events_detected ON threat_events(detected_at);
CREATE INDEX idx_threat_events_asset ON threat_events(affected_asset_id);
CREATE INDEX idx_threat_events_node ON threat_events(nearby_grid_node_id);
CREATE INDEX idx_threat_events_location ON threat_events USING GIST (location);
CREATE INDEX idx_threat_events_status ON threat_events(status);
CREATE INDEX idx_threat_events_confidence ON threat_events(confidence);

CREATE TABLE threat_evidence (
    threat_event_id UUID NOT NULL REFERENCES threat_events(id) ON DELETE CASCADE,
    evidence_type VARCHAR(50) NOT NULL,
    value DOUBLE PRECISION NOT NULL,
    unit VARCHAR(50),
    confidence DOUBLE PRECISION NOT NULL CHECK (confidence >= 0.0 AND confidence <= 1.0),
    source_observation_id UUID REFERENCES observations(id) ON DELETE SET NULL,
    extracted_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    extractor_version VARCHAR(50),
    metadata JSONB,
    PRIMARY KEY (threat_event_id, evidence_type, source_observation_id)
);

-- =========================================================================
-- INCIDENT
-- =========================================================================

CREATE TABLE incidents (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    incident_number VARCHAR(50) NOT NULL UNIQUE,
    state VARCHAR(20) NOT NULL DEFAULT 'DETECTED',
    severity VARCHAR(20) NOT NULL,
    originating_threat_id UUID REFERENCES threat_events(id) ON DELETE SET NULL,
    primary_asset_id UUID REFERENCES assets(id) ON DELETE SET NULL,
    primary_grid_node_id UUID REFERENCES grid_nodes(id) ON DELETE SET NULL,
    location GEOMETRY(GEOMETRY, 4326),
    description TEXT NOT NULL,
    operator_notes TEXT,
    assigned_operator_id UUID REFERENCES users(id) ON DELETE SET NULL,
    incident_commander_id UUID REFERENCES users(id) ON DELETE SET NULL,
    detected_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    triaged_at TIMESTAMPTZ,
    confirmed_at TIMESTAMPTZ,
    response_started_at TIMESTAMPTZ,
    contained_at TIMESTAMPTZ,
    recovering_at TIMESTAMPTZ,
    resolved_at TIMESTAMPTZ,
    closed_at TIMESTAMPTZ,
    correlation_id UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    version INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX idx_incidents_number ON incidents(incident_number);
CREATE INDEX idx_incidents_state ON incidents(state);
CREATE INDEX idx_incidents_severity ON incidents(severity);
CREATE INDEX idx_incidents_asset ON incidents(primary_asset_id);
CREATE INDEX idx_incidents_node ON incidents(primary_grid_node_id);
CREATE INDEX idx_incidents_location ON incidents USING GIST (location);
CREATE INDEX idx_incidents_detected ON incidents(detected_at);
CREATE INDEX idx_incidents_operator ON incidents(assigned_operator_id);
CREATE INDEX idx_incidents_commander ON incidents(incident_commander_id);

CREATE TABLE incident_state_history (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    incident_id UUID NOT NULL REFERENCES incidents(id) ON DELETE CASCADE,
    to_state VARCHAR(20) NOT NULL,
    actor_id UUID REFERENCES users(id) ON DELETE SET NULL,
    reason TEXT,
    correlation_id UUID,
    timestamp TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_incident_state_history_incident ON incident_state_history(incident_id);
CREATE INDEX idx_incident_state_history_timestamp ON incident_state_history(timestamp);

-- =========================================================================
-- SIMULATION
-- =========================================================================

CREATE TABLE simulations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    scenario_id VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    topology_snapshot_id VARCHAR(100),
    strategy_name VARCHAR(100),
    strategy_version VARCHAR(50),
    random_seed BIGINT,
    parameters JSONB NOT NULL,
    result JSONB,
    error_message TEXT,
    requested_by UUID REFERENCES users(id) ON DELETE SET NULL,
    correlation_id UUID,
    started_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    version INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX idx_simulations_scenario ON simulations(scenario_id);
CREATE INDEX idx_simulations_status ON simulations(status);
CREATE INDEX idx_simulations_requested_by ON simulations(requested_by);
CREATE INDEX idx_simulations_created ON simulations(created_at);

-- =========================================================================
-- RESPONSE
-- =========================================================================

CREATE TABLE response_plans (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    incident_id UUID NOT NULL REFERENCES incidents(id) ON DELETE CASCADE,
    name VARCHAR(255),
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    created_by UUID REFERENCES users(id) ON DELETE SET NULL,
    approved_by UUID REFERENCES users(id) ON DELETE SET NULL,
    correlation_id UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    approved_at TIMESTAMPTZ,
    started_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    version INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX idx_response_plans_incident ON response_plans(incident_id);
CREATE INDEX idx_response_plans_status ON response_plans(status);

CREATE TABLE response_actions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    response_plan_id UUID NOT NULL REFERENCES response_plans(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    type VARCHAR(50) NOT NULL,
    priority VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    target_asset_id UUID REFERENCES assets(id) ON DELETE SET NULL,
    target_grid_node_id UUID REFERENCES grid_nodes(id) ON DELETE SET NULL,
    assigned_to UUID REFERENCES users(id) ON DELETE SET NULL,
    reported_by UUID REFERENCES users(id) ON DELETE SET NULL,
    planned_start_at TIMESTAMPTZ,
    planned_end_at TIMESTAMPTZ,
    actual_start_at TIMESTAMPTZ,
    actual_end_at TIMESTAMPTZ,
    result_notes TEXT,
    correlation_id UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_response_actions_plan ON response_actions(response_plan_id);
CREATE INDEX idx_response_actions_status ON response_actions(status);
CREATE INDEX idx_response_actions_priority ON response_actions(priority);
CREATE INDEX idx_response_actions_assigned ON response_actions(assigned_to);

CREATE TABLE response_action_dependencies (
    action_id UUID NOT NULL REFERENCES response_actions(id) ON DELETE CASCADE,
    depends_on_id UUID NOT NULL REFERENCES response_actions(id) ON DELETE CASCADE,
    PRIMARY KEY (action_id, depends_on_id)
);

-- =========================================================================
-- RECOVERY
-- =========================================================================

CREATE TABLE recovery_states (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    incident_id UUID NOT NULL REFERENCES incidents(id) ON DELETE CASCADE,
    phase VARCHAR(20) NOT NULL DEFAULT 'NOT_STARTED',
    overall_progress DOUBLE PRECISION NOT NULL DEFAULT 0.0 CHECK (overall_progress >= 0.0 AND overall_progress <= 1.0),
    recovery_manager_id UUID REFERENCES users(id) ON DELETE SET NULL,
    estimated_completion_at TIMESTAMPTZ,
    actual_completion_at TIMESTAMPTZ,
    lessons_learned TEXT,
    after_action_report TEXT,
    correlation_id UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    version INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX idx_recovery_states_incident ON recovery_states(incident_id);
CREATE INDEX idx_recovery_states_phase ON recovery_states(phase);

CREATE TABLE recovery_milestones (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    recovery_state_id UUID NOT NULL REFERENCES recovery_states(id) ON DELETE CASCADE,
    milestone_id VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    type VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'NOT_STARTED',
    sequence_order INTEGER,
    target_asset_id UUID REFERENCES assets(id) ON DELETE SET NULL,
    target_grid_node_id UUID REFERENCES grid_nodes(id) ON DELETE SET NULL,
    assigned_to UUID REFERENCES users(id) ON DELETE SET NULL,
    verified_by UUID REFERENCES users(id) ON DELETE SET NULL,
    planned_at TIMESTAMPTZ,
    started_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    verified_at TIMESTAMPTZ,
    verification_notes TEXT,
    evidence_ref VARCHAR(255)
);

CREATE INDEX idx_recovery_milestones_state ON recovery_milestones(recovery_state_id);
CREATE INDEX idx_recovery_milestones_status ON recovery_milestones(status);
CREATE INDEX idx_recovery_milestones_sequence ON recovery_milestones(recovery_state_id, sequence_order);

-- =========================================================================
-- PROTECTION
-- =========================================================================

CREATE TABLE protection_plans (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    total_estimated_cost DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    total_expected_risk_reduction DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    budget_limit DOUBLE PRECISION,
    created_by UUID REFERENCES users(id) ON DELETE SET NULL,
    approved_by UUID REFERENCES users(id) ON DELETE SET NULL,
    correlation_id UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    approved_at TIMESTAMPTZ,
    version INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX idx_protection_plans_status ON protection_plans(status);

CREATE TABLE protection_measures (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    protection_plan_id UUID NOT NULL REFERENCES protection_plans(id) ON DELETE CASCADE,
    action_id VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(50) NOT NULL,
    estimated_cost DOUBLE PRECISION NOT NULL CHECK (estimated_cost >= 0),
    expected_risk_reduction DOUBLE PRECISION NOT NULL CHECK (expected_risk_reduction >= 0.0 AND expected_risk_reduction <= 1.0),
    confidence DOUBLE PRECISION NOT NULL DEFAULT 0.8 CHECK (confidence >= 0.0 AND confidence <= 1.0),
    implementation_window_start TIMESTAMPTZ,
    implementation_window_end TIMESTAMPTZ,
    priority INTEGER
);

CREATE INDEX idx_protection_measures_plan ON protection_measures(protection_plan_id);

-- =========================================================================
-- COPILOT
-- =========================================================================

CREATE TABLE copilot_requests (
    request_id VARCHAR(50) PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    prompt TEXT NOT NULL,
    grounding_context JSONB,
    requested_by UUID REFERENCES users(id) ON DELETE SET NULL,
    correlation_id UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_copilot_requests_user ON copilot_requests(requested_by);
CREATE INDEX idx_copilot_requests_created ON copilot_requests(created_at);

CREATE TABLE copilot_responses (
    response_id VARCHAR(50) PRIMARY KEY,
    request_id VARCHAR(50) NOT NULL REFERENCES copilot_requests(request_id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    model_name VARCHAR(100),
    model_version VARCHAR(50),
    confidence DOUBLE PRECISION,
    grounding_citations JSONB,
    requires_review BOOLEAN NOT NULL DEFAULT true,
    correlation_id UUID,
    generated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_copilot_responses_request ON copilot_responses(request_id);

-- =========================================================================
-- AUDIT
-- =========================================================================

CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    event_type VARCHAR(50) NOT NULL,
    aggregate_type VARCHAR(100) NOT NULL,
    aggregate_id VARCHAR(255) NOT NULL,
    action VARCHAR(100) NOT NULL,
    details JSONB,
    actor_id UUID REFERENCES users(id) ON DELETE SET NULL,
    actor_role VARCHAR(50),
    actor_ip VARCHAR(45),
    user_agent TEXT,
    correlation_id UUID,
    causation_id VARCHAR(255),
    ai_assisted BOOLEAN NOT NULL DEFAULT false,
    ai_model_version VARCHAR(50),
    ai_request_id VARCHAR(50),
    timestamp TIMESTAMPTZ NOT NULL DEFAULT now(),
    hash VARCHAR(64) NOT NULL
);

CREATE INDEX idx_audit_logs_event_type ON audit_logs(event_type);
CREATE INDEX idx_audit_logs_aggregate ON audit_logs(aggregate_type, aggregate_id);
CREATE INDEX idx_audit_logs_actor ON audit_logs(actor_id);
CREATE INDEX idx_audit_logs_correlation ON audit_logs(correlation_id);
CREATE INDEX idx_audit_logs_causation ON audit_logs(causation_id);
CREATE INDEX idx_audit_logs_timestamp ON audit_logs(timestamp);
CREATE INDEX idx_audit_logs_ai ON audit_logs(ai_assisted) WHERE ai_assisted = true;

-- =========================================================================
-- TOPOLOGY SNAPSHOTS (for simulation determinism)
-- =========================================================================

CREATE TABLE topology_snapshots (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    snapshot_id VARCHAR(100) NOT NULL UNIQUE,
    captured_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    node_count INTEGER NOT NULL,
    edge_count INTEGER NOT NULL,
    nodes_json JSONB NOT NULL,
    edges_json JSONB NOT NULL,
    adjacency_json JSONB NOT NULL,
    created_by UUID REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX idx_topology_snapshots_captured ON topology_snapshots(captured_at);
CREATE INDEX idx_topology_snapshots_id ON topology_snapshots(snapshot_id);

-- =========================================================================
-- UPDATED_AT TRIGGERS
-- =========================================================================

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    NEW.version = OLD.version + 1;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_user_profiles_updated_at BEFORE UPDATE ON user_profiles FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_assets_updated_at BEFORE UPDATE ON assets FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_grid_nodes_updated_at BEFORE UPDATE ON grid_nodes FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_grid_edges_updated_at BEFORE UPDATE ON grid_edges FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_observations_updated_at BEFORE UPDATE ON observations FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_threat_events_updated_at BEFORE UPDATE ON threat_events FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_incidents_updated_at BEFORE UPDATE ON incidents FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_simulations_updated_at BEFORE UPDATE ON simulations FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_response_plans_updated_at BEFORE UPDATE ON response_plans FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_response_actions_updated_at BEFORE UPDATE ON response_actions FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_recovery_states_updated_at BEFORE UPDATE ON recovery_states FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_protection_plans_updated_at BEFORE UPDATE ON protection_plans FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- =========================================================================
-- VIEWS FOR COMMON QUERIES
-- =========================================================================

-- Asset with latest risk score
CREATE VIEW asset_risk_view AS
SELECT a.*, rs.score as risk_score, rs.band as risk_band, rs.computed_at as risk_computed_at
FROM assets a
LEFT JOIN LATERAL (
    SELECT score, band, computed_at
    FROM risk_scores
    WHERE asset_id = a.id
    ORDER BY computed_at DESC
    LIMIT 1
) rs ON true;

-- Active incidents with response status
CREATE VIEW active_incidents_view AS
SELECT i.*, rp.status as response_status,
       (SELECT COUNT(*) FROM response_actions ra WHERE ra.response_plan_id = rp.id) as total_actions,
       (SELECT COUNT(*) FROM response_actions ra WHERE ra.response_plan_id = rp.id AND ra.status = 'COMPLETED') as completed_actions
FROM incidents i
LEFT JOIN response_plans rp ON rp.incident_id = i.id
WHERE i.state IN ('DETECTED', 'TRIAGED', 'CONFIRMED', 'ACTIVE_RESPONSE', 'CONTAINED', 'RECOVERING');

-- Threat events needing attention
CREATE VIEW actionable_threats_view AS
SELECT *
FROM threat_events
WHERE confidence >= 0.7
  AND severity IN ('MEDIUM', 'HIGH', 'CRITICAL')
  AND status IN ('NEW', 'TRIAGED')
ORDER BY severity DESC, confidence DESC, detected_at ASC;