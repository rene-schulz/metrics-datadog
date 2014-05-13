package io.schulz.metrics.dropwizard.datadog;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.base.Throwables;
import datadog.DatadogReporter;
import io.dropwizard.metrics.BaseFormattedReporterFactory;

import javax.validation.constraints.NotNull;
import java.net.URISyntaxException;

@JsonTypeName("datadog")
public class DatadogReporterFactory extends BaseFormattedReporterFactory {

    @NotNull
    private java.lang.String endpoint = "https://app.datadoghq.com/api/v1/series";

    @NotNull
    private java.lang.String apiKey;


    @JsonProperty
    public String getEndpoint() {
        return endpoint;
    }

    @JsonProperty
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    @JsonProperty
    public String getApiKey() {
        return apiKey;
    }

    @JsonProperty
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }


    @Override
    public ScheduledReporter build(MetricRegistry registry) {
        try {
            return DatadogReporter.forRegistry(registry)
                                  .withAPIKey(getApiKey())
                                  .withAPIEndPoint(getEndpoint())
                                  .convertDurationsTo(getDurationUnit())
                                  .convertRatesTo(getRateUnit())
                                  .filter(getFilter())
                                  .build();
        } catch (URISyntaxException e) {
            throw Throwables.propagate(e);
        }
    }
}
