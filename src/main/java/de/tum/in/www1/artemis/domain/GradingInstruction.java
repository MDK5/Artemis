package de.tum.in.www1.artemis.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A Structured Grading Instruction.
 */
@Entity
@Table(name = "grading_instruction")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class GradingInstruction implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Exercise exercise;

    @Column(name = "credits")
    private Double credits;

    @Column(name = "level")
    private String level;

    @Enumerated(EnumType.STRING)
    @Column(name = "instruction_description")
    @Lob
    private String instructionDescription;

    @Column(name = "feedback")
    @Lob
    private String feedback;

    @Column(name = "usage_count")
    private int usageCount;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public Double getCredits() {
        return credits;
    }
    public GradingInstruction credits(Double credits) {
        this.credits = credits;
        return this;
    }
    public void setCredits(Double credits) {
        this.credits = credits;
    }

    public void setUsageCount(int usageCount) {
        this.usageCount = usageCount;
    }
    public int getUsageCount() {
        return usageCount;
    }
    public GradingInstruction usageCount(int usageCount) {
        this.usageCount = usageCount;
        return this;
    }

    public Exercise getExercise() {
        return exercise;
    }
    public GradingInstruction result(Exercise exercise) {
        this.exercise = exercise;
        return this;
    }
    public void setExercise(Exercise exercise) {
        this.exercise = exercise;
    }

    public String getInstructionDescription() {
        return instructionDescription;
    }
    public GradingInstruction instructionDescription(String instructionDescription) {
        this.instructionDescription = instructionDescription;
        return this;
    }
    public void setInstructionDescription(String instructionDescription) {
        this.instructionDescription = instructionDescription;
    }
    public String getLevel() {
        return level;
    }
    public GradingInstruction level(String level) {
        this.level = level;
        return this;
    }
    public void setLevel(String level) {
        this.level = level;
    }

    public String getFeedback() {
        return feedback;
    }
    public GradingInstruction feedback(String feedback) {
        this.instructionDescription = feedback;
        return this;
    }
    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GradingInstruction gradingInstruction = (GradingInstruction) o;
        if (gradingInstruction.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), gradingInstruction.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "GradingInstruction{" + "id=" + getId() + ", credits='" + getCredits() + "'" + ", level='" + getLevel() + "'" + ", instructionDescription='"
            + getInstructionDescription() + "'" + ", feedback='" + getFeedback() +  ", usageCount='" + getUsageCount() + "'" + "}";
    }

}
