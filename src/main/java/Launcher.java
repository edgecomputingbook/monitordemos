import java.util.Map;

public class Launcher {

    public static void main(String[] args) {


        try {

            if (args.length != 1) {
                System.out.println("Demos:");
                System.out.println("\tbenchmark");
                System.out.println("\tmonitor");
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
                        System.out.println("Monitor Demo:");
                        monitorDemo();
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
                        sysInfoBuilder.printBenchmarkMonitor(benchmarkPid);
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


}