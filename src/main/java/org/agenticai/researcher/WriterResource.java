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
            .action(call(java("authorArticle", this::authorArticle)));
        OperationStateBuilder reviewResearch = operation()
            .action(call(java("reviewResearch", this::reviewResearch)));
        OperationStateBuilder formatResearch = operation()
            .action(call(java("formatResearch", this::formatResearch)));
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
    @Path("topic/{topic}")
    public String hello(String topic) {
        try (StaticWorkflowApplication application = StaticWorkflowApplication.create()) {
            JsonNode result = application.execute(workflow, Map.of("topic", topic, "counter", 0)).getWorkflowdata();
            System.out.println("Workflow execution result is " + result);
            return result.get("article").asText();
        }
    }

    private int counter1 = 0;

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
    
    public Map<String,Object> authorArticle(Map<String,Object> workflowData) {
        String topic = (String) workflowData.get("topic");
        String research = (String) workflowData.get("research");
        String article = "My article based on research " + research; // creativeWriter.generateNovel(topic);
        Log.infof("Article: %s", article);
        workflowData.put("article", article);
        return workflowData;
    }
    
    private int counter2 = 0;
    public Map<String,Object> reviewResearch(Map<String,Object> workflowData) {
        String article = (String) workflowData.get("article");
        Integer counter = (Integer) workflowData.get("counter");
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
        workflowData.put("review", review);
        workflowData.put("counter", counter+1);
        return workflowData;
    }

    public Map<String,Object> formatResearch(Map<String,Object> workflowData) {
        String article = (String) workflowData.get("article");
        article = article + " formatted"; // audienceEditor.editNovel(novel, audience);
        Log.infof("Formatted article: %s", article);
        workflowData.put("article", article);
        return workflowData;
    }
}
