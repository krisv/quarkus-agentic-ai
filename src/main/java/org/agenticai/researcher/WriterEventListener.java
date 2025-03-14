package org.agenticai.researcher;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import io.quarkus.logging.Log;
import io.smallrye.reactive.messaging.annotations.Blocking;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;


@ApplicationScoped
@Path("/research/event")
public class WriterEventListener {

    private final WriterService writer;

    public WriterEventListener(WriterService writer) {
        this.writer = writer;
    }

    @Channel("request-review")
    Emitter<ResearchInfo> reviewRequestEmitter; 

    @GET
    @Path("/review")
    public String createRequest() throws InterruptedException {
        Thread.sleep(1000);
        ResearchInfo info = new ResearchInfo("cars", null, null, null);
        reviewRequestEmitter.send(info); 
        return info.toString(); 
    }


    @Incoming("request-review-in")
    @Outgoing("result-review")
    @Blocking             
    public ResearchInfo process(ResearchInfo info) throws InterruptedException {
        Log.infof("Received message: %s", info);
        String review = writer.internalReviewArticle(info.getTopic(), info.getArticle());
        // simulate some hard working task
        Thread.sleep(3000);
        info.setReview(review);
        return info;
    }
}
