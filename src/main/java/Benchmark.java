import com.google.gson.Gson;
import jnt.scimark2.Random;
import jnt.scimark2.kernel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Benchmark {

    private Gson gson;

    public Benchmark() {

        gson = new Gson();
    }


    public String getJSON() {
        String returnString = null;

        try {

            BenchMetric bm = bench();
            List<Map<String,String>> list = new ArrayList<>();

            Map<String,String> cinfo = new HashMap<>();
            cinfo.put("plugin_id", bm.getINodeId());
            cinfo.put("runtime", String.valueOf(bm.getRunTime()));
            cinfo.put("cpu", String.valueOf(bm.getCPU()));
            cinfo.put("cpumc", String.valueOf(bm.getCpuMC()));
            cinfo.put("cpufft", String.valueOf(bm.getCpuFFT()));
            cinfo.put("cpulu", String.valueOf(bm.getCpuLU()));
            cinfo.put("cpusm", String.valueOf(bm.getCpuSM()));
            cinfo.put("cpusor", String.valueOf(bm.getCpuSOR()));
            list.add(cinfo);


            Map<String, List<Map<String,String>>> info = new HashMap<>();
            info.put("benchmark",list);

            returnString = gson.toJson(info);



        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return returnString;
    }

    public BenchMetric bench() {


        BenchMetric bm = null;

        System.out.println();
        System.out.println("SciMark 2.0a");
        System.out.println("Benchmarking: Please Wait....");

        try {
            long startTime = System.currentTimeMillis();
            double var1 = 2.0D;
            int var3 = 1024;
            short var4 = 100;
            int var5 = 1000;
            int var6 = 5000;
            short var7 = 100;

            double[] var10 = new double[6];
            Random var9 = new Random(101010);
            var10[1] = kernel.measureFFT(var3, var1, var9);
            var10[2] = kernel.measureSOR(var4, var1, var9);
            var10[3] = kernel.measureMonteCarlo(var1, var9);
            var10[4] = kernel.measureSparseMatmult(var5, var6, var1, var9);
            var10[5] = kernel.measureLU(var7, var1, var9);
            var10[0] = (var10[1] + var10[2] + var10[3] + var10[4] + var10[5]) / 5.0D;


            //public BenchMetric(String INodeId, long runTime, double cpuComposite, double cpuFFT, double cpuSOR, double cpuMC, double cpuSM, double cpuLI, String javaVendor, String javaVersion, String osArch, String osName, String osVersion) {
            long runTime = (System.currentTimeMillis() - startTime)/1000;
            System.out.println("Benchmark time: " + runTime + " seconds.");
            System.out.println();
        System.out.println("Composite Score: " + var10[0]);
        System.out.print("FFT (" + var3 + "): ");
        if(var10[1] == 0.0D) {
            System.out.println(" ERROR, INVALID NUMERICAL RESULT!");
        } else {
            System.out.println(var10[1]);
        }

        System.out.println("SOR (" + var4 + "x" + var4 + "): " + "  " + var10[2]);
        System.out.println("Monte Carlo : " + var10[3]);
        System.out.println("Sparse matmult (N=" + var5 + ", nz=" + var6 + "): " + var10[4]);
        System.out.print("LU (" + var7 + "x" + var7 + "): ");
        if(var10[5] == 0.0D) {
            System.out.println(" ERROR, INVALID NUMERICAL RESULT!");
        } else {
            System.out.println(var10[5]);
        }

        System.out.println();
        System.out.println("java.vendor: " + System.getProperty("java.vendor"));
        System.out.println("java.version: " + System.getProperty("java.version"));
        System.out.println("os.arch: " + System.getProperty("os.arch"));
        System.out.println("os.name: " + System.getProperty("os.name"));
        System.out.println("os.version: " + System.getProperty("os.version"));

        }

        catch(Exception ex) {
            ex.printStackTrace();
        }
        return bm;
    }

}