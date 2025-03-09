package org.agenticai.promptchaining;

import com.fasterxml.jackson.databind.JsonNode;

import io.quarkus.logging.Log;
import io.serverlessworkflow.api.Workflow;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.Map;

import org.kie.kogito.serverless.workflow.executor.StaticWorkflowApplication;
import org.kie.kogito.serverless.workflow.fluent.WorkflowBuilder;

import static org.kie.kogito.serverless.workflow.fluent.ActionBuilder.call;
import static org.kie.kogito.serverless.workflow.fluent.FunctionBuilder.java;
import static org.kie.kogito.serverless.workflow.fluent.StateBuilder.operation;

@Path("/write")
public class WriterResource {

    private final CreativeWriter creativeWriter;

    private final StyleEditor styleEditor;

    private final AudienceEditor audienceEditor;

    private Workflow workflow = 
        WorkflowBuilder.workflow("promptchaining"). 
            start(operation()
                .action(call(java("generateNovel", this::generateNovel), ".topic")
                    .outputFilter(".novel")))
            .next(operation()
                .action(call(java("editNovelStyle", this::editNovelStyle), ".novel", ".style")
                    .outputFilter(".novel")))
            .next(operation()
                .action(call(java("editNovelAudience", this::editNovelAudience), ".novel", ".audience")
                    .outputFilter(".novel")))
            .end() 
            .build();

    // Slightly different version where data input and output mapping is done in the operation itself.
    // This simplifies the workflow but requires the operation to use a Map as input and output and 
    // as a result will require some extra data mapping in the operation itself.

    private Workflow workflow2 = 
        WorkflowBuilder.workflow("promptchaining"). 
            start(operation()
                .action(call(java("generateNovel", this::generateNovel2))))
            .next(operation()
                .action(call(java("editNovelStyle", this::editNovelStyle2))))
            .next(operation()
                .action(call(java("editNovelAudience", this::editNovelAudience2))))
            .end() 
            .build();

    public WriterResource(CreativeWriter creativeWriter, StyleEditor styleEditor, AudienceEditor audienceEditor) {
        this.creativeWriter = creativeWriter;
        this.styleEditor = styleEditor;
        this.audienceEditor = audienceEditor;
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("topic/{topic}/style/{style}/audience/{audience}")
    public String hello(String topic, String style, String audience) {
        try (StaticWorkflowApplication application = StaticWorkflowApplication.create()) {
            JsonNode result = application.execute(workflow, Map.of("topic", topic, "style", style, "audience", audience)).getWorkflowdata();
            System.out.println("Workflow execution result is " + result);
            return result.get("novel").asText();
        }
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("topic2/{topic}/style/{style}/audience/{audience}")
    public String hello2(String topic, String style, String audience) {
        try (StaticWorkflowApplication application = StaticWorkflowApplication.create()) {
            JsonNode result = application.execute(workflow2, Map.of("topic", topic, "style", style, "audience", audience)).getWorkflowdata();
            System.out.println("Workflow execution result is " + result);
            return result.get("novel").asText();
        }
    }

    public String generateNovel(String topic) {
        String novel = "My very short novel about " + topic; // creativeWriter.generateNovel(topic);
        Log.infof("First 1-page text novel: %s", novel);
        return novel;
    }
    
    public String editNovelStyle(String novel, String style) {
        novel = novel + " in style " + style; // styleEditor.editNovel(novel, style);
        Log.infof("1-page text in style %s: %s", style, novel);
        return novel;
    }

    public String editNovelAudience(String novel, String audience) {
        novel = novel + " for audience " + audience; // audienceEditor.editNovel(novel, audience);
        Log.infof("Final 1-page text: %s", novel);
        return novel;
    }

    public Map<String,Object> generateNovel2(Map<String,Object> workflowData) {
        String topic = (String) workflowData.get("topic");
        String novel = "My very short novel about " + topic; // creativeWriter.generateNovel(topic);
        Log.infof("First 1-page text novel: %s", novel);
        workflowData.put("novel", novel);
        return workflowData;
    }
    
    public Map<String,Object> editNovelStyle2(Map<String,Object> workflowData) {
        String novel = (String) workflowData.get("novel");
        String style = (String) workflowData.get("style");
        novel = novel + " in style " + style; // styleEditor.editNovel(novel, style);
        Log.infof("1-page text in style %s: %s", style, novel);
        workflowData.put("novel", novel);
        return workflowData;
    }

    public Map<String,Object> editNovelAudience2(Map<String,Object> workflowData) {
        String novel = (String) workflowData.get("novel");
        String audience = (String) workflowData.get("audience");
        novel = novel + " for audience " + audience; // audienceEditor.editNovel(novel, audience);
        Log.infof("Final 1-page text: %s", novel);
        workflowData.put("novel", novel);
        return workflowData;
    }
}
