package com.example.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.time.Duration;
import java.util.*;

public class KafkaWebSocketHandler extends TextWebSocketHandler {
    private final KafkaConsumer<String, String> kafkaConsumer;

    private final Map<WebSocketSession, List<String>> sessions = new HashMap<>();

    public KafkaWebSocketHandler() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9094"); // Replace with your Kafka broker addresses
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "kafka-websocket-group2");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        kafkaConsumer = new KafkaConsumer<>(props);

        new Thread(() -> {
            try {
                while (true) {
                    ConsumerRecords<String, String> records;
                    synchronized (kafkaConsumer) {
                        if (kafkaConsumer.assignment().isEmpty()) {
                            continue;
                        }

                        records = kafkaConsumer.poll(Duration.ofMillis(1000));
                    }
                    for (ConsumerRecord<String, String> record : records) {
                        for (WebSocketSession session : sessions.keySet()) {
                            List<String> topics = sessions.get(session);
                            if (topics.contains(record.topic())) {
                                System.out.println("Sending topic " + record.value() + " to session " + session.getId());
                                //session.sendMessage(new TextMessage(String.format("{\"message\": \"%s\"}", record.value())));
                                session.sendMessage(new TextMessage(record.value()));
                            }
                        }
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.put(session, new ArrayList<>());
        System.out.println("Connected");
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String topic = message.getPayload().replace("\"", "");
        List<String> topics = sessions.get(session);

        System.out.println("Topic " + topic + " has been received");

        for(String tp : topics){
            System.out.println("Session " + session.getId() + " contains topic " + tp);
        }

        if(!topics.contains(topic)){
            topics.add(topic);
            sessions.put(session, topics);
            addTopic(topic);
        } else{
            System.out.println("Session " + session.getId() + " already subscribed to topic " + topic);
        }

    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
//        for(String topic : sessions.get(session))
//        {
//            removeTopic(topic);
//        }
        sessions.remove(session);
        System.out.println("Session " + session.getId() + " was closed");
    }

    private void removeTopic(String topic){
        synchronized (kafkaConsumer)
        {
            Set<TopicPartition> partitions = new HashSet<>(kafkaConsumer.assignment());
            partitions.removeIf(partition -> partition.topic().equals(topic));
            kafkaConsumer.assign(partitions);
        }
    }

    private void addTopic(String topic){
        synchronized (kafkaConsumer)
        {
            TopicPartition topicPartition = new TopicPartition(topic, 0);
            Set<TopicPartition> partitions = new HashSet<>(kafkaConsumer.assignment());
            partitions.add(topicPartition);
            kafkaConsumer.assign(partitions);
            kafkaConsumer.seekToBeginning(Collections.singletonList(topicPartition));
        }

    }

}

