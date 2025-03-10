package org.agenticai.researcher;

public class ResearchInfo {

    private String topic;
    private String research;
    private String article;
    private String review;

    public ResearchInfo(String topic, String research, String article, String review) {
        this.topic = topic;
        this.research = research;
        this.article = article;
        this.review = review;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getResearch() {
        return research;
    }

    public void setResearch(String research) {
        this.research = research;
    }

    public String getArticle() {
        return article;
    }

    public void setArticle(String article) {
        this.article = article;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

}
