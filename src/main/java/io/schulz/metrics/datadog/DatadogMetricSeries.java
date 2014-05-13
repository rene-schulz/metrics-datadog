package io.schulz.metrics.datadog;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;

import java.util.List;

import static io.schulz.metrics.datadog.DatadogMetricSeries.DatadogMetric.TYPE.COUNTER;
import static io.schulz.metrics.datadog.DatadogMetricSeries.DatadogMetric.TYPE.GAUGE;


public class DatadogMetricSeries {

    private transient final long timestamp;
    private transient final String host;
    private transient final String[] tags;

    private List<DatadogMetric> series;

    public DatadogMetricSeries(long timestamp, String host, String[] tags) {
        this.timestamp = timestamp;
        this.host = host;
        this.tags = tags;
        series = Lists.newArrayList();
    }

    public void addGauge(String name, Number value) {
        series.add(new DatadogMetric(name, timestamp, value, GAUGE, host, tags));
    }

    public void addCounter(String name, Long value) {
        series.add(new DatadogMetric(name, timestamp, value, COUNTER, host, tags));
    }

    @JsonProperty
    public List<DatadogMetric> getSeries() {
        return this.series;
    }

    static class DatadogMetric {

        static enum TYPE {
            COUNTER,
            GAUGE
        }

        private String metric;
        private List<Number[]> points;
        private TYPE type;
        private String host;
        private String[] tags;

        DatadogMetric(String metric, Long epoch, Number value, TYPE type, String host, String[] tags) {
            this.metric = metric;
            this.points = Lists.<Number[]>newArrayList(new Number[]{epoch, value});
            this.type = type;
            this.host = host;
            this.tags = tags;
        }

        @JsonProperty
        public String getMetric() {
            return metric;
        }

        @JsonProperty
        public List<Number[]> getPoints() {
            return points;
        }

        @JsonProperty
        public TYPE getType() {
            return type;
        }

        @JsonProperty
        public String getHost() {
            return host;
        }

        @JsonProperty
        public String[] getTags() {
            return tags;
        }
    }
}
