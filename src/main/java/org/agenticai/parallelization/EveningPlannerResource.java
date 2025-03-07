package org.agenticai.parallelization;

import io.quarkus.logging.Log;
import io.serverlessworkflow.api.Workflow;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Vertx;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import static org.kie.kogito.serverless.workflow.fluent.ActionBuilder.call;
import static org.kie.kogito.serverless.workflow.fluent.FunctionBuilder.expr;
import static org.kie.kogito.serverless.workflow.fluent.FunctionBuilder.java;
import static org.kie.kogito.serverless.workflow.fluent.StateBuilder.operation;
import static org.kie.kogito.serverless.workflow.fluent.StateBuilder.parallel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import org.agenticai.parallelization.EveningPlannerResource.EveningPlan;
import org.kie.kogito.serverless.workflow.executor.StaticWorkflowApplication;
import org.kie.kogito.serverless.workflow.fluent.WorkflowBuilder;

import com.fasterxml.jackson.databind.JsonNode;

@Path("/evening")
public class EveningPlannerResource {

    private final MovieExpert movieExpert;

    private final FoodExpert foodExpert;

    private final Vertx vertx;

    private Workflow workflow = 
        WorkflowBuilder.workflow("parallelization"). 
            start(parallel()
                .newBranch()
                    .action(call(java("getMeals", this::getMeals), ".mood")
                        .outputFilter(".meals")).endBranch()
                .newBranch()
                    .action(call(java("getMovies", this::getMovies), ".mood")
                        .outputFilter(".movies")).endBranch()
            )
            .next(operation()
                .action(call(expr("moviesAndMeals",
                    ".movies as $movies | .meals as $meals | range(0,3) | [{\"movie\": $movies[.],\"meal\": $meals[.]}]"))))
            .end() 
            .build();
            
    public EveningPlannerResource(MovieExpert movieExpert, FoodExpert foodExpert, Vertx vertx) {
        this.movieExpert = movieExpert;
        this.foodExpert = foodExpert;
        this.vertx = vertx;
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("mood/{mood}")
    public JsonNode plan(String mood) {
        try (StaticWorkflowApplication application = StaticWorkflowApplication.create()) {
            JsonNode result = application.execute(workflow, Map.of("mood", mood)).getWorkflowdata();
            System.out.println("Workflow execution result is " + result);
            return result.get("response");
        }
        // Executor scheduler = r -> vertx.getDelegate().executeBlocking(() -> {
        //     r.run();
        //     return null;
        // }, false);

        // return Uni.combine().all()
        //         .unis(Uni.createFrom().item(() -> movieExpert.findMovie(mood)).runSubscriptionOn(scheduler),
        //               Uni.createFrom().item(() -> foodExpert.findMeal(mood)).runSubscriptionOn(scheduler))
        //         .with((movies, meals) -> {
        //             return getMoviesAndMeals(movies, meals);
        //         })
        //         .await()
        //         .indefinitely();
    }

    @SuppressWarnings("unchecked")
    private Map<String,Object> getMoviesAndMeals(Map<String,Object> workflowData) {
        List<EveningPlan> moviesAndMeals = new ArrayList<>();
        List<String> movies = (List<String>) workflowData.get("movies");
        List<String> meals = (List<String>) workflowData.get("meals");
        for (int i = 0; i < 3; i++) {
            Log.infof("Movie #%d: %s", i+1, movies.get(i));
            Log.infof("Meal #%d: %s", i+1, meals.get(i));
            moviesAndMeals.add(new EveningPlan(movies.get(i), meals.get(i)));
        }
        workflowData.put("moviesAndMeals", moviesAndMeals);
        return workflowData;
    }

    public List<String> getMovies(String mood) {
        return movieExpert.findMovie(mood);
    }

    public List<String> getMeals(String mood) {
        return foodExpert.findMeal(mood);
    }

    public record EveningPlan(String movie, String meal) { }
}
