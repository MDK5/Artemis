package de.tum.in.www1.artemis;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;

import de.tum.in.www1.artemis.domain.Course;
import de.tum.in.www1.artemis.domain.enumeration.DifficultyLevel;
import de.tum.in.www1.artemis.domain.quiz.*;
import de.tum.in.www1.artemis.repository.*;
import de.tum.in.www1.artemis.util.DatabaseUtilService;
import de.tum.in.www1.artemis.util.ModelFactory;
import de.tum.in.www1.artemis.util.RequestUtilService;

public class QuizSubmissionIntegrationTest extends AbstractSpringIntegrationTest {

    @Autowired
    DatabaseUtilService database;

    @Autowired
    RequestUtilService request;

    @Autowired
    CourseRepository courseRepository;

    @Autowired
    ExerciseRepository exerciseRepository;

    @Autowired
    ParticipationRepository participationRepository;

    @Autowired
    SubmissionRepository submissionRepository;

    @Autowired
    ResultRepository resultRepository;

    @BeforeEach
    public void init() {
        database.addUsers(10, 5, 1);
    }

    @AfterEach
    public void tearDown() {
        database.resetDatabase();
    }

    @Test
    @WithMockUser(value = "student1", roles = "USER")
    public void submitForPractice_asStudent() throws Exception {
        QuizExercise quizExercise = createQuizOnServer();
        QuizExercise updatedQuizExercise = request.postWithResponseBody("/api/exercises/" + quizExercise.getId() + "/submissions/practice", quizExercise, QuizExercise.class,
            HttpStatus.OK);
        // TODO: add some additional checks for the retrieved data
    }

    private QuizExercise createQuizOnServer() throws Exception {
        List<Course> courses = database.createCoursesWithExercisesAndLectures();
        Course course = courses.get(0);

        QuizExercise quizExercise = createQuiz(course);
        return request.postWithResponseBody("/api/quiz-exercises", quizExercise, QuizExercise.class, HttpStatus.CREATED);
        // TODO: add some checks
    }
    @NotNull
    private QuizExercise createQuiz(Course course) {
        QuizExercise quizExercise = ModelFactory.generateQuizExercise(ZonedDateTime.now().plusHours(5), null, course);
        quizExercise.addQuestions(createMultipleChoiceQuestion());
        quizExercise.addQuestions(createDragAndDropQuestion());
        quizExercise.addQuestions(createShortAnswerQuestion());
        return quizExercise;
    }

    @NotNull
    private ShortAnswerQuestion createShortAnswerQuestion() {
        ShortAnswerQuestion sa = (ShortAnswerQuestion) new ShortAnswerQuestion().title("SA").score(2).text("This is a long answer text");
        var shortAnswerSpot1 = new ShortAnswerSpot().spotNr(0).width(1);
        shortAnswerSpot1.setTempID(generateTempId());
        var shortAnswerSpot2 = new ShortAnswerSpot().spotNr(2).width(2);
        shortAnswerSpot2.setTempID(generateTempId());
        sa.getSpots().add(shortAnswerSpot1);
        sa.getSpots().add(shortAnswerSpot2);
        var shortAnswerSolution1 = new ShortAnswerSolution().text("is");
        shortAnswerSolution1.setTempID(generateTempId());
        var shortAnswerSolution2 = new ShortAnswerSolution().text("long");
        shortAnswerSolution2.setTempID(generateTempId());
        sa.getSolutions().add(shortAnswerSolution1);
        sa.getSolutions().add(shortAnswerSolution2);
        sa.getCorrectMappings().add(new ShortAnswerMapping().spot(sa.getSpots().get(0)).solution(sa.getSolutions().get(0)));
        sa.getCorrectMappings().add(new ShortAnswerMapping().spot(sa.getSpots().get(1)).solution(sa.getSolutions().get(1)));
        return sa;
    }

    @NotNull
    private DragAndDropQuestion createDragAndDropQuestion() {
        DragAndDropQuestion dnd = (DragAndDropQuestion) new DragAndDropQuestion().title("DnD").score(1).text("Q2");
        var dropLocation1 = new DropLocation().posX(10).posY(10).height(10).width(10);
        dropLocation1.setTempID(generateTempId());
        var dropLocation2 = new DropLocation().posX(20).posY(20).height(10).width(10);
        dropLocation2.setTempID(generateTempId());
        dnd.getDropLocations().add(dropLocation1);
        dnd.getDropLocations().add(dropLocation2);
        var dragItem1 = new DragItem().text("D1");
        dragItem1.setTempID(generateTempId());
        var dragItem2 = new DragItem().text("D2");
        dragItem2.setTempID(generateTempId());
        dnd.getDragItems().add(dragItem1);
        dnd.getDragItems().add(dragItem2);
        dnd.getCorrectMappings().add(new DragAndDropMapping().dragItem(dragItem1).dropLocation(dropLocation1));
        dnd.getCorrectMappings().add(new DragAndDropMapping().dragItem(dragItem2).dropLocation(dropLocation2));
        return dnd;
    }

    private Long generateTempId() {
        return ThreadLocalRandom.current().nextLong(Long.MAX_VALUE);
    }

    @NotNull
    private MultipleChoiceQuestion createMultipleChoiceQuestion() {
        MultipleChoiceQuestion mc = (MultipleChoiceQuestion) new MultipleChoiceQuestion().title("MC").score(1).text("Q1");
        mc.getAnswerOptions().add(new AnswerOption().text("A").hint("H1").explanation("E1").isCorrect(true));
        mc.getAnswerOptions().add(new AnswerOption().text("B").hint("H2").explanation("E2").isCorrect(false));
        return mc;
    }

}
