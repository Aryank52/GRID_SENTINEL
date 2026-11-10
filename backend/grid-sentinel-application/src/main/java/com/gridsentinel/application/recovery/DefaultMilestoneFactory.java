package com.gridsentinel.application.recovery;

import com.gridsentinel.incident.Incident;
import com.gridsentinel.recovery.RecoveryMilestone;
import com.gridsentinel.recovery.MilestoneType;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Factory for creating recovery milestones based on incident characteristics.
 */
public class DefaultMilestoneFactory implements InitializeRecoveryUseCase.MilestoneFactory {

    @Override
    public List<RecoveryMilestone> createMilestones(Incident incident) {
        List<RecoveryMilestone> milestones = new ArrayList<>();
        int sequence = 1;

        // Phase 1: Assessment
        milestones.add(createMilestone("MS-DA-001", "Damage Assessment", 
                "Assess damage to affected assets and infrastructure",
                MilestoneType.DAMAGE_ASSESSMENT, sequence++, null, null));
        
        milestones.add(createMilestone("MS-RA-002", "Resource Assessment",
                "Assess required resources, equipment, and personnel",
                MilestoneType.RESOURCE_ASSESSMENT, sequence++, null, null));
        
        milestones.add(createMilestone("MS-SA-003", "Safety Assessment",
                "Conduct safety assessment of affected area",
                MilestoneType.SAFETY_ASSESSMENT, sequence++, null, null));

        // Phase 2: Resource Mobilization
        milestones.add(createMilestone("MS-CM-004", "Crew Mobilization",
                "Mobilize repair crews and personnel",
                MilestoneType.CREW_MOBILIZATION, sequence++, null, null));
        
        milestones.add(createMilestone("MS-EM-005", "Equipment Mobilization",
                "Mobilize required equipment and tools",
                MilestoneType.EQUIPMENT_MOBILIZATION, sequence++, null, null));
        
        milestones.add(createMilestone("MS-MM-006", "Material Mobilization",
                "Mobilize replacement materials and components",
                MilestoneType.MATERIAL_MOBILIZATION, sequence++, null, null));
        
        milestones.add(createMilestone("MS-LS-007", "Logistics Setup",
                "Establish logistics and staging areas",
                MilestoneType.LOGISTICS_SETUP, sequence++, null, null));

        // Phase 3: Restoration
        milestones.add(createMilestone("MS-DE-008", "De-energization",
                "Safely de-energize affected equipment",
                MilestoneType.DE_ENERGIZATION, sequence++, null, null));
        
        milestones.add(createMilestone("MS-IS-009", "Isolation",
                "Isolate affected equipment and sections",
                MilestoneType.ISOLATION, sequence++, null, null));
        
        milestones.add(createMilestone("MS-RS-010", "Repair Start",
                "Begin repair operations on damaged equipment",
                MilestoneType.REPAIR_START, sequence++, null, null));
        
        milestones.add(createMilestone("MS-RC-011", "Repair Complete",
                "Complete all repair operations",
                MilestoneType.REPAIR_COMPLETE, sequence++, null, null));
        
        milestones.add(createMilestone("MS-RE-012", "Equipment Replacement",
                "Replace damaged equipment as needed",
                MilestoneType.EQUIPMENT_REPLACEMENT, sequence++, null, null));
        
        milestones.add(createMilestone("MS-TE-013", "Testing",
                "Perform functional testing of repaired equipment",
                MilestoneType.TESTING, sequence++, null, null));
        
        milestones.add(createMilestone("MS-REN-014", "Re-energization",
                "Safely re-energize restored equipment",
                MilestoneType.RE_ENERGIZATION, sequence++, null, null));

        // Phase 4: Verification
        milestones.add(createMilestone("MS-FT-015", "Functional Test",
                "Perform functional testing of restored systems",
                MilestoneType.FUNCTIONAL_TEST, sequence++, null, null));
        
        milestones.add(createMilestone("MS-PV-016", "Performance Verification",
                "Verify system performance meets requirements",
                MilestoneType.PERFORMANCE_VERIFICATION, sequence++, null, null));
        
        milestones.add(createMilestone("MS-SI-017", "Safety Inspection",
                "Conduct post-restoration safety inspection",
                MilestoneType.SAFETY_INSPECTION, sequence++, null, null));
        
        milestones.add(createMilestone("MS-CC-018", "Compliance Check",
                "Verify compliance with regulations and standards",
                MilestoneType.COMPLIANCE_CHECK, sequence++, null, null));

        // Phase 5: Completion
        milestones.add(createMilestone("MS-SR-019", "Service Restored",
                "Confirm service is fully restored to customers",
                MilestoneType.SERVICE_RESTORED, sequence++, null, null));
        
        milestones.add(createMilestone("MS-DC-020", "Documentation Complete",
                "Complete all restoration documentation",
                MilestoneType.DOCUMENTATION_COMPLETE, sequence++, null, null));
        
        milestones.add(createMilestone("MS-LL-021", "Lessons Learned",
                "Document lessons learned from the incident",
                MilestoneType.LESSONS_LEARNED, sequence++, null, null));
        
        milestones.add(createMilestone("MS-CO-022", "Closeout",
                "Formal closeout of recovery activities",
                MilestoneType.CLOSEOUT, sequence++, null, null));

        return milestones;
    }

    private RecoveryMilestone createMilestone(String id, String name, String description,
                                               MilestoneType type, int sequence,
                                               com.gridsentinel.common.CommonKernel.AssetId assetId,
                                               com.gridsentinel.common.CommonKernel.GridNodeId nodeId) {
        return RecoveryMilestone.builder()
                .id(id)
                .name(name)
                .description(description)
                .type(type)
                .sequenceOrder(sequence)
                .plannedAt(java.time.Instant.now().plusSeconds(sequence * 3600))
                .build();
    }
}