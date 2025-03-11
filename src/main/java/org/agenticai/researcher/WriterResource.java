package org.agenticai.researcher;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/research")
public class WriterResource {

    private final WriterService writer;

    public WriterResource(WriterService writer) {
        this.writer = writer;
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("article/topic/{topic}")
    public String hello(String topic) {
        return writer.writeArticle(topic);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("research/")
    public ResearchInfo remoteCreateResearch(ResearchInfo info) {
        String research = writer.internalCreateResearch(info.getTopic(), info.getResearch(), info.getReview());
        info.setResearch(research);
        return info;
    }
     
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("author")
    public ResearchInfo remoteAuthorArticle(ResearchInfo info) {
        String article = writer.internalAuthorArticle(info.getTopic(), info.getResearch(), info.getArticle(), info.getReview());
        info.setArticle(article);
        return info;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("review")
    public ResearchInfo remoteReviewResearch(ResearchInfo info) {
        String review = writer.internalReviewArticle(info.getTopic(), info.getArticle());
        info.setReview(review);
        return info;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("format")
    public ResearchInfo remoteFormatArticle(ResearchInfo info) {
        String article = writer.internalFormatArticle(info.getArticle());
        info.setArticle(article);
        return info;
    }

}
