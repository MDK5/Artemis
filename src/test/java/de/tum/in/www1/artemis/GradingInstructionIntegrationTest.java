package de.tum.in.www1.artemis;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Iterator;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import de.tum.in.www1.artemis.domain.Exercise;
import de.tum.in.www1.artemis.domain.GradingInstruction;
import de.tum.in.www1.artemis.repository.CourseRepository;
import de.tum.in.www1.artemis.repository.ExerciseRepository;
import de.tum.in.www1.artemis.repository.GradingInstructionRepository;
import de.tum.in.www1.artemis.service.GradingInstructionService;
import de.tum.in.www1.artemis.util.DatabaseUtilService;
import de.tum.in.www1.artemis.util.RequestUtilService;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@ActiveProfiles("artemis")

public class GradingInstructionIntegrationTest {

    @Autowired
    ExerciseRepository exerciseRepo;

    @Autowired
    RequestUtilService request;

    @Autowired
    DatabaseUtilService database;

    @Autowired
    CourseRepository courseRepository;

    @Autowired
    ExerciseRepository exerciseRepository;

    @Autowired
    GradingInstructionRepository gradingInstructionRepository;

    @Autowired
    GradingInstructionService gradingInstructionService;

    private Exercise exercise;

    private Set<GradingInstruction> gradingInstructionSet;

    private Iterator<GradingInstruction> iterator;

    @BeforeEach
    public void initTestCase() {
        database.addUsers(1, 2, 1);
        database.addCourseWithOneTextExercise();
        long courseID = courseRepository.findAllActive().get(0).getId();
        exercise = exerciseRepository.findByCourseId(courseID).get(0);

        gradingInstructionSet = database.addGradingInstructionsToExercise(exercise);
        iterator = gradingInstructionSet.iterator();
    }

    @AfterEach
    public void tearDown() {
        database.resetDatabase();
    }

    @Test
    @WithMockUser(username = "tutor1", roles = "TA")
    public void createGradingInstruction_asTutor_forbidden() throws Exception {
        for (GradingInstruction gradingInstruction : gradingInstructionSet) {
            request.post("/api/grading-instructions", gradingInstruction, HttpStatus.FORBIDDEN);
        }
        assertThat(gradingInstructionRepository.findAll().isEmpty()).isTrue();
    }

    @Test
    @WithMockUser(username = "instructor1", roles = "INSTRUCTOR")
    public void createGradingInstruction() throws Exception {
        for (GradingInstruction gradingInstruction : gradingInstructionSet) {
            gradingInstruction = request.postWithResponseBody("/api/grading-instructions", gradingInstruction, GradingInstruction.class);
            assertThat(gradingInstruction).isNotNull();
            assertThat(gradingInstruction.getId()).isNotNull();
            assertThat(gradingInstruction.getInstructionDescription()).isNotNull();
        }
        assertThat(gradingInstructionSet.size()).isEqualTo(2);
        GradingInstruction gradingInstruction = new GradingInstruction();
        gradingInstruction.setId(1l);
        request.postWithResponseBody("/api/grading-instructions", gradingInstruction, GradingInstruction.class, HttpStatus.BAD_REQUEST);
        assertThat(gradingInstructionRepository.findAll()).as("Grading instruction has not been stored").size().isEqualTo(2);
    }

    @Test
    @WithMockUser(username = "instructor1", roles = "INSTRUCTOR")
    public void deleteGradingInstruction() throws Exception {
        for (GradingInstruction gradingInstruction : gradingInstructionSet) {
            GradingInstruction savedGradingInstruction = gradingInstructionRepository.save(gradingInstruction);
            request.delete("/api/grading-instructions/" + savedGradingInstruction.getId(), HttpStatus.OK);
        }
        assertThat(gradingInstructionRepository.findAll().isEmpty()).isTrue();
        request.delete("/api/grading-instructions/" + null, HttpStatus.BAD_REQUEST);
    }

