public class BenchMetric {

    private String INodeId;
    private long runTime;
    private double cpuComposite;
    private double cpuFFT;
    private double cpuSOR;
    private double cpuMC;
    private double cpuSM;
    private double cpuLU;

    public BenchMetric(String INodeId, long runTime, double cpuComposite, double cpuFFT, double cpuSOR, double cpuMC, double cpuSM, double cpuLU) {
        this.INodeId = INodeId;
        this.runTime = runTime;
        this.cpuComposite = cpuComposite;
        this.cpuFFT = cpuFFT;
        this.cpuSOR = cpuSOR;
        this.cpuMC = cpuMC;
        this.cpuSM = cpuSM;
        this.cpuLU = cpuLU;

    }

    public long getRunTime() {
        return runTime;
    }

    public double getCPU() {

        return cpuComposite;
    }

    public double getCpuFFT() {
        return cpuFFT;
    }

    public double getCpuSOR() {
        return  cpuSOR;
    }

    public double getCpuMC() {
        return  cpuMC;
    }

    public double getCpuSM() {
        return  cpuSM;
    }

    public double getCpuLU() {
        return  cpuLU;
    }

    public String getINodeId() {
        return INodeId;
    }


}