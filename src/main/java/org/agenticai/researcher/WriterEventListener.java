package org.agenticai.researcher;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.cloudevents.CloudEvent;
import io.cloudevents.CloudEventData;
import io.cloudevents.core.builder.CloudEventBuilder;
import io.cloudevents.core.data.PojoCloudEventData;
import io.quarkus.logging.Log;
import io.smallrye.reactive.messaging.annotations.Blocking;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;


@ApplicationScoped
@Path("/research/event")
public class WriterEventListener {

    private final WriterService writer;

    public WriterEventListener(WriterService writer) {
        this.writer = writer;
    }
    
    @Inject
    ObjectMapper objectMapper;
    
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
    public CloudEvent process(CloudEvent ce) throws InterruptedException, IOException {
    	ResearchInfo info = objectMapper.readValue(ce.getData().toBytes(), ResearchInfo.class);
        Log.infof("Received message: %s", info);
        String review = writer.internalReviewArticle(info.getTopic(), info.getArticle());
        // simulate some hard working task
        Thread.sleep(3000);
        info.setReview(review);
        return CloudEventBuilder.v1().withData(PojoCloudEventData.wrap(info, objectMapper::writeValueAsBytes)).withType("result-review").withSource(URI.create("http://agentic-ai-com"))
                .withId(UUID.randomUUID().toString()).withExtension("kogitoprocrefid", (String)ce.getExtension("kogitoprocinstanceid")).build();
    }
}
