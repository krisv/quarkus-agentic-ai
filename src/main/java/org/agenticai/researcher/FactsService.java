package org.agenticai.researcher;

import jakarta.enterprise.context.ApplicationScoped;
import dev.langchain4j.agent.tool.Tool;

@ApplicationScoped
public class FactsService {
    private final FactsAIService service;

    public FactsService(FactsAIService service) {
        this.service = service;
    }

    @Tool("search the web")
    public String getFacts(String topic) {   
        System.err.println("---------- FACT SERVICE TOOL [" + topic + "] ----------");
        String facts = service.getFacts(topic);
        System.out.println("facts: " + facts);
        return facts;
    }
}