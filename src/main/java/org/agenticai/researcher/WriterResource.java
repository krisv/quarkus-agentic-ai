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
import java.util.Random;

import org.kie.kogito.serverless.workflow.executor.StaticWorkflowApplication;
import org.kie.kogito.serverless.workflow.fluent.OperationStateBuilder;
import org.kie.kogito.serverless.workflow.fluent.WorkflowBuilder;

import static org.kie.kogito.serverless.workflow.fluent.ActionBuilder.call;
import static org.kie.kogito.serverless.workflow.fluent.FunctionBuilder.expr;
import static org.kie.kogito.serverless.workflow.fluent.FunctionBuilder.java;
import static org.kie.kogito.serverless.workflow.fluent.StateBuilder.operation;

@Path("/research")
public class WriterResource {

    private boolean useLLM = false;

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
            .action(call(java("createResearch", this::createResearch)));
        OperationStateBuilder authorArticle = operation()
            .action(call(java("authorArticle", this::authorArticle)));
        OperationStateBuilder reviewResearch = operation()
            .action(call(java("reviewResearch", this::reviewArticle)));
        OperationStateBuilder formatResearch = operation()
            .action(call(java("formatResearch", this::formatArticle)));
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
            JsonNode result = application.execute(workflow, Map.of("topic", topic)).getWorkflowdata();
            System.out.println("Workflow execution result is " + result);
            return result.get("article").asText();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("research/")
    public ResearchInfo remoteCreateResearch(ResearchInfo info) {
        String research = internalCreateResearch(info.getTopic(), info.getResearch(), info.getReview());
        info.setResearch(research);
        return info;
    }

    public Map<String,Object> createResearch(Map<String,Object> workflowData) {
        String topic = (String) workflowData.get("topic");
        String review = (String) workflowData.get("review");
        String prev_research = (String) workflowData.get("research");
        String research = internalCreateResearch(topic, prev_research, review);
        workflowData.put("research", research);
        return workflowData;
    }

    public String internalCreateResearch(String topic, String prev_research, String review) {
        String research;
        if (useLLM) {
            research = researcher.doResearch(topic, prev_research, review);
        } else {
            if (prev_research == null || prev_research.trim().length() == 0) {
                research = "My research about " + topic;
            } else {
                research = "More research based on review '" + review + "' and previous research '" + prev_research + "'";
            }
        }
        Log.infof("Research: %s", research);
        return research;
    }
     
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("author")
    public ResearchInfo remoteAuthorArticle(ResearchInfo info) {
        String article = internalAuthorArticle(info.getTopic(), info.getResearch(), info.getArticle(), info.getReview());
        info.setArticle(article);
        return info;
    }

    public Map<String,Object> authorArticle(Map<String,Object> workflowData) {
        String topic = (String) workflowData.get("topic");
        String review = (String) workflowData.get("review");
        String research = (String) workflowData.get("research");
        String prev_article = (String) workflowData.get("article");
        String article = internalAuthorArticle(topic, research, prev_article, review);
        workflowData.put("article", article);
        return workflowData;
    }

    public String internalAuthorArticle(String topic, String research, String prev_article, String review) {
        String article;
        if (useLLM) {
            article = author.writeBlog(topic, research, prev_article, review);
        } else {
            article = "My article based on research " + research;
        }
        Log.infof("Article: %s", article);
        return article;
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("review")
    public ResearchInfo remoteReviewResearch(ResearchInfo info) {
        String review = internalReviewArticle(info.getTopic(), info.getArticle());
        info.setReview(review);
        return info;
    }

    String[] reviews = { "RESEARCHER: Add more detail", "AUTHOR: use more precise language"};
    Random random = new Random();
    public Map<String,Object> reviewArticle(Map<String,Object> workflowData) {
        String topic = (String) workflowData.get("topic");
        String article = (String) workflowData.get("article");
        String review = internalReviewArticle(topic, article);
        Integer counter = (Integer) workflowData.get("counter");
        workflowData.put("review", review);
        workflowData.put("counter", counter == null ? 1 : counter+1);
        return workflowData;
    }

    public String internalReviewArticle(String topic, String article) {
        String review;
        if (useLLM) {
            review = reviewer.reviewBlog(topic, article);
        } else {
            review = reviews[random.nextInt(2)];
        }
        Log.infof("Review: %s", review);
        return review;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("format")
    public ResearchInfo remoteFormatArticle(ResearchInfo info) {
        String article = internalFormatArticle(info.getArticle());
        info.setArticle(article);
        return info;
    }

    public Map<String,Object> formatArticle(Map<String,Object> workflowData) {
        String prev_article = (String) workflowData.get("article");
        String article = internalFormatArticle(prev_article);
        workflowData.put("article", article);
        return workflowData;
    }

    public String internalFormatArticle(String prev_article) {
        String article;
        if (useLLM) {
            article = formatter.format(prev_article, "Format in HTML, with a cool banner at the top introducing the blog. Pay attention to spacing. Replace asterisks with HTML formatting as appropriate.");
        } else {
            article = prev_article + " formatted";
        }
        Log.infof("Formatted article: %s", article);
        return article;
    }
}
