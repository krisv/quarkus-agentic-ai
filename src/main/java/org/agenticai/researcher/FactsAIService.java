package org.agenticai.researcher;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService()
public interface FactsAIService {

    @SystemMessage("""
    You are a fact service. 
    Your job is to provide a list of facts about a topic. 
    Provide each fact on its own line, starting with a hyphen, with no additional formatting.
    """)
    @UserMessage("Please provide facts about the following topic: {{topic}}")
    String getFacts(String topic);

}