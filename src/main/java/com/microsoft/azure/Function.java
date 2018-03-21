package com.microsoft.azure;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.microsoft.azure.serverless.functions.ExecutionContext;
import com.microsoft.azure.serverless.functions.HttpRequestMessage;
import com.microsoft.azure.serverless.functions.HttpResponseMessage;
import com.microsoft.azure.serverless.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.serverless.functions.annotation.FunctionName;
import com.microsoft.azure.serverless.functions.annotation.HttpTrigger;

/**
 * Azure Functions with HTTP Trigger.
 */
public class Function {

    /**
     * This function listens at endpoint "/api/hello". Two ways to invoke it using "curl" command in bash:
     * 1. curl -d "HTTP Body" {your host}/api/hello
     * 2. curl {your host}/api/hello?name=HTTP%20Query
     */
    @FunctionName("hello")
    public HttpResponseMessage<String> hello(
            @HttpTrigger(name = "req", methods = { "get",
                    "post" }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");

        // Parse query parameter
        String query = request.getQueryParameters().get("name");
        String name = request.getBody().orElse(query);

        if (name == null) {
            return request.createResponse(400, "Please pass a name on the query string or in the request body");
        } else {
            context.getLogger().info("Going to call DataStore class");
            new DataStore(context.getLogger()).save(name);
            return request.createResponse(200, "Hello, " + name + ".");
        }
    }

    /**
     * This function listens at endpoint "/api/greetedPeople". One simple way to invoke it using "curl" command in bash:
     * 1. curl {your host}/api/greetedPeople
     */
    @FunctionName("greetedPeople")
    public HttpResponseMessage<String> greetedPeople(
            @HttpTrigger(name = "req", methods = {
                    "get" }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");

        return request.createResponse(200, String.format("{\"greetedPeople\":[%s]}", new DataStore(context.getLogger())
                .greetedPeople().stream().map(s -> String.format("'%s'", s)).collect(Collectors.joining(","))));
    }

}
