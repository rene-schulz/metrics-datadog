package datadog;

import com.codahale.metrics.*;

import java.net.URISyntaxException;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("unused")
public final class DatadogReporter extends ScheduledReporter {

    public static Builder forRegistry(MetricRegistry registry) {
        return new Builder(registry);
    }

    public static class  Builder {

        private MetricRegistry registry;
        private String datadogKey;
        private String apiEndpoint;
        private MetricFilter filter;
        private TimeUnit rateUnit;
        private TimeUnit durationUnit;
        private Clock clock;

        Builder(MetricRegistry registry) {
            this.registry = registry;
            filter = MetricFilter.ALL;
            rateUnit = TimeUnit.SECONDS;
            durationUnit = TimeUnit.MILLISECONDS;
            clock = Clock.defaultClock();
        }


        public ScheduledReporter build() throws URISyntaxException {
            return new DatadogReporter(registry,
                                       clock,
                                       apiEndpoint,
                                       datadogKey,
                                       "datadog-reporter",
                                       filter,
                                       rateUnit,
                                       durationUnit);
        }

        public Builder withClock(Clock clock) {
            this.clock = clock;
            return this;
        }

        public Builder withAPIEndPoint(String apiEndpoint) {
            this.apiEndpoint = apiEndpoint;
            return this;
        }

        public Builder withAPIKey(String datadogKey) {
            this.datadogKey = datadogKey;
            return this;
        }

        public Builder convertDurationsTo(TimeUnit durationUnit) {
            this.durationUnit = durationUnit;
            return this;
        }


        public Builder convertRatesTo(TimeUnit rateUnit) {
            this.rateUnit = rateUnit;
            return this;
        }

        public Builder filter(MetricFilter filter) {
            this.filter = filter;
            return this;
        }
    }

    private final DatadogClient ddClient;
    private final Clock clock;

    /**
     * Creates a new {@link com.codahale.metrics.ScheduledReporter} instance.
     *
     * @param registry      the {@link com.codahale.metrics.MetricRegistry} containing the metrics this
     *                      reporter will report
     * @param apiKey        the key for Datadog API
     * @param name          the reporter's name
     * @param filter        the filter for which metrics to report
     */
    protected DatadogReporter(MetricRegistry registry,
                              Clock clock,
                              String apiEndpoint,
                              String apiKey,
                              String name,
                              MetricFilter filter,
                              TimeUnit rateUnit,
                              TimeUnit durationUnit) throws URISyntaxException {
        super(registry, name, filter, rateUnit, durationUnit);
        this.clock = clock;
        ddClient = new DatadogClient(apiEndpoint, apiKey);
    }

    @Override
    public void report(SortedMap<String, Gauge> gauges,
                       SortedMap<String, Counter> counters,
                       SortedMap<String, Histogram> histograms,
                       SortedMap<String, Meter> meters,
                       SortedMap<String, Timer> timers) {
        final long timestamp = TimeUnit.MILLISECONDS.toSeconds(clock.getTime());
        DatadogMetricSeries ddMetric = new DatadogMetricSeries(timestamp, null, null);

        for (Map.Entry<String, Gauge> entry : gauges.entrySet()) {
            Object value = entry.getValue().getValue();
            ddMetric.addGauge(entry.getKey(), (value instanceof Number) ? (Number) value : null);
        }

        for (Map.Entry<String, Counter> entry : counters.entrySet()) {
            ddMetric.addCounter(entry.getKey(), entry.getValue().getCount());
        }

        for (Map.Entry<String, Histogram> entry : histograms.entrySet()) {
            processHistogram(ddMetric, entry.getKey(), entry.getValue());
        }

        for (Map.Entry<String, Meter> entry : meters.entrySet()) {
            processMetered(ddMetric, entry.getKey(), entry.getValue());
        }
        for (Map.Entry<String, Timer> entry : timers.entrySet()) {
            processTimer(ddMetric, entry.getKey(), entry.getValue());
        }

        ddClient.push(ddMetric);
    }

    private void processHistogram(DatadogMetricSeries ddMetrics, String key, Histogram histogram) {
        ddMetrics.addGauge(key + ".count", histogram.getCount());
        Snapshot snapshot = histogram.getSnapshot();
        ddMetrics.addGauge(key + ".min", snapshot.getMin());
        ddMetrics.addGauge(key + ".max", snapshot.getMax());
        ddMetrics.addGauge(key + ".mean", snapshot.getMean());
        ddMetrics.addGauge(key + ".mean", snapshot.getMean());
        ddMetrics.addGauge(key + ".stddev", snapshot.getStdDev());
        ddMetrics.addGauge(key + ".median", snapshot.getMedian());
        ddMetrics.addGauge(key + ".75percentile", snapshot.get75thPercentile());
        ddMetrics.addGauge(key + ".95percentile", snapshot.get95thPercentile());
        ddMetrics.addGauge(key + ".98percentile", snapshot.get98thPercentile());
        ddMetrics.addGauge(key + ".99percentile", snapshot.get99thPercentile());
        ddMetrics.addGauge(key + ".999percentile", snapshot.get999thPercentile());
    }

    private void processMetered(DatadogMetricSeries ddMetrics, String key, Metered metered) {
        ddMetrics.addGauge(key + ".count", metered.getCount());
        ddMetrics.addGauge(key + ".mean_rate", convertRate(metered.getMeanRate()));
        ddMetrics.addGauge(key + ".1minute_rate", convertRate(metered.getOneMinuteRate()));
        ddMetrics.addGauge(key + ".5minute_rate", convertRate(metered.getFiveMinuteRate()));
        ddMetrics.addGauge(key + ".15minute_rate", convertRate(metered.getFifteenMinuteRate()));
    }

    private void processTimer(DatadogMetricSeries ddMetrics, String key, Timer timer) {
        processMetered(ddMetrics, key, timer);
        Snapshot snapshot = timer.getSnapshot();
        ddMetrics.addGauge(key + ".min", convertDuration(snapshot.getMin()));
        ddMetrics.addGauge(key + ".max", convertDuration(snapshot.getMax()));
        ddMetrics.addGauge(key + ".mean", convertDuration(snapshot.getMean()));
        ddMetrics.addGauge(key + ".stddev", convertDuration(snapshot.getStdDev()));
        ddMetrics.addGauge(key + ".median", convertDuration(snapshot.getMedian()));
        ddMetrics.addGauge(key + ".75percentile", convertDuration(snapshot.get75thPercentile()));
        ddMetrics.addGauge(key + ".95percentile", convertDuration(snapshot.get95thPercentile()));
        ddMetrics.addGauge(key + ".98percentile", convertDuration(snapshot.get98thPercentile()));
        ddMetrics.addGauge(key + ".99percentile", convertDuration(snapshot.get99thPercentile()));
        ddMetrics.addGauge(key + ".999percentile", convertDuration(snapshot.get999thPercentile()));
    }

}
