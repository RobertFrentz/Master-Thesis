package org.example;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

public class StockEventsKafkaProducer {
    public static void main(String[] args) {
        String topicName = "trade-data";
        String csvFile = "../../usr/share/file/debs2022-gc-trading-day-08-11-21.csv";
        String line = "";
        String csvSplitBy = ",";

//        CSVReader reader = null;
//        try {
//            reader = new CSVReader(new FileReader(csvFile));
//            CsvToBean<EventCSV> csvToBean = new CsvToBeanBuilder<EventCSV>(reader)
//                    .withType(EventCSV.class)
//                    .withIgnoreLeadingWhiteSpace(true)
//                    .build();
//
//            // parse CSV and convert to list of events
//            List<EventCSV> events = csvToBean.parse();
//            for(EventCSV event: events)
//            {
//                System.out.println(event);
//            }
//        } catch (FileNotFoundException ex) {
//            throw new RuntimeException(ex);
//        }


        Properties props = new Properties();
        props.put("bootstrap.servers", "kafka:9092");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        try (
                Producer<String, String> producer = new KafkaProducer<>(props);
                CSVReader reader = new CSVReader(new FileReader(csvFile))
        )
        {

            // create CSV to bean parser
            CsvToBean<EventCSV> csvToBean = new CsvToBeanBuilder<EventCSV>(reader)
                    .withType(EventCSV.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();

            // parse CSV and convert to list of events
            List<EventCSV> events = csvToBean.parse();

            // publish each object to Kafka topic
            ObjectMapper mapper = new ObjectMapper();
            for (EventCSV event : events) {
                String key = event.getID();
                String value = mapper.writeValueAsString(Mapper.mapEventFrom(event));

                ProducerRecord<String, String> record = new ProducerRecord<>(topicName, key, value);
                producer.send(record);
                Thread.sleep(3000);
            }

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
