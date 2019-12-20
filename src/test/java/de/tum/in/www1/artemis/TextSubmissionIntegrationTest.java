package de.tum.in.www1.artemis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

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

import de.tum.in.www1.artemis.domain.*;
import de.tum.in.www1.artemis.domain.enumeration.Language;
import de.tum.in.www1.artemis.domain.participation.StudentParticipation;
import de.tum.in.www1.artemis.repository.ExerciseRepository;
import de.tum.in.www1.artemis.repository.StudentParticipationRepository;
import de.tum.in.www1.artemis.repository.TextSubmissionRepository;
import de.tum.in.www1.artemis.util.DatabaseUtilService;
import de.tum.in.www1.artemis.util.ModelFactory;
import de.tum.in.www1.artemis.util.RequestUtilService;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@ActiveProfiles("artemis")
public class TextSubmissionIntegrationTest {

    @Autowired
    ExerciseRepository exerciseRepo;

    @Autowired
    TextSubmissionRepository submissionRepository;

    @Autowired
    StudentParticipationRepository participationRepository;

    @Autowired
    RequestUtilService request;

    @Autowired
    DatabaseUtilService database;

    private TextExercise textExerciseAfterDueDate;

    private TextExercise textExerciseBeforeDueDate;

    private TextSubmission textSubmission;

    private StudentParticipation afterDueDateParticipation;

    private User student;

    @BeforeEach
    public void initTestCase() {
        student = database.addUsers(1, 1, 1).get(0);
        database.addCourseWithOneTextExerciseDueDateReached();
        textExerciseBeforeDueDate = (TextExercise) database.addCourseWithOneTextExercise().getExercises().iterator().next();
        textExerciseAfterDueDate = (TextExercise) exerciseRepo.findAll().get(0);
        afterDueDateParticipation = database.addParticipationForExercise(textExerciseAfterDueDate, student.getLogin());
        afterDueDateParticipation.setInitializationDate(ZonedDateTime.now().minusDays(2));
        participationRepository.save(afterDueDateParticipation);
        database.addParticipationForExercise(textExerciseBeforeDueDate, student.getLogin());

        textSubmission = ModelFactory.generateTextSubmission("example text", Language.ENGLISH, true);
    }

    @AfterEach
    public void tearDown() {
        database.resetDatabase();
    }

    @Test
    @WithMockUser(value = "tutor1", roles = "TA")
    public void getAllTextSubmissions_studentHiddenForTutor() throws Exception {
        textSubmission = database.addTextSubmissionWithResultAndAssessor(textExerciseAfterDueDate, textSubmission, "student1", "tutor1");

        List<TextSubmission> textSubmissions = request.getList("/api/exercises/" + textExerciseAfterDueDate.getId() + "/text-submissions?assessedByTutor=true", HttpStatus.OK,
                TextSubmission.class);

        assertThat(textSubmissions.size()).as("one text submission was found").isEqualTo(1);
        assertThat(textSubmissions.get(0).getId()).as("correct text submission was found").isEqualTo(textSubmission.getId());
        assertThat(((StudentParticipation) textSubmissions.get(0).getParticipation()).getStudent()).as("student of participation is hidden").isNull();
    }

    @Test
    @WithMockUser(username = "instructor1", roles = "INSTRUCTOR")
    public void getAllTextSubmissions_studentVisibleForInstructor() throws Exception {
        textSubmission = database.addTextSubmission(textExerciseAfterDueDate, textSubmission, "student1");

        List<TextSubmission> textSubmissions = request.getList("/api/exercises/" + textExerciseAfterDueDate.getId() + "/text-submissions", HttpStatus.OK, TextSubmission.class);

        assertThat(textSubmissions.size()).as("one text submission was found").isEqualTo(1);
        assertThat(textSubmissions.get(0).getId()).as("correct text submission was found").isEqualTo(textSubmission.getId());
        assertThat(((StudentParticipation) textSubmissions.get(0).getParticipation()).getStudent()).as("student of participation is hidden").isNotNull();
    }

    @Test
    @WithMockUser(value = "tutor1", roles = "TA")
    public void getTextSubmissionWithoutAssessment_studentHidden() throws Exception {
        textSubmission = database.addTextSubmission(textExerciseAfterDueDate, textSubmission, "student1");

        TextSubmission textSubmissionWithoutAssessment = request.get("/api/exercises/" + textExerciseAfterDueDate.getId() + "/text-submission-without-assessment", HttpStatus.OK,
                TextSubmission.class);

        assertThat(textSubmissionWithoutAssessment).as("text submission without assessment was found").isNotNull();
        assertThat(textSubmissionWithoutAssessment.getId()).as("correct text submission was found").isEqualTo(textSubmission.getId());
        assertThat(((StudentParticipation) textSubmissionWithoutAssessment.getParticipation()).getStudent()).as("student of participation is hidden").isNull();
    }

    @Test
    @WithMockUser(value = "tutor1", roles = "TA")
    public void getTextSubmissionWithoutAssessment_lockSubmission() throws Exception {
        User user = database.getUserByLogin("tutor1");
        textSubmission = database.addTextSubmission(textExerciseAfterDueDate, textSubmission, "student1");

        TextSubmission storedSubmission = request.get("/api/exercises/" + textExerciseAfterDueDate.getId() + "/text-submission-without-assessment?lock=true", HttpStatus.OK,
                TextSubmission.class);

        // set dates to UTC and round to milliseconds for comparison
        textSubmission.setSubmissionDate(ZonedDateTime.ofInstant(textSubmission.getSubmissionDate().truncatedTo(ChronoUnit.MILLIS).toInstant(), ZoneId.of("UTC")));
        storedSubmission.setSubmissionDate(ZonedDateTime.ofInstant(storedSubmission.getSubmissionDate().truncatedTo(ChronoUnit.MILLIS).toInstant(), ZoneId.of("UTC")));
        assertThat(storedSubmission).as("submission was found").isEqualToIgnoringGivenFields(textSubmission, "result");
        assertThat(storedSubmission.getResult()).as("result is set").isNotNull();
        assertThat(storedSubmission.getResult().getAssessor()).as("assessor is tutor1").isEqualTo(user);
        checkDetailsHidden(storedSubmission, false);
    }

