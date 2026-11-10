package com.gridsentinel.auth;

/**
 * User roles for authorization.
 */
public enum Role {
    // Operations
    OPERATOR,
    DISPATCHER,
    INCIDENT_COMMANDER,
    
    // Management
    SUPERVISOR,
    MANAGER,
    DIRECTOR,
    ADMINISTRATOR,
    
    // Analysis
    ANALYST,
    RISK_ANALYST,
    SECURITY_ANALYST,
    SIMULATION_ENGINEER,
    
    // Planning
    PLANNER,
    RECOVERY_MANAGER,
    
    // Security
    AUDITOR,
    
    // Executive
    EXECUTIVE_VIEWER,
    
    // Read-only
    VIEWER
}