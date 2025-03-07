package org.agenticai.promptchaining;

import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService(chatMemoryProviderSupplier = RegisterAiService.NoChatMemoryProviderSupplier.class)
public interface CreativeWriter {

    @UserMessage("""
            You are a creative writer.
            Generate a short draft of a 1-page text around the given topic. 
            Return only the 1-page text and nothing else. 
            The topic is {topic}.
            """)
    String generateNovel(String topic);

}
