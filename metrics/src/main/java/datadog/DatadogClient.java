package datadog;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public final class DatadogClient {

    private static final Logger LOG = LoggerFactory.getLogger(DatadogClient.class);

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient;
    private final URI apiUri;

    public DatadogClient(String apiEndPoint, String apiKey) throws URISyntaxException {
        httpClient = newHttpClient();
        apiUri = buildAPIURI(apiEndPoint, apiKey);
    }

    private URI buildAPIURI(String uri, String apiKey) throws URISyntaxException {
        return new URIBuilder(uri).addParameter("api_key", apiKey).build();
    }

    private HttpClient newHttpClient() {
        return HttpClientBuilder.create().setConnectionManager(new PoolingHttpClientConnectionManager()).build();
    }

    public void push(DatadogMetricSeries message) {
        try {
            HttpPost post = new HttpPost(apiUri);
            post.addHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
            post.setEntity(jsonify(message));
            HttpResponse response = httpClient.execute(post);
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_ACCEPTED) {
                LOG.warn(String.format("Error submitting datadog metrics: %s", response.getStatusLine()));
            }
            EntityUtils.consumeQuietly(response.getEntity());
        } catch (IOException e) {
            LOG.warn("Error submitting datadog metrics", e);
        }
    }

    private StringEntity jsonify(Object entity) throws JsonProcessingException {
        return new StringEntity(objectMapper.writeValueAsString(entity), ContentType.APPLICATION_JSON);
    }
}
