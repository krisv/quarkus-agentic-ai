package org.agenticai.researcher;

import com.fasterxml.jackson.databind.JsonNode;

import io.quarkus.logging.Log;
import io.serverlessworkflow.api.Workflow;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.Map;

import org.kie.kogito.serverless.workflow.executor.StaticWorkflowApplication;
import org.kie.kogito.serverless.workflow.fluent.OperationStateBuilder;
import org.kie.kogito.serverless.workflow.fluent.WorkflowBuilder;

import static org.kie.kogito.serverless.workflow.fluent.ActionBuilder.call;
import static org.kie.kogito.serverless.workflow.fluent.FunctionBuilder.expr;
import static org.kie.kogito.serverless.workflow.fluent.FunctionBuilder.java;
import static org.kie.kogito.serverless.workflow.fluent.StateBuilder.operation;

@Path("/research")
public class WriterResource {

    private final ResearcherAIService researcher;

    private final AuthorAIService author;

    private final ReviewerAIService reviewer;

    private final FormatterAIService formatter;

    private Workflow workflow;

    public WriterResource(ResearcherAIService researcher, AuthorAIService author, ReviewerAIService reviewer, FormatterAIService formatter) {
        this.researcher = researcher;
        this.author = author;
        this.reviewer = reviewer;
        this.formatter = formatter;
        OperationStateBuilder createResearch = operation()
            .action(call(java("createResearch", this::createResearch), ".topic")
                .outputFilter(".research"));
        OperationStateBuilder authorArticle = operation()
            .action(call(java("authorArticle", this::authorArticle), ".topic", ".research")
                .outputFilter(".article"));
        OperationStateBuilder reviewResearch = operation()
            .action(call(java("reviewResearch", this::countAndReviewResearch)));
        OperationStateBuilder formatResearch = operation()
            .action(call(java("formatResearch", this::formatResearch), ".article")
                .outputFilter(".article"));
        workflow = 
            WorkflowBuilder.workflow("research"). 
                start(createResearch)
                .next(authorArticle)
                .next(reviewResearch)
                .when(".counter<=3")
                    .when(".review | startswith(\"RESEARCHER\")").next(createResearch).end()
                    .or().when(".review | startswith(\"AUTHOR\")").next(authorArticle).end()
                    .or().next(formatResearch)
                .end().or().next(formatResearch)
                .end() 
                .build();
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("article/topic/{topic}")
    public String hello(String topic) {
        try (StaticWorkflowApplication application = StaticWorkflowApplication.create()) {
            JsonNode result = application.execute(workflow, Map.of("topic", topic, "counter", 0)).getWorkflowdata();
            System.out.println("Workflow execution result is " + result);
            return result.get("article").asText();
        }
    }

    private int counter1 = 0;

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Path("research/topic/{topic}")
    public String createResearch(String topic) {
        String research;
        switch (counter1++) {
            case 0:
                research = "My research about " + topic;
                break;
            case 1:
                research = "More research about " + topic;
                break;
            case 2:
                research = "Even more research about " + topic;
                break;
            default:
                throw new IllegalArgumentException();
        }
        // creativeWriter.generateNovel(topic);
        Log.infof("Research: %s", research);
        return research;
    }
    
    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    @Path("author/topic/{topic}")
    public String authorArticle(String topic, String research) {
        String article = "My article based on research " + research; // creativeWriter.generateNovel(topic);
        Log.infof("Article: %s", article);
        return article;
    }
    
    public Map<String,Object> countAndReviewResearch(Map<String,Object> workflowData) {
        String article = (String) workflowData.get("article");
        Integer counter = (Integer) workflowData.get("counter");
        String review = reviewResearch(article);
        workflowData.put("review", review);
        workflowData.put("counter", counter+1);
        return workflowData;
    }

    private int counter2 = 0;
    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    @Path("review")
    public String reviewResearch(String article) {
        String review;
        switch (counter2++) {
            case 0:
                review = "RESEARCHER: Add more detail";
                break;
            case 1:
                review = "RESEARCHER: Add even more detail";
                break;
            case 2:
                review = "AUTHOR: use more precise language";
                break;
            case 3:
                review = "AUTHOR: asking too much, right?";
                break;
            default:
                throw new IllegalArgumentException();
        }
        // styleEditor.editNovel(novel, style);
        Log.infof("Review: %s", review);
        return review;
    }

    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    @Path("format")
    public String formatResearch(String article) {
        article = article + " formatted"; // audienceEditor.editNovel(novel, audience);
        Log.infof("Formatted article: %s", article);
        return article;
    }
}
