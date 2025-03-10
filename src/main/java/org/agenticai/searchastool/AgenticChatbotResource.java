package org.agenticai.searchastool;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/ask")
public class AgenticChatbotResource {

    private final IntelligentAgent agent;

    public AgenticChatbotResource(IntelligentAgent agent) {
        this.agent = agent;
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("{question}")
    public String ask(String question) {
        return agent.chat(question);
    }


}
