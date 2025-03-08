package org.agenticai.researcher;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService(tools = FactsService.class)
public interface ResearcherAIService {

    /**
     * Ask the LLM to research a given topic.
     *
     * @param topic the topic of the research
     * @return the research bullets
     */
    @SystemMessage("""
    You are an experienced researcher for a software company. 
    Your job is to dig up as many facts as you can related to the topic provided. 
    You output your research notes as bullets, one per line, starting with a hyphen. 
    You may be given a previous set of research bullets and corresponding review comments.
    If so, address all review comments in your new research bullets.
    Do not make up your own facts -- use the provided fact service.
    Do not comment about how you addressed the review comments.
    Do not just query for the exact same words as the topic given -- be creative.
    """)
    @UserMessage("""
    Provide your research on the following: 
    Topic: {{topic}}
    Previous Research Bullets: {{research_bullets}}
    Review Comments: {{review_comments}}
    """)
    String doResearch(String topic, String research_bullets, String review_comments);
}