package com.gridsentinel.grid;

/**
 * Type of grid topology node.
 */
public enum NodeType {
    SUBSTATION,
    SWITCHING_STATION,
    GENERATION,
    LOAD,
    JUNCTION,
    HVDC_CONVERTER,
    FACTS_DEVICE,
    OTHER
}