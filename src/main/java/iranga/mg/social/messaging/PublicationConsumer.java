package iranga.mg.social.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import iranga.mg.social.config.RabbitConfig;
import iranga.mg.social.dto.PublicationDTO;
import iranga.mg.social.dto.publication.CommentResponseDTO;
import iranga.mg.social.model.Comment;
import iranga.mg.social.model.Publication;
import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class PublicationConsumer {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(PublicationConsumer.class);

    private final SimpMessagingTemplate  messagingTemplate;

    @RabbitListener(queues = RabbitConfig.PUBLICATION_QUEUE)
    public void handlePublicationMessage(PublicationDTO publication) {
        logger.info("Received publication message: {}", publication);
        messagingTemplate.convertAndSend("/topic/publications", publication);
        // Add your processing logic here
    }

    @RabbitListener(queues = RabbitConfig.COMMENT_QUEUE)
    public void handleCommentMessage(CommentResponseDTO comment) {
        logger.info("Received comment message: {}", comment);
        messagingTemplate.convertAndSend("/topic/comments", comment);
        // Add your processing logic here
    }
}
