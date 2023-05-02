package topology;

public class TopologyOptions {
    String brokerUrl;
    int numOfWorkers;
    int numOfTasksForKafkaSpout;
    int numOfTasksForWindowBolt;
    int numOfTasksForKafkaBolt;
    int numOfExecutorsForKafkaSpout;
    int numOfExecutorsForWindowBolt;
    int numOfExecutorsForKafkaBolt;
    String numOfAcks;
    int windowDuration;


    @SuppressWarnings("DuplicateExpressions")
    public TopologyOptions(String[] args) throws Exception {
        if(args.length != 10){
            throw new Exception("The required number of parameters is 10");
        }

        for(String arg: args){
            if(arg.matches("^--workers=[1-9]$")){
                numOfWorkers = Integer.parseInt(String.valueOf(arg.charAt(arg.length() - 1)));
            }
            else if(arg.matches("^--tasks-kafka-spout=[1-9]$")){
                numOfTasksForKafkaSpout = Integer.parseInt(String.valueOf(arg.charAt(arg.length() - 1)));
            }
            else if(arg.matches("^--tasks-window-bolt=[1-9]$")){
                numOfTasksForWindowBolt = Integer.parseInt(String.valueOf(arg.charAt(arg.length() - 1)));
            }
            else if(arg.matches("^--tasks-kafka-bolt=[1-9]$")){
                numOfTasksForKafkaBolt = Integer.parseInt(String.valueOf(arg.charAt(arg.length() - 1)));
            }
            else if(arg.matches("^--executors-kafka-spout=[1-9]$")){
                numOfExecutorsForKafkaSpout = Integer.parseInt(String.valueOf(arg.charAt(arg.length() - 1)));
            }
            else if(arg.matches("^--executors-window-bolt=[1-9]$")){
                numOfExecutorsForWindowBolt = Integer.parseInt(String.valueOf(arg.charAt(arg.length() - 1)));
            }
            else if(arg.matches("^--executors-kafka-bolt=[1-9]$")){
                numOfExecutorsForKafkaBolt = Integer.parseInt(String.valueOf(arg.charAt(arg.length() - 1)));
            }
            else if(arg.matches("^--acks=[1-9]$")){
                numOfAcks = String.valueOf(arg.charAt(arg.length() - 1));
            }
            else if(arg.matches("^--window-duration-seconds=[0-9]+$")){
                String seconds = arg.substring(arg.indexOf("=") + 1);
                windowDuration = Integer.parseInt(seconds);
            }
            else if(arg.matches("^--brokerUrl=[a-zA-Z]+:[0-9]+$")){
                brokerUrl = arg.substring(arg.indexOf("=") + 1);
            }
            else {
                throw new Exception("The following parameter is not valid: " + arg);
            }
        }

    }

    @Override
    public String toString() {
        return "TopologyOptions{" +
                "brokerUrl='" + brokerUrl + '\'' +
                ", numOfWorkers=" + numOfWorkers +
                ", numOfTasksForKafkaSpout=" + numOfTasksForKafkaSpout +
                ", numOfTasksForWindowBolt=" + numOfTasksForWindowBolt +
                ", numOfTasksForKafkaBolt=" + numOfTasksForKafkaBolt +
                ", numOfExecutorsForKafkaSpout=" + numOfExecutorsForKafkaSpout +
                ", numOfExecutorsForWindowBolt=" + numOfExecutorsForWindowBolt +
                ", numOfExecutorsForKafkaBolt=" + numOfExecutorsForKafkaBolt +
                ", numOfAcks='" + numOfAcks + '\'' +
                ", windowDuration=" + windowDuration +
                '}';
    }
}
