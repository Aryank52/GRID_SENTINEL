package com.gridsentinel.recovery;

/**
 * Type of recovery milestone.
 */
public enum MilestoneType {
    // Initial assessment
    DAMAGE_ASSESSMENT,
    RESOURCE_ASSESSMENT,
    SAFETY_ASSESSMENT,

    // Resource mobilization
    CREW_MOBILIZATION,
    EQUIPMENT_MOBILIZATION,
    MATERIAL_MOBILIZATION,
    LOGISTICS_SETUP,

    // Restoration
    DE_ENERGIZATION,
    ISOLATION,
    REPAIR_START,
    REPAIR_COMPLETE,
    REPLACEMENT,
    TESTING,
    RE_ENERGIZATION,

    // Verification
    FUNCTIONAL_TEST,
    PERFORMANCE_VERIFICATION,
    SAFETY_INSPECTION,
    COMPLIANCE_CHECK,

    // Completion
    SERVICE_RESTORED,
    DOCUMENTATION_COMPLETE,
    LESSONS_LEARNED,
    CLOSEOUT
}