package com.zhuo.im.common.config;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "httpclient")
public class GlobalHttpClientConfig {
	private Integer maxTotal; // Maximum number of connections
	private Integer defaultMaxPerRoute; // Maximum number of concurrent connections
	private Integer connectTimeout;
	private Integer connectionRequestTimeout;
	private Integer socketTimeout; // Maximum time for data transmission
	private boolean staleConnectionCheckEnabled; // Check whether the connection is available when submitting

	PoolingHttpClientConnectionManager manager = null;
	HttpClientBuilder httpClientBuilder = null;

	// Define httpClient connection pool
	@Bean(name = "httpClientConnectionManager")
	public PoolingHttpClientConnectionManager getPoolingHttpClientConnectionManager() {
		return getManager();
	}

	private PoolingHttpClientConnectionManager getManager() {
		if (manager != null) {
			return manager;
		}
		manager = new PoolingHttpClientConnectionManager();
		manager.setMaxTotal(maxTotal);
		manager.setDefaultMaxPerRoute(defaultMaxPerRoute);
		return manager;
	}

	/**
	 * Instantiate the connection pool and set up the connection pool manager. We need to inject the connection pool manager instantiated above in the form of parameters.
	 * 
	 * @Qualifier Specify bean tag for injection
	 */
	@Bean(name = "httpClientBuilder")
	public HttpClientBuilder getHttpClientBuilder(
			@Qualifier("httpClientConnectionManager") PoolingHttpClientConnectionManager httpClientConnectionManager) {
		// The constructor in HttpClientBuilder is modified with protected, so we cannot directly use new to instantiate an HttpClientBuilder.
		// However, we can use the static method create() provided by HttpClientBuilder to obtain the HttpClientBuilder object.
		httpClientBuilder = HttpClientBuilder.create();
		httpClientBuilder.setConnectionManager(httpClientConnectionManager);
		return httpClientBuilder;
	}


	/**
	 * Inject into the connection pool to obtain httpClient
	 * 
	 * @param httpClientBuilder
	 * @return
	 */
	@Bean
	public CloseableHttpClient getCloseableHttpClient(
			@Qualifier("httpClientBuilder") HttpClientBuilder httpClientBuilder) {
		return httpClientBuilder.build();
	}

	public CloseableHttpClient getCloseableHttpClient() {
		if (httpClientBuilder != null) {
			return httpClientBuilder.build();
		}
		httpClientBuilder = HttpClientBuilder.create();
		httpClientBuilder.setConnectionManager(getManager());
		return httpClientBuilder.build();
	}

	/**
	 * Builder is an internal class of RequestConfig. Obtain a Builder object through the custom method of RequestConfig.
	 * Set builder’s connection information
	 * 
	 * @return
	 */
	@Bean(name = "builder")
	public RequestConfig.Builder getBuilder() {
		RequestConfig.Builder builder = RequestConfig.custom();
		return builder.setConnectTimeout(connectTimeout).setConnectionRequestTimeout(connectionRequestTimeout)
				.setSocketTimeout(socketTimeout).setStaleConnectionCheckEnabled(staleConnectionCheckEnabled);
	}

	/**
	 * Use builder to build a RequestConfig object
	 * 
	 * @param builder
	 * @return
	 */
	@Bean
	public RequestConfig getRequestConfig(@Qualifier("builder") RequestConfig.Builder builder) {
		return builder.build();
	}

	public Integer getMaxTotal() {
		return maxTotal;
	}

	public void setMaxTotal(Integer maxTotal) {
		this.maxTotal = maxTotal;
	}

	public Integer getDefaultMaxPerRoute() {
		return defaultMaxPerRoute;
	}

	public void setDefaultMaxPerRoute(Integer defaultMaxPerRoute) {
		this.defaultMaxPerRoute = defaultMaxPerRoute;
	}

	public Integer getConnectTimeout() { return connectTimeout;}

	public void setConnectTimeout(Integer connectTimeout) { this.connectTimeout = connectTimeout;}

	public Integer getConnectionRequestTimeout() {
		return connectionRequestTimeout;
	}

	public void setConnectionRequestTimeout(Integer connectionRequestTimeout) {
		this.connectionRequestTimeout = connectionRequestTimeout;
	}

	public Integer getSocketTimeout() {
		return socketTimeout;
	}

	public void setSocketTimeout(Integer socketTimeout) {
		this.socketTimeout = socketTimeout;
	}

	public boolean isStaleConnectionCheckEnabled() {
		return staleConnectionCheckEnabled;
	}

	public void setStaleConnectionCheckEnabled(boolean staleConnectionCheckEnabled) {
		this.staleConnectionCheckEnabled = staleConnectionCheckEnabled;
	}
}
