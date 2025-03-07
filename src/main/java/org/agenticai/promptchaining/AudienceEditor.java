package org.agenticai.promptchaining;

import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService(chatMemoryProviderSupplier = RegisterAiService.NoChatMemoryProviderSupplier.class)
public interface AudienceEditor {

    @UserMessage("""
            You are a professional editor.
            Analyze and rewrite the following 1-page text to better align with the target audience of {audience}.
            Return only the 1-page text and nothing else. 
            The 1-page text is "{novel}".
            """)
    String editNovel(String novel, String audience);

}
