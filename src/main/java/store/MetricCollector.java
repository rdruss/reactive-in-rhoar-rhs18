package store;

import io.prometheus.client.Collector;
import io.prometheus.client.GaugeMetricFamily;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class MetricCollector extends Collector {
    private static final com.sun.management.OperatingSystemMXBean osBean = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    private static final FileSystem defaultFs = Paths.get(System.getProperty("user.dir")).getFileSystem();

    @Override
    public List<MetricFamilySamples> collect() {
        List<MetricFamilySamples> mfs = new ArrayList<>();

        mfs.add(new GaugeMetricFamily("os_load_average", "The system load average for the last minute",
            osBean.getSystemLoadAverage()));
        mfs.add(new GaugeMetricFamily("os_avail_processors", "The number of processors available to the Java virtual machine",
            osBean.getAvailableProcessors()));
        mfs.add(new GaugeMetricFamily("os_system_cpu_load", "The system CPU load as a number between 0 and 1",
            osBean.getSystemCpuLoad()));

        long availDiskSpace = 0;
        long totalDiskSpace = 0;

        try {
            for (FileStore store : defaultFs.getFileStores()) {
                availDiskSpace += store.getUsableSpace();
                totalDiskSpace += store.getTotalSpace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        mfs.add(new GaugeMetricFamily("disk_space_bytes_used", "The disk space used in bytes for all filesystems",
            totalDiskSpace - availDiskSpace));
        mfs.add(new GaugeMetricFamily("disk_space_bytes_max", "The total disk space in bytes for all filesystems",
            totalDiskSpace));

        return mfs;
    }
}
