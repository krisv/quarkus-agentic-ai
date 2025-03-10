package org.agenticai.routing;

import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.ApplicationScoped;

@RegisterAiService
public interface CategoryRouter {

    @UserMessage("""
            Analyze the following user request and categorize it as 'legal', 'medical', 'technical'
            or 'unknown' in case the request doesn't belong to any of those categories.
            Reply with only one of those words and nothing else.
            The user request is {request}.
            """)
    RequestCategory classify(String request);

}
