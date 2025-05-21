package app;

import io.javalin.Javalin;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Gateway {

    public static void main(String[] args) {
        HttpClient client = HttpClient.newHttpClient();

        Javalin app = Javalin.create().start(7070);
        app.get("/", ctx -> {
            ctx.result("Welcome to the Real Estate API Gateway!");
        });
        //  post, sales by post code, salesby, sales by download date, get
        app.get("/sales", ctx -> {

                    // Forward to Property Server
                    HttpRequest propertyRequest = HttpRequest.newBuilder()
                            .uri(URI.create("http://localhost:7071/sales"))
                            .GET()
                            .build();

            // Fire and forget analytics increment

            // Await property data and return to user
            HttpResponse<String> response = client.send(propertyRequest, HttpResponse.BodyHandlers.ofString());
            ctx.result(response.body());
                });

        // Sales by Post Code
        app.get("/sales/Post_Code/{Post_Code}", ctx -> {

            // Forward to Property Server
            HttpRequest propertyRequest = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:7071/sales/postcode/" + ctx.pathParam("Post_Code")))
                    .GET()
                    .build();
            HttpRequest analyticsRequest = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:7072/track/postcode/" + ctx.pathParam("Post_Code")))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            client.sendAsync(analyticsRequest, HttpResponse.BodyHandlers.discarding());

            // Await property data and return to user
            HttpResponse<String> response = client.send(propertyRequest, HttpResponse.BodyHandlers.ofString());
            ctx.result(response.body());
        });

        // salesby
        app.get("/salesby", ctx -> {

            // Forward to Property Server
            HttpRequest propertyRequest = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:7071/salesby/"))
                    .GET()
                    .build();


            // Await property data and return to user
            HttpResponse<String> analyticsRequest = client.send(propertyRequest, HttpResponse.BodyHandlers.ofString());
            ctx.result(analyticsRequest.body());
        });


        // Sales by Download Date
        app.get("/sales/download_date/{download_date}", ctx -> {

            // Forward to Property Server
            HttpRequest propertyRequest = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:7071/sales/date/" + ctx.pathParam("download_date")))
                    .GET()
                    .build();

            // Forward to Analytics Server
            HttpRequest analyticsRequest = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:7072/track/date/" + ctx.pathParam("download_date")))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            client.sendAsync(analyticsRequest, HttpResponse.BodyHandlers.discarding());

            // Await property data and return to user
            HttpResponse<String> response = client.send(propertyRequest, HttpResponse.BodyHandlers.ofString());
            ctx.result(response.body());
        });


        // Route: GET /sales/:id
        app.get("/sales/{id}", ctx -> {
            String id = ctx.pathParam("id");

            // Forward to Property Server
            HttpRequest propertyRequest = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:7071/sales/" + id))
                    .GET()
                    .build();

            // Tell Analytics Server to increment ID count
            HttpRequest analyticsRequest = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:7072/track/id/" + id))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();



            // Fire and forget analytics increment
            client.sendAsync(analyticsRequest, HttpResponse.BodyHandlers.discarding());

            // Await property data and return to user
            HttpResponse<String> response = client.send(propertyRequest, HttpResponse.BodyHandlers.ofString());
            ctx.result(response.body());
        });

        // count for ID search
        app.get("/track/ID/{id}", ctx -> {
            String id = ctx.pathParam("id");

            // Forward to Property Server
            HttpRequest propertyRequest = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:7072/track/ID_Count/" + id))
                    .GET()
                    .build();

            // Await property data and return to user
            HttpResponse<String> response = client.send(propertyRequest, HttpResponse.BodyHandlers.ofString());
            ctx.result(response.body());
        });

        app.get("/track/Post_Code/{post}", ctx -> {
            String post = ctx.pathParam("post");

            // Forward to Property Server
            HttpRequest propertyRequest = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:7072/track/Post_Count/" + post))
                    .GET()
                    .build();

            // Await property data and return to user
            HttpResponse<String> response = client.send(propertyRequest, HttpResponse.BodyHandlers.ofString());
            ctx.result(response.body());
        });

    }
}
