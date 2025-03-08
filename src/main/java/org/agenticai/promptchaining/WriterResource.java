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
                .action(call(java("editNovelStyle", this::editNovelStyle))))
            .next(operation()
                .action(call(java("editNovelAudience", this::editNovelAudience))))
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

    public String generateNovel(String topic) {
        String novel = "My very short novel about " + topic; // creativeWriter.generateNovel(topic);
        Log.infof("First 1-page text novel: %s", novel);
        return novel;
    }
    
    public Map<String,Object> editNovelStyle(Map<String,Object> workflowData) {
        String novel = (String) workflowData.get("novel");
        String style = (String) workflowData.get("style");
        novel = novel + " in style " + style; // styleEditor.editNovel(novel, style);
        Log.infof("1-page text in style %s: %s", style, novel);
        workflowData.put("novel", novel);
        return workflowData;
    }

    public String editNovelStyle2(String novel, String style) {
        novel = novel + " in style " + style; // styleEditor.editNovel(novel, style);
        Log.infof("1-page text in style %s: %s", style, novel);
        return novel;
    }

    public Map<String,Object> editNovelAudience(Map<String,Object> workflowData) {
        String novel = (String) workflowData.get("novel");
        String audience = (String) workflowData.get("audience");
        novel = novel + " for audience " + audience; // audienceEditor.editNovel(novel, audience);
        Log.infof("Final 1-page text: %s", novel);
        workflowData.put("novel", novel);
        return workflowData;
    }
}
