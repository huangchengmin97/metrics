package com.alibaba.metrics.jvm;

import com.alibaba.metrics.Metric;
import com.alibaba.metrics.MetricName;
import com.alibaba.metrics.MetricRegistry;
import com.alibaba.metrics.MetricSet;
import com.alibaba.metrics.PersistentGauge;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * A set of gauges for the counts and elapsed times of garbage collections.
 */
public class GarbageCollectorMetricSet implements MetricSet {
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]+");

    private final List<GarbageCollectorMXBean> garbageCollectors;

    /**
     * Creates a new set of gauges for all discoverable garbage collectors.
     */
    public GarbageCollectorMetricSet() {
        this(ManagementFactory.getGarbageCollectorMXBeans());
    }

    /**
     * Creates a new set of gauges for the given collection of garbage collectors.
     *
     * @param garbageCollectors    the garbage collectors
     */
    public GarbageCollectorMetricSet(Collection<GarbageCollectorMXBean> garbageCollectors) {
        this.garbageCollectors = new ArrayList<GarbageCollectorMXBean>(garbageCollectors);
    }

    @Override
    public Map<MetricName, Metric> getMetrics() {
        final Map<MetricName, Metric> gauges = new HashMap<MetricName, Metric>();
        for (final GarbageCollectorMXBean gc : garbageCollectors) {
            final String name = WHITESPACE.matcher(gc.getName()).replaceAll("_").toLowerCase();
            gauges.put(MetricRegistry.name(name, "count"), new PersistentGauge<Long>() {
                @Override
                public Long getValue() {
                    return gc.getCollectionCount();
                }
            });

            gauges.put(MetricRegistry.name(name, "time"), new PersistentGauge<Long>() {
                @Override
                public Long getValue() {
                    return gc.getCollectionTime();
                }
            });
        }
        return Collections.unmodifiableMap(gauges);
    }

    @Override
    public long lastUpdateTime() {
        return System.currentTimeMillis();
    }
}
