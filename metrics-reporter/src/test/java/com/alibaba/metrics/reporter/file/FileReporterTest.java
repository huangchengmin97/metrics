package com.alibaba.metrics.reporter.file;

import com.alibaba.metrics.Counter;
import com.alibaba.metrics.IMetricManager;
import com.alibaba.metrics.MetricLevel;
import com.alibaba.metrics.MetricManager;
import com.alibaba.metrics.MetricName;
import com.alibaba.metrics.MetricRegistry;
import com.alibaba.metrics.common.config.MetricsCollectPeriodConfig;
import com.alibaba.metrics.jvm.GarbageCollectorMetricSet;
import com.alibaba.metrics.jvm.MemoryUsageGaugeSet;
import com.alibaba.metrics.reporter.ConsoleReporter;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class FileReporterTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Ignore
    @Test
    public void test() throws IOException, InterruptedException {
        IMetricManager metricManager = MetricManager.getIMetricManager();

        MetricRegistry metricRegistry = metricManager.getMetricRegistryByGroup("system");

        RollingFileAppender appender = RollingFileAppender.builder()
                .name("metrics/metrics.log")
                .fileSize(1024 * 1024 * 10)
                .build();

        // 设置全局的report时间间隔是3秒
        MetricsCollectPeriodConfig config = new MetricsCollectPeriodConfig(3);
        // 设置CRITICAL的report时间间隔为1秒
        config.configPeriod(MetricLevel.CRITICAL, 1);

        FileMetricManagerReporter reporter = FileMetricManagerReporter.forMetricManager(metricManager)
                .withGlobalTags(Collections.singletonMap("foo", "bar"))
                .timestampPrecision(TimeUnit.MILLISECONDS)
                .fileAppender(appender)
                .metricsReportPeriodConfig(config)
                .build();

        long period = 1;
        try {
            reporter.start(period, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }

        metricRegistry.register("jvm.mem", new MemoryUsageGaugeSet());
        metricRegistry.register("jvm.gc", new GarbageCollectorMetricSet());
        Counter c = MetricManager.getCounter("test", MetricName.build("AAA").tagged("hi", null));
        c.inc(10000);

        ConsoleReporter consoleReporter = ConsoleReporter.forRegistry(metricRegistry).build();
        consoleReporter.start(period, TimeUnit.SECONDS);

        TimeUnit.SECONDS.sleep(6);
    }
}
