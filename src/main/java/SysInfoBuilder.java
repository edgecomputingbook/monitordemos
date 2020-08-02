import com.google.gson.Gson;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import oshi.SystemInfo;
import oshi.hardware.*;
import oshi.software.os.OSFileStore;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static jdk.nashorn.internal.objects.NativeMath.round;

class SysInfoBuilder {
    private SystemInfo systemInfo;
    private HardwareAbstractionLayer hardwareAbstractionLayer;
    private OperatingSystem os;
    private Gson gson;
    private Timer timerc;

    public SysInfoBuilder() {
        gson = new Gson();
        systemInfo = new SystemInfo();
        hardwareAbstractionLayer = systemInfo.getHardware();
        os = systemInfo.getOperatingSystem();
        MeterRegistry registry = new SimpleMeterRegistry();
        timerc = Timer
                .builder("my.timer")
                .description("a description of what this timer does") // optional
                .register(registry);
    }

    public String getSysInfoFullMap() {
        String jsonInfo = null;
        try {
            systemInfo = new SystemInfo();
            hardwareAbstractionLayer = systemInfo.getHardware();
            os = systemInfo.getOperatingSystem();

            Map<String,List<Map<String,String>>> info = new HashMap<>();
            info.put("os",getOSInfo());
            info.put("cpu",getCPUInfo());
            info.put("mem",getMemoryInfo());
            info.put("disk",getDiskInfo());
            info.put("fs",getFSInfo());
            info.put("part",getPartitionInfo());
            info.put("net",getNetworkInfo());
            info.put("proc",getProcessInfo());

            //getSensorInfo();
            jsonInfo = gson.toJson(info);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return jsonInfo;
    }

    public String getSysInfoMap() {
        String jsonInfo = null;
        try {

            if(systemInfo == null) {
                systemInfo = new SystemInfo();
            }

            if(hardwareAbstractionLayer == null) {
                hardwareAbstractionLayer = systemInfo.getHardware();
            }

            if(os == null) {
                os = systemInfo.getOperatingSystem();
            }

            Map<String,List<Map<String,String>>> info = new HashMap<>();
            info.put("os",getOSInfo());
            info.put("cpu",getCPUInfo());
            info.put("mem",getMemoryInfo());
            info.put("disk",getDiskInfo());

            //todo Raspbian GNU/Linux 9 (stretch) build 4.14.34-v7+ (32-bit)
            // and perhaps others hang on os.getFileSystem().getFileStores()
            if(!os.getFamily().contains("Raspbian")) {
                info.put("fs",getFSInfo());
            } else {
                //logger.trace("Disabling FSInfo for Raspbian-based systems.");
            }
            info.put("part",getPartitionInfo());
            info.put("net",getNetworkInfo());
            //info.put("proc",getProcessInfo());

            //getSensorInfo();
            jsonInfo = gson.toJson(info);
            info.clear();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonInfo;
    }

    public List<Map<String,String>> getProcessInfo() {
        List<Map<String,String>> list = null;
        try{
            list = new ArrayList<>();

            OSProcess[] p = os.getProcesses(0, OperatingSystem.ProcessSort.CPU);
            for(OSProcess op : p) {
                Map<String,String> info = new HashMap<>();
                info.put("name",op.getName());

                //info.put("commandline",op.getCommandLine());
                info.put("path",op.getPath());
                info.put("bytes-read",String.valueOf(op.getBytesRead()));
                info.put("bytes-written",String.valueOf(op.getBytesWritten()));
                info.put("kernel-time",String.valueOf(op.getKernelTime()));
                info.put("virtual-size",String.valueOf(op.getVirtualSize()));
                info.put("thread-count",String.valueOf(op.getThreadCount()));
                list.add(info);
                if(op.getCommandLine().contains("monitordemos-1.0-SNAPSHOT.jar")) {
                    System.out.println(info);
                    System.out.println(op.getCommandLine());
                    //System.out.println(op.get);
                }
            }
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
        return list;
    }

    public int getBenchmarkPid() {
        int pid = -1;

        try{
            OSProcess[] p = os.getProcesses(0, OperatingSystem.ProcessSort.CPU);
            for(OSProcess op : p) {
                if(op.getCommandLine().contains("monitordemos-1.0-SNAPSHOT.jar")) {
                    System.out.println(op.getCommandLine());
                    pid = op.getProcessID();
                }
            }
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }


        return pid;
    }

    public void printBenchmarkMonitor(int pid, String monitorType) {
        try {
            //DecimalFormat df = new DecimalFormat("###.#");

            //int pid = 123;
            long currentTime = 0;
            long previousTime = 0;
            long timeDifference = 0;
            OSProcess process;

            //SystemInfo si = new SystemInfo();
            //OperatingSystem os = si.getOperatingSystem();
            CentralProcessor processor = hardwareAbstractionLayer.getProcessor();
            int cpuNumber = processor.getLogicalProcessorCount();
            boolean processExists = true;
            long[][] longs = hardwareAbstractionLayer.getProcessor().getProcessorCpuLoadTicks();
            while (processExists) {
                process = os.getProcess(pid);
                if (process != null) {
                    // CPU
                    currentTime = process.getKernelTime() + process.getUserTime();

                    if (previousTime != -1) {
                        // If we have both a previous and a current time
                        // we can calculate the CPU usage
                        timeDifference = currentTime - previousTime;
                        double singleCPU = 100d * (timeDifference / ((double) 1000));
                        double totalCPU = singleCPU / cpuNumber;
                        timerc.record((long)singleCPU, TimeUnit.MILLISECONDS);
                        System.out.println(monitorType + " Total CPU: " + (int)totalCPU + "%");
                        System.out.println(monitorType + " Single CPU: " + (int)singleCPU + "%");
                        System.out.println(monitorType + " MEAN Single CPU: " + (int)timerc.mean(TimeUnit.MILLISECONDS));


                        //System.out.println("Benchmark Bytes-read: " + process.getBytesRead());
                        //System.out.println("Benchmark Bytes-written: " + process.getBytesWritten());

                        //long[][] longs = hardwareAbstractionLayer.getProcessor().getProcessorCpuLoadTicks();
                        double[] d = hardwareAbstractionLayer.getProcessor().getProcessorCpuLoadBetweenTicks(longs);

                        int coreCount = 0;
                        System.out.print("Logical Core Utilization: ");
                        for (double l1 : d) {
                            System.out.print("c" + coreCount + "[" + (int)(l1 * 100) + "]" + "% ");
                            coreCount++;
                        }
                        System.out.println("\n");
                    }

                    previousTime = currentTime;
                    longs = hardwareAbstractionLayer.getProcessor().getProcessorCpuLoadTicks();
                    Thread.sleep(1000);
                } else {
                    processExists = false;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private List<Map<String,String>> getFSInfo() {
        List<Map<String,String>> list = null;
        try{
            list = new ArrayList<>();

            //OSFileStore[] fsArray = hardwareAbstractionLayer.getFileSystem().getFileStores();

            OSFileStore[] fsArray = os.getFileSystem().getFileStores();

            for (OSFileStore fs : fsArray) {
                Map<String,String> info = new HashMap<>();
                info.put("name",fs.getName());
                info.put("description",fs.getDescription());
                info.put("mount",fs.getMount());
                info.put("type",fs.getType());
                info.put("uuid",fs.getUUID());
                info.put("volume",fs.getVolume());
                info.put("total-space",String.valueOf(fs.getTotalSpace()));
                info.put("available-space",String.valueOf(fs.getUsableSpace()));
                list.add(info);
            }
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
        return list;
    }

    public List<Map<String,String>> getOSInfo() {
        List<Map<String,String>> list = null;
        try{
            list = new ArrayList<>();
            Map<String,String> info = new HashMap<>();
            try {
                info.put("sys-os", os.getVersion().toString());
            } catch (Exception e) {
                info.put("sys-os", "unknown");
            }

            try {
                info.put("sys-family", os.getFamily());
            } catch (Exception e) {
                info.put("sys-family", "unknown");
            }
            //info.put("sys-threadcount", String.valueOf(os.getThreadCount()));
            try {
                info.put("sys-manufacturer", os.getManufacturer());
            } catch (Exception e) {
                info.put("sys-manufacturer", "unknown");
            }
            //info.put("sys-uptime", FormatUtil.formatElapsedSecs(hardwareAbstractionLayer.getProcessor().getSystemUptime()));
            try {
                info.put("sys-uptime", String.valueOf(os.getSystemUptime()));
            } catch (Exception e) {
                info.put("sys-uptime", "unknown");
            }
            try {
                info.put("process-count", String.valueOf(os.getProcessCount()));
            } catch (Exception e) {
                info.put("process-count", "unknown");
            }
            list.add(info);
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
        return list;
    }

    private List<Map<String,String>> getMemoryInfo() {
        List<Map<String,String>> list = null;
        try{
            list = new ArrayList<>();
            Map<String,String> info = new HashMap<>();
            info.put("memory-total", String.valueOf(hardwareAbstractionLayer.getMemory().getTotal()));
            info.put("memory-available", String.valueOf(hardwareAbstractionLayer.getMemory().getAvailable()));
            info.put("swap-total", String.valueOf(hardwareAbstractionLayer.getMemory().getVirtualMemory().getSwapTotal()));
            info.put("swap-used", String.valueOf(hardwareAbstractionLayer.getMemory().getVirtualMemory().getSwapUsed()));
            list.add(info);

        }
        catch(Exception ex) {
            ex.printStackTrace();

        }
        return list;
    }

    private List<Map<String,String>> getPartitionInfo() {
        List<Map<String,String>> list = null;
        try{
            list = new ArrayList<>();

            HWDiskStore[] diskArray = hardwareAbstractionLayer.getDiskStores();
            for (HWDiskStore disk : diskArray) {

                for(HWPartition part : disk.getPartitions()) {
                    Map<String,String> info = new HashMap<>();
                    info.put("part-id", String.valueOf(part.getIdentification()));
                    info.put("part-name", String.valueOf(part.getName()));
                    info.put("part-size", String.valueOf(part.getSize()));
                    info.put("part-mount", String.valueOf(part.getMountPoint()));
                    list.add(info);
                }
            }

        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
        return list;
    }

    private List<Map<String,String>> getDiskInfo() {

        List<Map<String,String>> list = null;
        try{
            list = new ArrayList<>();
            HWDiskStore[] diskArray = hardwareAbstractionLayer.getDiskStores();

            for (HWDiskStore disk : diskArray) {
                Map<String, String> info = new HashMap<>();

                info.put("disk-size", String.valueOf(disk.getSize()));
                info.put("disk-model", String.valueOf(disk.getModel()));
                info.put("disk-name", String.valueOf(disk.getName()));
                info.put("disk-readbytes", String.valueOf(disk.getReadBytes()));
                info.put("disk-reads", String.valueOf(disk.getReads()));
                info.put("disk-writebytes", String.valueOf(disk.getWriteBytes()));
                info.put("disk-writes", String.valueOf(disk.getWrites()));
                info.put("disk-transfertime", String.valueOf(disk.getTransferTime()));

                list.add(info);
            }
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
        return list;
    }

    private List<Map<String,String>> getNetworkInfo() {
        List<Map<String,String>> list = null;
        try{
            list = new ArrayList<>();

            NetworkIF[] networks = hardwareAbstractionLayer.getNetworkIFs();
            for (NetworkIF net : networks) {
                Map<String,String> info = new HashMap<>();

                info.put("ipv4-addresses", gson.toJson(net.getIPv4addr()));
                info.put("ipv6-addresses", gson.toJson(net.getIPv6addr()));
                info.put("bytes-received", String.valueOf(net.getBytesRecv()));
                info.put("bytes-sent", String.valueOf(net.getBytesSent()));
                info.put("packets-received", String.valueOf(net.getPacketsRecv()));
                info.put("packets-sent", String.valueOf(net.getPacketsSent()));
                info.put("mtu",String.valueOf(net.getMTU()));
                info.put("interface-name",net.getName());
                info.put("mac",net.getMacaddr());
                info.put("errors-in",String.valueOf(net.getInErrors()));
                info.put("errors-out",String.valueOf(net.getOutErrors()));
                info.put("link-speed", String.valueOf(net.getSpeed()));
                info.put("timestamp",String.valueOf(net.getTimeStamp()));
                list.add(info);
            }
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
        return list;
    }

    public List<Map<String,String>> getCPUInfo() {
        List<Map<String,String>> list = null;
        try{
            list = new ArrayList<>();
            Map<String,String> info = new HashMap<>();

            try {
                info.put("is64bit", String.valueOf(hardwareAbstractionLayer.getProcessor().isCpu64bit()));
            }
            catch (Exception ex) {
                info.put("is64bit", "unknown");
            }
            try {
                info.put("cpu-physical-count", String.valueOf(hardwareAbstractionLayer.getProcessor().getPhysicalProcessorCount()));
            }
            catch (Exception ex) {
                info.put("cpu-physical-count", "1");
            }
            try {
                System.out.println("GET CPU: " + hardwareAbstractionLayer.getProcessor().getPhysicalProcessorCount());

                //CentralProcessor.LogicalProcessor[] lps = hardwareAbstractionLayer.getProcessor().get
                //for(CentralProcessor.LogicalProcessor lp : lps) {
                //    System.out.println("CODY: " + lp.);
                //}
                info.put("cpu-logical-count", String.valueOf(hardwareAbstractionLayer.getProcessor().getLogicalProcessorCount()));
            }
            catch (Exception ex){
                ex.printStackTrace();
                info.put("cpu-logical-count", "1");
            }
            try {
                info.put("cpu-summary", hardwareAbstractionLayer.getProcessor().toString());
            }
            catch (Exception ex){
                info.put("cpu-summary", "unknown");
            }
            try {
                info.put("cpu-ident", hardwareAbstractionLayer.getProcessor().getIdentifier());
            }
            catch (Exception ex) {
                info.put("cpu-ident", "unknown");
            }
            try {
                info.put("cpu-id",hardwareAbstractionLayer.getProcessor().getProcessorID());
            }
            catch(Exception ex) {
                info.put("cpu-id","unknown");
            }

            try {
                //performance

                int logicalProcessorCount = hardwareAbstractionLayer.getProcessor().getLogicalProcessorCount();
                int packageCount = hardwareAbstractionLayer.getProcessor().getPhysicalPackageCount();
                int processorCount = hardwareAbstractionLayer.getProcessor().getPhysicalProcessorCount();

                System.out.println(packageCount);
                System.out.println(processorCount);
                System.out.println(logicalProcessorCount);


                //long[][] longs = new long[8][8];
                long[][] longs = hardwareAbstractionLayer.getProcessor().getProcessorCpuLoadTicks();

                while(true) {
                    //systemInfo = new SystemInfo();
                    //hardwareAbstractionLayer = systemInfo.getHardware();
                    double[] d = hardwareAbstractionLayer.getProcessor().getProcessorCpuLoadBetweenTicks(longs);

                    for (double l1 : d) {
                        System.out.println(l1);
                    }
                    Thread.sleep(1000);
                    System.out.println("---");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            list.add(info);

        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
        return list;
    }


/*
    private Map<String,String> getSensorInfo() {
        Map<String,String> info = null;
        try{

            info = new HashMap<>();
            info.put("sys-temperature", String.format("%.1f", hardwareAbstractionLayer.getSensors().getCpuTemperature()));
            info.put("sys-voltage", String.format("%.1f",hardwareAbstractionLayer.getSensors().getCpuVoltage()));
            StringBuilder fanStringBuilder = new StringBuilder();
            for(int fanspeed : hardwareAbstractionLayer.getSensors().getFanSpeeds()) {
                fanStringBuilder.append(String.valueOf(fanspeed)).append(",");
            }

            int index = fanStringBuilder.lastIndexOf(",");
            if(index != -1) {
                fanStringBuilder.deleteCharAt(index);
            }

            info.put("sys-fanspeeds", fanStringBuilder.toString());

        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
        return info;
    }
*/


    private Map<String,String> getNetworkInterfaces() {
        Map<String,String> info = null;
        try{
            info = new HashMap<>();
            int nicCount = 0;
            StringBuilder nicStringBuilder = new StringBuilder();
            //try {

            Enumeration<NetworkInterface> nicEnum = NetworkInterface.getNetworkInterfaces();

            while (nicEnum.hasMoreElements()) {
                NetworkInterface nic = nicEnum.nextElement();

                if (nic.isLoopback())
                    continue;
                nicStringBuilder.append(String.valueOf(nicCount)).append(":").append(nic.getName()).append(",");
                StringBuilder interfaceAddressStringBuilder = new StringBuilder();

                for (InterfaceAddress interfaceAddress : nic.getInterfaceAddresses()) {
                    if (interfaceAddress == null)
                        continue;
                    try {
                        InetAddress address = interfaceAddress.getAddress();
                        interfaceAddressStringBuilder.append(address.getHostAddress()).append(",");
                    } catch (Exception e) {
                        System.out.println("SysInfoBuilder : Constructor : nicLoop : addrLoop : Error : " + e.getMessage());
                    }
                }
                if (interfaceAddressStringBuilder.length() == 0)
                    continue;
                interfaceAddressStringBuilder.deleteCharAt(interfaceAddressStringBuilder.lastIndexOf(","));
                info.put("nic-" + String.valueOf(nicCount) + "-ip", interfaceAddressStringBuilder.toString());
                info.put("nic-" + String.valueOf(nicCount) + "-mtu", String.valueOf(nic.getMTU()));

            }

            //NetworkIF[] networkIFs = hardwareAbstractionLayer.getNetworkIFs();
            //networkIFs[0].
            //NetworkIF[] networkIFs = hal.getNetworkIFs();


            int index = nicStringBuilder.lastIndexOf(",");
            if(index != -1) {
                nicStringBuilder.deleteCharAt(index);
            }

            info.put("nic-map", nicStringBuilder.toString());
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
        return info;
    }

}