package com.assessment;

import com.assessment.Data.schoolClass;
import com.assessment.Data.schoolScores;
import com.assessment.Data.schoolStudent;
import com.assessment.Data.schoolSubject;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.PostgreSQLClient;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Hello world!
 */
public class StudentResult {
    private static List<schoolStudent> schoolStudents = new ArrayList<>();
    private static List<schoolClass> schoolClasses = new ArrayList<>(3);
    private static List<schoolSubject> schoolSubjects = new ArrayList<>();

    private final static List<Integer> terms = Arrays.asList(1, 2, 3);
    private static List<schoolScores> allSchoolScores = new ArrayList<>();

    public static void main(String[] args) {

        initialize_data();

        Vertx vertx = Vertx.vertx();
        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);

        //Add student @POST Method
        router.post("/addStudent")
                .handler(BodyHandler.create())
                .handler(routingContext -> {
                    final schoolStudent schoolStudent = Json.decodeValue(routingContext.getBody(), schoolStudent.class);
                    HttpServerResponse serverResponse = routingContext.response();
                    serverResponse.setChunked(true);
                    schoolStudents.add(schoolStudent);
                    serverResponse.end("Request response code: " + routingContext.response().getStatusCode() + "\n"
                            + "Request response message: " + routingContext.response().getStatusMessage()
                            + "\n" +
                            "Added " + schoolStudents.size() + " student");
                });

        //Add score for student @POST Method
        router.post("/addScore")
                .handler(BodyHandler.create())
                .handler(routingContext -> {
                    final schoolScores studentScore = Json.decodeValue(routingContext.getBody(), schoolScores.class);

                    HttpServerResponse serverResponse = routingContext.response();
                    serverResponse.setChunked(true);

                    allSchoolScores.add(studentScore);
                    serverResponse.end("Request response code: " + routingContext.response().getStatusCode() + "\n"
                            + "Request response message: " + routingContext.response().getStatusMessage()
                            + "\n" +
                            "Added " + Json.encodePrettily(studentScore));
                });

        //Return all students @Get Method
        router.get("/Student")
                .produces("*/json")
                .handler(routingContext -> {

                    routingContext.response().setChunked(true)
                            .end("Request response code: " + routingContext.response().getStatusCode() + "\n"
                                    + "Request response message: " + routingContext.response().getStatusMessage()
                                    + "\n" +
                                    Json.encodePrettily(schoolStudents));
                });

        //Return  all the scores of all subjects of any student, for any selected term, displaying the average score as well
        router.get("/Student/:name/:term")
                .produces("*/json")
                .handler(routingContext -> {

                    String name = routingContext.request().getParam("name");
                    int term = Integer.parseInt(routingContext.request().getParam("term"));
                    schoolStudent schStudent = new schoolStudent();
                    List<schoolScores> studentScores = new ArrayList<>();

                    if (validateTerm(term)) {

                        if (validateStudent(name)) {
                            studentScores = allSchoolScores;
                            schStudent = schoolStudents.stream().filter(n -> n.getStudentName().equals(name)).findFirst().get();
                            int studentId = schStudent.getStudentId();

                            studentScores.removeIf(t -> t.getStudentId() != studentId);
                        } else {
                            routingContext.response().setChunked(true)
                                    .end("Request response code: " + routingContext.response().getStatusCode() + "\n"
                                            + "Request response message: " + routingContext.response().getStatusMessage()
                                            + "\n" + "Students requested does not Exist"
                                    );
                        }
                        studentScores.removeIf(t -> t.getTerm() != term);
                        int x = 0;

                        for (int y = 0; y < studentScores.size(); y++) {
                            x += studentScores.get(y).getScore();
                        }

                        int average = x / studentScores.size();

                        routingContext.response().setChunked(true)
                                .end("Request response code: " + routingContext.response().getStatusCode() + "\n"
                                        + "Request response message: " + routingContext.response().getStatusMessage()
                                        + "\n" + Json.encodePrettily(schStudent)
                                        + "\n" + Json.encodePrettily(studentScores)
                                        + "\n" + " Average score ::: " + average
                                );
                    } else {
                        routingContext.response().setChunked(true)
                                .end("Request response code: " + routingContext.response().getStatusCode() + "\n"
                                        + "Request response message: " + routingContext.response().getStatusMessage()
                                        + "\n" + "term requested does not Exist"
                                );
                    }


                });

