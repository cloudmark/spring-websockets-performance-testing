package org.cloudmark.samples.performance.services;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudmark.samples.performance.dto.Ping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.broker.BrokerAvailabilityEvent;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class PingController implements ApplicationListener<BrokerAvailabilityEvent> {
    private static Log logger = LogFactory.getLog(PingController.class);

    private final SimpMessagingTemplate messagingTemplate;

    private AtomicBoolean brokerAvailable = new AtomicBoolean();

    @Value("${dummyBytes}")
    private Integer fakeDummyData;

    @Autowired
    public PingController(SimpMessagingTemplate simpMessagingTemplate) {
        this.messagingTemplate = simpMessagingTemplate;
    }

    @Scheduled(fixedDelay = 1000)
    public void pingEveryOneSecond() {
        if (this.brokerAvailable.get()) {
            this.messagingTemplate.convertAndSend("/topic/ping.1", createPingMessageResponse());
        }
    }

    @Scheduled(fixedDelay = 2000)
    public void pingEveryTwoSeconds() {
        if (this.brokerAvailable.get()) {
            this.messagingTemplate.convertAndSend("/topic/ping.2", createPingMessageResponse());
        }
    }

    @Scheduled(fixedDelay = 3000)
    public void pingEveryThreeSeconds() {
        if (this.brokerAvailable.get()) {
            this.messagingTemplate.convertAndSend("/topic/ping.3", createPingMessageResponse());
        }
    }

    private Ping createPingMessageResponse(){
        return new Ping(System.currentTimeMillis(), RandomStringUtils.random(fakeDummyData));
    }

    @Override
    public void onApplicationEvent(BrokerAvailabilityEvent event) {
        this.brokerAvailable.set(event.isBrokerAvailable());
    }
}
