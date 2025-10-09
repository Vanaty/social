package iranga.mg.social.messaging;

import java.util.Map;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import iranga.mg.social.config.RabbitConfig;
import iranga.mg.social.dto.publication.CommentResponseDTO;
import iranga.mg.social.model.Comment;

@Component
public class PublicationProducer {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(PublicationProducer.class);
    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendPublication(Object publication) {
        logger.info("Sending publication message: {}", publication);
        rabbitTemplate.convertAndSend(RabbitConfig.PUBLICATION_EXCHANGE, "publication.new", publication);
    }

    public void sendComment(Comment comment) {
        logger.info("Sending comment message: {}", comment);
        CommentResponseDTO commentDto = new CommentResponseDTO();
        commentDto.setId(comment.getId());
        commentDto.setContent(comment.getContent());
        commentDto.setCreatedAt(comment.getCreatedAt());
        commentDto.setAuthor(comment.getAuthor());
        commentDto.setPublication(Map.of("id", comment.getPublication().getId(), "title", comment.getPublication().getTitle()));
        rabbitTemplate.convertAndSend(RabbitConfig.COMMENT_EXCHANGE, "comment.new", commentDto);
    }
}
