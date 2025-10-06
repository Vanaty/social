package iranga.mg.social.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import iranga.mg.social.config.RabbitConfig;

@Component
public class PublicationProducer {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(PublicationProducer.class);
    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendPublication(Object publication) {
        logger.info("Sending publication message: {}", publication);
        rabbitTemplate.convertAndSend(RabbitConfig.PUBLICATION_EXCHANGE, "publication.new", publication);
    }
}
