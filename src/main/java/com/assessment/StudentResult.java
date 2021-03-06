package com.assessment;

import com.assessment.Data.*;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlConnection;

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

    private static List<Registrations> registrations = new ArrayList<>();
    private final static List<Integer> terms = Arrays.asList(1, 2, 3);
    private static List<schoolScores> allSchoolScores = new ArrayList<>();

    public static void main(String[] args) {

        initialize_data();

        Vertx vertx = Vertx.vertx();
        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);

        //Add student @POST Method
        router.post("/addStudent/:term")
                .handler(BodyHandler.create())
                .handler(routingContext -> {
                    final schoolStudent schoolStudent = Json.decodeValue(routingContext.getBody(), schoolStudent.class);
                    int term = Integer.parseInt(routingContext.request().getParam("term"));

                    if (validateTerm(term)) {


                        HttpServerResponse serverResponse = routingContext.response();
                        serverResponse.setChunked(true);
                        schoolStudents.add(schoolStudent);
                        registrations.add(new Registrations(schoolStudent.getStudentId(), schoolStudent.getStudentClass(), term));
                        serverResponse.end("Request response code: " + routingContext.response().getStatusCode() + "\n"
                                + "Request response message: " + routingContext.response().getStatusMessage()
                                + "\n" +
                                "New Student Registration created for " + term + "\n" + schoolStudent);
                    } else {
                        routingContext.response().setChunked(true)
                                .end("Request response code: " + routingContext.response().getStatusCode() + "\n"
                                        + "Request response message: " + routingContext.response().getStatusMessage()
                                        + "\n" + "term requested does not Exist"
                                );
                    }
                });

        //Add score for student @POST Method
        router.post("/addScore")
                .handler(BodyHandler.create())
                .handler(routingContext -> {
                    final schoolScores studentScore = Json.decodeValue(routingContext.getBody(), schoolScores.class);

                    HttpServerResponse serverResponse = routingContext.response();
                    serverResponse.setChunked(true);

                    if (checkRegistration(studentScore.getStudentId(), studentScore.getTerm())) {
                        if (studentScore.getScore() > 100 || studentScore.getScore() < 0) {
                            serverResponse.end("Request response code: " + routingContext.response().getStatusCode() + "\n"
                                    + "Request response message: " + routingContext.response().getStatusMessage()
                                    + "\n" +
                                    "Invalid score " + studentScore.getScore());
                        }
                        allSchoolScores.add(studentScore);
                        serverResponse.end("Request response code: " + routingContext.response().getStatusCode() + "\n"
                                + "Request response message: " + routingContext.response().getStatusMessage()
                                + "\n" +
                                "Added " + Json.encodePrettily(studentScore));
                    } else {
                        serverResponse.end("Request response code: " + routingContext.response().getStatusCode() + "\n"
                                + "Request response message: " + routingContext.response().getStatusMessage()
                                + "\n" +
                                "No term " + studentScore.getTerm()
                                + " registration found for Student "
                                + schoolStudents.stream().filter(t -> t.getStudentId() == studentScore.getStudentId()).findFirst().get().getStudentName());
                    }


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

                            if (checkRegistration(studentId, term)) {
                                studentScores.removeIf(t -> t.getStudentId() != studentId);

                            } else {
                                routingContext.response().setChunked(true)
                                        .end("Request response code: " + routingContext.response().getStatusCode() + "\n"
                                                + "Request response message: " + routingContext.response().getStatusMessage()
                                                + "\n" + "Students not registered for term " + term
                                        );
                            }

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


        server.requestHandler(router::accept).listen(9001);

        //Database handler
//        JsonObject postgreSQLClientConfig = new JsonObject().put("localhost", "school");
//        SQLClient postgreSQLClient = PostgreSQLClient.createShared(vertx, postgreSQLClientConfig, "PostgreSQLPool1");
//
//        postgreSQLClient.getConnection(res -> {
//            if (res.succeeded()) {
//
//                SQLConnection connection = res.result();
//
//                // Got a connection
//
//            } else {
//                // Failed to get connection - deal with it
//            }
//        });

        PgConnectOptions connectOptions = new PgConnectOptions()
                .setPort(5432)
                .setHost("the-host")
                .setDatabase("the-db")
                .setUser("user")
                .setPassword("secret");

// Pool options
        PoolOptions poolOptions = new PoolOptions()
                .setMaxSize(5);

// Create the pooled client
        PgPool client = PgPool.pool(vertx, connectOptions, poolOptions);

// Get a connection from the pool
        client.getConnection(ar1 -> {

            if (ar1.succeeded()) {

                System.out.println("Connected");

                // Obtain our connection
                SqlConnection conn = ar1.result();

                // All operations execute on the same connection
                conn
                        .query("SELECT * FROM users WHERE id='postgres'")
                        .execute();
            } else {
                System.out.println("Could not connect: " + ar1.cause().getMessage());
            }
        });
    }

    private static boolean checkRegistration(int studentId, int term) {

        return registrations.stream().filter(t -> t.getStudentID() == studentId && t.getTerm() == term).findFirst().isPresent();
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
