import java.util.Map;

public class Launcher {

    public static void main(String[] args) {


        try {

            if (args.length != 1) {
                System.out.println("Demos:");
                System.out.println("\tbenchmark");
                System.out.println("\tmonitor");
                System.out.println("\tcepmonitor");
                System.out.println("\tcepbenchmarkmonitor");
                System.out.println("\n");
                System.out.println("Enter the type of demo:");
                System.out.println("\t java -jar monitordemos.jar [name of demo]");

            } else {


                switch (args[0].toLowerCase()) {
                    case "benchmark":
                        System.out.println("Benchmark Demo:");
                        benchmarkDemo();
                        break;
                    case "monitor":
                        System.out.println("Benchmark Monitor Demo:");
                        monitorDemo();
                        break;
                    case "cepmonitor":
                        System.out.println("CEP Monitor Demo:");
                        cepMonitorDemo();
                        break;
                    case "cepbenchmarkmonitor":
                        System.out.println("CEP Benchmark Monitor Demo:");
                        cepBenchmarkMonitorDemo();
                        break;
                    default:
                        System.out.println("no demo selected");

                }

            }

        } catch(Exception ex){
            ex.printStackTrace();
        }
    }

    public static void benchmarkDemo() {

        try {

            Benchmark benchmark = new Benchmark();

            while (true) {
                benchmark.bench();
                Thread.sleep(5000);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public static void monitorDemo() {

        try {


            SysInfoBuilder sysInfoBuilder = new SysInfoBuilder();
            Benchmark benchmark = new Benchmark();

            int benchmarkPid = sysInfoBuilder.getBenchmarkPid();
            System.out.println(benchmarkPid);
            if(benchmarkPid != -1) {
                Thread thread = new Thread(){
                    public void run(){
                        System.out.println("Start Monitor Thread");
                        sysInfoBuilder.printBenchmarkMonitor(benchmarkPid,"Benchmark");
                    }
                };
                thread.start();

                while (true) {
                    Thread.sleep(15000);
                    benchmark.bench();
                    Thread.sleep(15000);
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public static void cepMonitorDemo() {

        try {

            CEPEngine cepEngine = new CEPEngine();

            String inputStreamName = "SensorStream";
            String inputStreamAttributesString = "sensor_type string, sensor_id string, timestamp long, transactiontime long, value double, metric string, payload string";

            String outputStreamName = "MeasureStream";
            String outputStreamAttributesString = "sensor_type string, sensor_id string, timestamp long, transactiontime long, value double, metric string, payload string";

            /*
            String queryString = " " +
                    "from SensorStream#window.timeBatch(15 sec) " +
                    "select sensor_id, avg(value) as avg_value " +
                    "  group by sensor_id " +
                    "insert into AVGStream; ";
            */
            String queryString = " " +
                    "from SensorStream " +
                    "select * " +
                    "insert into MeasureStream; ";


            cepEngine.createCEP(inputStreamName, outputStreamName, inputStreamAttributesString, outputStreamAttributesString, queryString);

            SysInfoBuilder sysInfoBuilder = new SysInfoBuilder();

            int benchmarkPid = sysInfoBuilder.getBenchmarkPid();
            System.out.println(benchmarkPid);
            if(benchmarkPid != -1) {
                Thread thread = new Thread(){
                    public void run(){
                        System.out.println("Start Monitor Thread");
                        sysInfoBuilder.printBenchmarkMonitor(benchmarkPid,"CEP");
                    }
                };
                thread.start();

                while (true) {
                    String inputEvent = cepEngine.getStringPayloadSensor();
                    //System.out.println("INPUT EVENT: " + inputEvent);
                    cepEngine.input(inputStreamName, inputEvent);
                    Thread.sleep(1000);
                }

            }


        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public static void cepBenchmarkMonitorDemo() {

        try {
            Benchmark benchmark = new Benchmark();
            BenchMetric bm = benchmark.bench();

            Thread.sleep(3000);

            CEPEngine cepEngine = new CEPEngine();

            String inputStreamName = "SensorStream";
            String inputStreamAttributesString = "sensor_type string, sensor_id string, timestamp long, transactiontime long, value double, metric string, payload string";

            String outputStreamName = "MeasureStream";
            String outputStreamAttributesString = "sensor_type string, sensor_id string, timestamp long, transactiontime long, value double, metric string, payload string";

            /*
            String queryString = " " +
                    "from SensorStream#window.timeBatch(15 sec) " +
                    "select sensor_id, avg(value) as avg_value " +
                    "  group by sensor_id " +
                    "insert into AVGStream; ";
            */
            String queryString = " " +
                    "from SensorStream " +
                    "select * " +
                    "insert into MeasureStream; ";


            cepEngine.createCEP(inputStreamName, outputStreamName, inputStreamAttributesString, outputStreamAttributesString, queryString);

            SysInfoBuilder sysInfoBuilder = new SysInfoBuilder();

            int benchmarkPid = sysInfoBuilder.getBenchmarkPid();
            System.out.println(benchmarkPid);
            if(benchmarkPid != -1) {
                Thread thread = new Thread(){
                    public void run(){
                        System.out.println("Start Monitor Thread");
                        String monitorType = "CEP";
                        sysInfoBuilder.printBenchmarkMonitor(benchmarkPid,monitorType,bm.getCPU());

                    }
                };
                thread.start();

                while (true) {
                    String inputEvent = cepEngine.getStringPayloadSensor();
                    //System.out.println("INPUT EVENT: " + inputEvent);
                    cepEngine.input(inputStreamName, inputEvent);
                    //Thread.sleep(1000);
                }

            }


        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }



}