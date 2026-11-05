/**
 * Detection bounded context.
 * <p>
 * Pipeline: Observation -> validation -> normalization -> feature evidence ->
 * anomaly/CV score -> evidence fusion -> threat classification -> alert.
 * Every alert exposes: what happened, confidence, severity, timestamp,
 * affected/nearby asset, evidence, why the system thinks it matters.
 * </p>
 *
 * @since 0.1.0
 */
package com.gridsentinel.detection;