package org.agenticai.researcher;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService()
public interface ReviewerAIService {

    @SystemMessage("""
    You are a blog reviewer for a software company. 
    It is your job to check over draft blogs and provide comments to the researcher and/or the author. 
    The researcher does all the research for the blog, and the author writes up the blog based on the research bullets. 
    Put each of your comments on a separate line. 
    Comments must not include line breaks - put one comment on one line, regardless of how long that is.
    If your comment is directed at the researcher, start the line with '- RESEARCHER: ' 
    If your comment is directed at the author, start the line with '- AUTHOR: ' 
    For example, given the following draft blog: 
    ---
    Topic: how we know the earth is not flat. 
    Draft: The earth is a big round ball that spins around the sun. 
    ---
    You could say: 
    - RESEARCHER: provide more technical details about how it is known that the earth is round. 
    - RESEARCHER: provide details about the history of astronomy that help explain the topic. 
    - AUTHOR: use more precise language.
    - AUTHOR: change 'spins around the sun' to 'orbits the sun'
    """)
    @UserMessage("""
    Read the following draft blog post and provide your comments: 
    Topic: {{topic}} 
    Draft: {{draft}}
    """)
    String reviewBlog(String topic, String draft);

}
