package de.tum.in.www1.artemis.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import de.tum.in.www1.artemis.domain.GradingCriteria;
import de.tum.in.www1.artemis.domain.GradingInstruction;
import de.tum.in.www1.artemis.repository.GradingInstructionRepository;
import de.tum.in.www1.artemis.web.rest.errors.EntityNotFoundException;

/**
 * Service Implementation for managing Structured Grading Instructions.
 */
@Service
public class GradingInstructionService {

    private final Logger log = LoggerFactory.getLogger(GradingInstructionService.class);

    private final GradingInstructionRepository gradingInstructionRepository;

    public GradingInstructionService(GradingInstructionRepository gradingInstructionRepository) {
        this.gradingInstructionRepository = gradingInstructionRepository;
    }

    /**
     * Save a grading instruction.
     *
     * @param gradingInstruction the entity to save
     * @return the persisted entity
     */
    public GradingInstruction save(GradingInstruction gradingInstruction) {
        log.debug("Request to save Grading Instruction : {}", gradingInstruction);
        return gradingInstructionRepository.save(gradingInstruction);

    }

    /**
     * Delete the grading instruction by id.
     * @param gradingInstruction the grading instruction to be deleted
     */

    public void delete(GradingInstruction gradingInstruction) {
        log.info("GradingInstructionService.Request to delete Grading Instruction : {}", gradingInstruction.getId());
        gradingInstructionRepository.delete(gradingInstruction);
    }

    /**
     * Get one grading instruction by gradingInstructionId.
     *
     * @param gradingInstructionId the gradingInstructionId of the entity
     * @return the entity
     */

    public GradingInstruction findOne(long gradingInstructionId) {
        return gradingInstructionRepository.findById(gradingInstructionId)
                .orElseThrow(() -> new EntityNotFoundException("Grading Instruction with gradingInstructionId  " + gradingInstructionId + " does not exist!"));
    }

    /**
     * Finds all Grading Instructions for a given GradingCriteria
     *
     * @param gradingCriteria corresponding criteria
     * @return a List of all Grading Instructions for the given criteria
     */

    public List<GradingInstruction> findAllForCriteria(GradingCriteria gradingCriteria) {
        return gradingInstructionRepository.findByGradingCriteriaId(gradingCriteria.getId());
    }
}
