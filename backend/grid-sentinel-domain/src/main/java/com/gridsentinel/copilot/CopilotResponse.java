package com.gridsentinel.copilot;

import com.gridsentinel.common.CommonKernel;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Response from the AI Copilot with grounding citations.
 */
@Entity
@Table(name = "copilot_responses")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CopilotResponse {

    @Id
    @Setter(AccessLevel.PRIVATE)
    private String responseId;

    @NotNull
    @Setter(AccessLevel.PRIVATE)
    private String requestId;

    @NotNull
    @Size(max = 8000)
    @Setter(AccessLevel.PRIVATE)
    private String content; // Generated response

    @Setter(AccessLevel.PRIVATE)
    private String modelName;

    @Setter(AccessLevel.PRIVATE)
    private String modelVersion;

    @Setter(AccessLevel.PRIVATE)
    private Double confidence; // [0.0, 1.0]

    @Setter(AccessLevel.PRIVATE)
    private String groundingCitations; // JSON array of citations to system data

    @Setter(AccessLevel.PRIVATE)
    private Boolean requiresReview; // Flag for human review

    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.CorrelationId correlationId;

    @Setter(AccessLevel.PRIVATE)
    private Instant generatedAt;

    private CopilotResponse(Builder builder) {
        this.responseId = "COP-RES-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
        this.requestId = Objects.requireNonNull(builder.requestId, "Request ID cannot be null");
        this.content = Objects.requireNonNull(builder.content, "Content cannot be null");
        this.modelName = builder.modelName;
        this.modelVersion = builder.modelVersion;
        this.confidence = builder.confidence;
        this.groundingCitations = builder.groundingCitations;
        this.requiresReview = builder.requiresReview != null ? builder.requiresReview : true;
        this.correlationId = builder.correlationId != null ? builder.correlationId : CommonKernel.CorrelationId.generate();
        this.generatedAt = Instant.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CopilotResponse)) return false;
        CopilotResponse that = (CopilotResponse) o;
        return responseId != null && responseId.equals(that.responseId);
    }

    @Override
    public int hashCode() {
        return responseId != null ? responseId.hashCode() : 0;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String requestId;
        private String content;
        private String modelName;
        private String modelVersion;
        private Double confidence;
        private String groundingCitations;
        private Boolean requiresReview;
        private CommonKernel.CorrelationId correlationId;

        private Builder() {}

        public Builder requestId(String requestId) {
            this.requestId = Objects.requireNonNull(requestId);
            return this;
        }

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public Builder modelName(String modelName) {
            this.modelName = modelName;
            return this;
        }

        public Builder modelVersion(String modelVersion) {
            this.modelVersion = modelVersion;
            return this;
        }

        public Builder confidence(double confidence) {
            this.confidence = confidence;
            return this;
        }

        public Builder groundingCitations(String groundingCitations) {
            this.groundingCitations = groundingCitations;
            return this;
        }

        public Builder requiresReview(boolean requiresReview) {
            this.requiresReview = requiresReview;
            return this;
        }

        public Builder correlationId(CommonKernel.CorrelationId correlationId) {
            this.correlationId = correlationId;
            return this;
        }

        public CopilotResponse build() {
            return new CopilotResponse(this);
        }
    }
}