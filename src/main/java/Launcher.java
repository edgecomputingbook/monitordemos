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
            //System.out.println(sysInfoBuilder.getSysInfoMap());
            for(Map<String,String> cpu : sysInfoBuilder.getCPUInfo())
                System.out.println(cpu);


        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }


}