    @Test
    @WithMockUser(value = "tutor1", roles = "TA")
    public void getTextSubmissionWithoutAssessment_noSubmittedSubmission_notFound() throws Exception {
        TextSubmission submission = ModelFactory.generateTextSubmission("text", Language.ENGLISH, false);
        database.addTextSubmission(textExerciseAfterDueDate, submission, "student1");

        request.get("/api/exercises/" + textExerciseAfterDueDate.getId() + "/text-submission-without-assessment", HttpStatus.NOT_FOUND, TextSubmission.class);
    }

    @Test
    @WithMockUser(value = "tutor1", roles = "TA")
    public void getTextSubmissionWithoutAssessment_dueDateNotOver() throws Exception {
        textSubmission = database.addTextSubmission(textExerciseBeforeDueDate, textSubmission, "student1");

        request.get("/api/exercises/" + textExerciseBeforeDueDate.getId() + "/text-submission-without-assessment", HttpStatus.NOT_FOUND, TextSubmission.class);
    }

    @Test
    @WithMockUser(value = "student1")
    public void getTextSubmissionWithoutAssessment_asStudent_forbidden() throws Exception {
        textSubmission = database.addTextSubmission(textExerciseAfterDueDate, textSubmission, "student1");

        request.get("/api/exercises/" + textExerciseAfterDueDate.getId() + "/text-submission-without-assessment", HttpStatus.FORBIDDEN, TextSubmission.class);
    }

    @Test
    @WithMockUser(username = "student1")
    public void getResultsForCurrentStudent_assessorHiddenForStudent() throws Exception {
        textSubmission = ModelFactory.generateTextSubmission("Some text", Language.ENGLISH, true);
        database.addTextSubmissionWithResultAndAssessor(textExerciseAfterDueDate, textSubmission, "student1", "tutor1");

        Exercise returnedExercise = request.get("/api/exercises/" + textExerciseAfterDueDate.getId() + "/details", HttpStatus.OK, Exercise.class);

        assertThat(returnedExercise.getStudentParticipations().iterator().next().getResults().iterator().next().getAssessor()).as("assessor is null").isNull();
    }

    @Test
    @WithMockUser(value = "student1", roles = "USER")
    public void getDataForTextEditorWithResult() throws Exception {
        TextSubmission textSubmission = database.addTextSubmissionWithResultAndAssessor(textExerciseAfterDueDate, this.textSubmission, "student1", "tutor1");
        Long participationId = textSubmission.getParticipation().getId();

        StudentParticipation participation = request.get("/api/text-editor/" + participationId, HttpStatus.OK, StudentParticipation.class);

        assertThat(participation.getResults(), is(notNullValue()));
        assertThat(participation.getResults(), hasSize(1));

        assertThat(participation.getSubmissions(), is(notNullValue()));
    }

    @Test
    @WithMockUser(value = "student1", roles = "USER")
    public void submitExercise_afterDueDate_forbidden() throws Exception {
        request.put("/api/exercises/" + textExerciseAfterDueDate.getId() + "/text-submissions", textSubmission, HttpStatus.FORBIDDEN);
    }

    @Test
    @WithMockUser(value = "student1", roles = "USER")
    public void submitExercise_beforeDueDate_allowed() throws Exception {
        request.put("/api/exercises/" + textExerciseBeforeDueDate.getId() + "/text-submissions", textSubmission, HttpStatus.OK);
    }

    @Test
    @WithMockUser(value = "student1", roles = "USER")
    public void submitExercise_beforeDueDateWithTwoSubmissions_allowed() throws Exception {
        final var submitPath = "/api/exercises/" + textExerciseBeforeDueDate.getId() + "/text-submissions";
        final var newSubmissionText = "Some other test text";
        textSubmission = request.putWithResponseBody(submitPath, textSubmission, TextSubmission.class, HttpStatus.OK);
        textSubmission.setText(newSubmissionText);
        request.put(submitPath, textSubmission, HttpStatus.OK);

        final var submissionInDb = submissionRepository.findById(textSubmission.getId());
        assertThat(submissionInDb.isPresent());
        assertThat(submissionInDb.get().getText()).isEqualTo(newSubmissionText);
    }

    @Test
    @WithMockUser(value = "student1", roles = "USER")
    public void submitExercise_afterDueDateWithParticipationStartAfterDueDate_allowed() throws Exception {
        afterDueDateParticipation.setInitializationDate(ZonedDateTime.now());
        participationRepository.save(afterDueDateParticipation);

        request.put("/api/exercises/" + textExerciseBeforeDueDate.getId() + "/text-submissions", textSubmission, HttpStatus.OK);
    }

    private void checkDetailsHidden(TextSubmission submission, boolean isStudent) {
        assertThat(submission.getParticipation().getResults()).as("results are hidden in participation").isNullOrEmpty();
        if (isStudent) {
            assertThat(submission.getResult()).as("result is hidden").isNull();
        }
        else {
            assertThat(((StudentParticipation) submission.getParticipation()).getStudent()).as("student of participation is hidden").isNull();
        }
    }
}
