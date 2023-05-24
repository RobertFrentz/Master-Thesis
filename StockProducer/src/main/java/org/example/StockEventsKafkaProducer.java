package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class StockEventsKafkaProducer {

    public static void main(String[] args) {
        int delay = Integer.parseInt(args[0]);



        String topicName = "trade-data";
//        String csvFile = "../../usr/share/file/debs2022-gc-trading-day-08-11-21.csv";
        String csvFile = "C:/Users/Robert/Downloads/output08-copy-Copy.csv";
        String line = "";
        String csvSplitBy = ",";

        Properties props = new Properties();
//        props.put("bootstrap.servers", "kafka:9092");
        props.put("bootstrap.servers", "localhost:9094");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        //AdminClient kafkaAdminClient = KafkaAdminClient.create(props);

        try (
                Producer<String, String> producer = new KafkaProducer<>(props);
                CSVReader reader = new CSVReader(new FileReader(csvFile));
        )
        {

//            // create CSV to bean parser
//            CsvToBean<EventCSV> csvToBean = new CsvToBeanBuilder<EventCSV>(reader)
//                    .withType(EventCSV.class)
//                    .withIgnoreLeadingWhiteSpace(true)
//                    .build();
//
//            // parse CSV and convert to list of events
//            List<EventCSV> events = csvToBean.parse();

            ObjectMapper mapper = new ObjectMapper();

            try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
                boolean firstLineRead = false;
                int lineNumber = 0;
                while ((line = br.readLine()) != null) {
                    if(!firstLineRead){
                        firstLineRead = true;
                        continue;
                    }
                    lineNumber++;
                    System.out.println(lineNumber);
                    String[] data = line.split(csvSplitBy);

                    //System.out.println(Arrays.toString(data));

                    Event event = new Event(data[0], data[1], Double.parseDouble(data[2]), data[3], data[4]);

                    String key = event.getId();
                    String value = mapper.writeValueAsString(event);

                    long timestamp = EventDateTimeHelper.getDateTimeInMillis(event);

//                    if(!kafkaAdminClient.listTopics().names().get().contains(key)){
//                        int numPartitions = 1;
//                        short replicationFactor = 1;
//                        NewTopic newTopic = new NewTopic(key, numPartitions, replicationFactor);
//                        kafkaAdminClient.createTopics(Collections.singletonList(newTopic));
//                    }

                    ProducerRecord<String, String> record = new ProducerRecord<>(topicName, 0, timestamp, key, value);
                    //ProducerRecord<String, String> record = new ProducerRecord<>(topicName, key, value);
                    producer.send(record);
                    if(delay != 0){
                        Thread.sleep(delay);
                    }
                   // System.out.println();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println("Finished reading");
            //kafkaAdminClient.close();

            // publish each object to Kafka topic
//            ObjectMapper mapper = new ObjectMapper();
//            for (EventCSV event : events) {
//                String key = event.getID();
//                String value = mapper.writeValueAsString(Mapper.mapEventFrom(event));
//
//                ProducerRecord<String, String> record = new ProducerRecord<>(topicName, key, value);
//                producer.send(record);
//                if(delay != 0){
//                    Thread.sleep(delay);
//                }
//
//            }

        } catch (Exception e) {
            e.printStackTrace();
        }
//        Producer<String, String> producer = new KafkaProducer<>(props);
//        for (int i = 0; i < 10; i++) {
//            String key = "key-" + i;
//            String value = "value-" + i;
//            ProducerRecord<String, String> record = new ProducerRecord<>(topicName, key, value);
//            producer.send(record);
//        }
//        producer.close();

        /*try (
                Producer<String, String> producer = new KafkaProducer<>(props);
                BufferedReader br = new BufferedReader(new FileReader(csvFile))
        )
        {

            while ((line = br.readLine()) != null) {

                // use comma as separator
                String[] data = line.split(csvSplitBy);

                // create Kafka record and publish to topic
                ProducerRecord<String, String> record = new ProducerRecord<>(topicName, data[0], data[1]);
                producer.send(record);

            }

        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }
}
