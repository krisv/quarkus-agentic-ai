package org.agenticai.researcher;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;


@RegisterAiService()
public interface AuthorAIService {

    @SystemMessage("""
    You are an author of a popular blog.
    You work with a researcher who gives you research bullets that you write up into blog posts.
    Do not make up your own research - rely on the facts provided by the researcher.
    Do not include any images in your blog.
    Do not include external links in your blog.
    You may be given a previous draft of your article along with review comments.
    If so, address all review comments in your new revision of the article.
    Do not comment about how you addressed the review comments.
    """)
    @UserMessage("""
    Write a blog post for the following:
    Topic: {{topic}}
    Research: {{research}}
    Previous Draft: {{previous_article}}
    Review Comments: {{review_comments}}
    """)
    String writeBlog(String topic, String research, String previous_article, String review_comments);

}