        //Return result of any student for a specific subject, class and term
        router.get("/Student/:name/:subject/:class/:term")
                .produces("*/json")
                .handler(routingContext -> {

                    String studentName = routingContext.request().getParam("name");
                    int term = Integer.parseInt(routingContext.request().getParam("term"));
                    int studentClass = Integer.parseInt(routingContext.request().getParam("class"));
                    String subject = routingContext.request().getParam("subject");
                    schoolStudent schStudent = schoolStudents.stream().filter(n -> n.getStudentName().equals(studentName) && n.getStudentClass() == studentClass).findFirst().get();
                    List<schoolScores> studentScores = allSchoolScores;
                    int studentId = schStudent.getStudentId();
                    studentScores.removeIf(t -> t.getStudentId() != studentId);
                    studentScores.removeIf(t -> t.getSubjectId() != schoolSubjects.stream()
                            .filter(s -> s.getSubjectName().equals(subject)).findFirst().get().getSubjectID());
                    studentScores.removeIf(t -> t.getTerm() != term);

                    routingContext.response().setChunked(true)
                            .end("Request response code: " + routingContext.response().getStatusCode() + "\n"
                                    + "Request response message: " + routingContext.response().getStatusMessage()
                                    + "\n" + Json.encodePrettily(schStudent)
                                    + "\n" + Json.encodePrettily(studentScores)

                            );

                });


        /*
        @TODO :::: add registration to know what student is registered for what class
        @TODO :::: verify registration before adding result
        @TODO :::: add postgresql and switch lists to DB calls
         */


        server.requestHandler(router::accept).listen(9001);

        //Database handler
        JsonObject postgreSQLClientConfig = new JsonObject().put("localhost", "school");
        SQLClient postgreSQLClient = PostgreSQLClient.createShared(vertx, postgreSQLClientConfig);


    }

    private static boolean validateStudent(String name) {
        return schoolStudents.stream().filter(t -> t.getStudentName().equals(name)).findFirst().isPresent();
    }

    private static boolean validateTerm(int term) {

        return terms.contains(term);
    }


    private static void initialize_data() {

        schoolStudents = Arrays.asList(
                new schoolStudent(1, "Tamil", 2),
                new schoolStudent(2, "Simbi", 1),
                new schoolStudent(3, "Ade", 1),
                new schoolStudent(4, "Chimuna", 3),
                new schoolStudent(5, "Samuel", 3),
                new schoolStudent(6, "Sylvia", 2)
        );

        schoolClasses = Arrays.asList(
                new schoolClass(1, Arrays.asList(1, 4, 13, 14), "Class A1"),
                new schoolClass(2, Arrays.asList(1, 2, 3, 4), "Class A2"),
                new schoolClass(3, Arrays.asList(1, 4, 5, 6, 8, 7), "Class B1"),
                new schoolClass(4, Arrays.asList(1, 9, 10, 11, 14, 4), "Class B2")
        );

        schoolSubjects = Arrays.asList(
                new schoolSubject("Math", 1),
                new schoolSubject("Arts", 2),
                new schoolSubject("Economics", 3),
                new schoolSubject("English", 4),
                new schoolSubject("Biology", 5),
                new schoolSubject("Physics", 6),
                new schoolSubject("Chemistry", 7),
                new schoolSubject("Geography", 8),
                new schoolSubject("Painting", 9),
                new schoolSubject("Drawing", 10),
                new schoolSubject("Music", 11),

                new schoolSubject("Writing", 12),
                new schoolSubject("General Science", 13),
                new schoolSubject("Accounting", 14)
        );


    }
}