    @Test
    @WithMockUser(username = "tutor1", roles = "TA")
    public void deleteGradingInstruction_asTutor_forbidden() throws Exception {
        for (GradingInstruction gradingInstruction : gradingInstructionSet) {
            GradingInstruction savedGradingInstruction = gradingInstructionRepository.save(gradingInstruction);
            request.delete("/api/grading-instructions/" + savedGradingInstruction.getId(), HttpStatus.FORBIDDEN);
        }
        assertThat(gradingInstructionRepository.findAll().isEmpty()).isFalse();
        assertThat(gradingInstructionRepository.findAll().size()).isEqualTo(2);
    }

    @Test
    @WithMockUser(value = "student1", roles = "USER")
    public void getAllGradingInstructionsOfExercise_asStudent_forbidden() throws Exception {
        for (GradingInstruction gradingInstruction : gradingInstructionSet) {
            gradingInstructionRepository.save(gradingInstruction);
        }
        request.getList("/api/exercises/" + exercise.getId() + "/grading-instructions", HttpStatus.FORBIDDEN, GradingInstruction.class);
    }

    @Test
    @WithMockUser(username = "tutor2", roles = "TA")
    public void getAllGradingInstructionsOfExercise() throws Exception {
        for (GradingInstruction gradingInstruction : gradingInstructionSet) {
            gradingInstructionRepository.save(gradingInstruction);
        }
        request.getList("/api/exercises/" + exercise.getId() + "/grading-instructions", HttpStatus.OK, GradingInstruction.class);
        assertThat(gradingInstructionRepository.findAll().isEmpty()).isFalse();
        assertThat(gradingInstructionRepository.findAll().size()).isEqualTo(2);
        request.getList("/api/exercises/" + null + "/grading-instructions", HttpStatus.BAD_REQUEST, GradingInstruction.class);
    }

    @Test
    @WithMockUser(username = "instructor1", roles = "INSTRUCTOR")
    public void updateGradingInstruction() throws Exception {
        for (GradingInstruction gradingInstruction : gradingInstructionSet) {
            gradingInstructionRepository.save(gradingInstruction);
            gradingInstruction.setCredits(0.5);
            gradingInstruction.setGradingScale("bad");
            gradingInstruction.setInstructionDescription("UPDATE DESCRIPTION");
            gradingInstruction.setFeedback("UPDATE FEEDBACK");
            gradingInstruction.setUsageCount(0);
            gradingInstruction = request.putWithResponseBody("/api/grading-instructions", gradingInstruction, GradingInstruction.class, HttpStatus.OK);
            assertThat(gradingInstruction).isNotNull();
            assertThat(gradingInstruction.getId()).isNotNull();
            assertThat(gradingInstruction.getCredits()).isEqualTo(0.5);
            assertThat(gradingInstruction.getGradingScale()).isEqualTo("bad");
            assertThat(gradingInstruction.getInstructionDescription()).isEqualTo("UPDATE DESCRIPTION");
            assertThat(gradingInstruction.getFeedback()).isEqualTo("UPDATE FEEDBACK");
            assertThat(gradingInstruction.getUsageCount()).isEqualTo(0);

            request.putWithResponseBody("/api/grading-instructions", null, GradingInstruction.class, HttpStatus.BAD_REQUEST);
        }
        assertThat(gradingInstructionSet.size()).isEqualTo(2);
        assertThat(gradingInstructionRepository.findAll().isEmpty()).isFalse();
        assertThat(gradingInstructionRepository.findAll().size()).isEqualTo(2);
    }

    @Test
    @WithMockUser(username = "tutor2", roles = "TA")
    public void updateGradingInstruction_asTutor_forbidden() throws Exception {
        for (GradingInstruction gradingInstruction : gradingInstructionSet) {
            gradingInstructionRepository.save(gradingInstruction);
            request.putWithResponseBody("/api/grading-instructions", gradingInstruction, GradingInstruction.class, HttpStatus.FORBIDDEN);
        }
        assertThat(gradingInstructionRepository.findAll().size()).isEqualTo(2);
    }
}
