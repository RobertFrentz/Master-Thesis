package job;

public class JobOptions {

    int kafkaSourceParallelism;
    int kafkaSinkParallelism;
    int windowParallelism;

    int windowTime;

    String brokerUrl;

    public JobOptions() {
    }

    @SuppressWarnings("DuplicateExpressions")
    public JobOptions(String[] args) throws Exception {
        if(args.length != 5){
            throw new Exception("The required number of parameters is 5");
        }

        for(String arg: args){
            if(arg.matches("^--parallelism-kafka-source=[1-9]$")){
                kafkaSourceParallelism = Integer.parseInt(String.valueOf(arg.charAt(arg.length() - 1)));
            }
            else if(arg.matches("^--parallelism-window=[1-9]$")){
                windowParallelism = Integer.parseInt(String.valueOf(arg.charAt(arg.length() - 1)));
            }
            else if(arg.matches("^--parallelism-kafka-sink=[1-9]$")){
                kafkaSinkParallelism = Integer.parseInt(String.valueOf(arg.charAt(arg.length() - 1)));
            }
            else if(arg.matches("^--window-time=[0-9]+$")){
                String seconds = arg.substring(arg.indexOf("=") + 1);
                windowTime = Integer.parseInt(seconds);
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
        return "JobOptions{" +
                "kafkaSourceParallelism=" + kafkaSourceParallelism +
                ", kafkaSinkParallelism=" + kafkaSinkParallelism +
                ", windowParallelism=" + windowParallelism +
                ", windowTime=" + windowTime +
                ", brokerUrl='" + brokerUrl + '\'' +
                '}';
    }
}
