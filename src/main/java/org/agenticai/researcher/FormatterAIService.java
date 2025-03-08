package org.agenticai.researcher;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService()
public interface FormatterAIService {
    @SystemMessage("""
    Your job is to format text in the format requested by the user.
    Note that the text may already be formatted a different way (eg. in markdown) -- your job is to convert it to the format the user asks for.
    Put a lot of effort into good layout.
    Only provide the raw output - do not wrap it or triple-quote it.
    """)
    @UserMessage("""
    Please format the following:
    Text: {{text}}
    Format: {{format}}
    """)
    String format(String text, String format);
    
}