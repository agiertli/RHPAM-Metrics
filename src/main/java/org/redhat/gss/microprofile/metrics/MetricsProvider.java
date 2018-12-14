package org.redhat.gss.microprofile.metrics;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Gauge;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.definition.QueryDefinition;
import org.kie.server.api.model.definition.QueryFilterSpec;
import org.kie.server.api.model.definition.QueryParam;
import org.kie.server.api.util.QueryFilterSpecBuilder;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;
import org.kie.server.client.QueryServicesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
@Path("/appMetrics")
public class MetricsProvider {

	private static final String SQL_EXPRESSION = "select * from ProcessInstanceInfo";
	private static final String KIE_SERVER_DATASOURCE_JNDI = "${org.kie.server.persistence.ds}";
	private static final String ACTIVE_INSTANCES_QUERY_NAME = "totalActiveInstances";
	private static Exception error;

	private QueryDefinition queryDefinition;
	private QueryFilterSpec spec;
	Logger logger = LoggerFactory.getLogger(MetricsProvider.class);

	private KieServicesClient client;
	private QueryServicesClient queryClient;

	@Inject
	@ConfigProperty(name = "kie.server.url", defaultValue = "http://localhost:8280/kie-server/services/rest/server")
	private String kieServerUrl;

	@Inject
	@ConfigProperty(name = "kie.server.username", defaultValue = "anton")
	private String kieServerUsername;

	@Inject
	@ConfigProperty(name = "kie.server.password", defaultValue = "password1!")
	private String kieServerPassword;

	@Produces
	@Gauge(absolute = true, name = "totalActiveProcessInstancesCount", unit = MetricUnits.NONE)
	public Long getActiveInstancesCount() {

		logger.debug("executing activeInstancesCount");
		if (client != null) {

			List<?> result = queryClient.query(ACTIVE_INSTANCES_QUERY_NAME, QueryServicesClient.QUERY_MAP_RAW, spec, 0,
					10, List.class);
			List<?> obscurity = (List<?>) result.get(0);
			Long activeCount = Math.round((Double) obscurity.get(0));
			return activeCount;
		} else {
			logger.error("Kie Server Client initialization failed");
			return -1L;
		}
	}

	@GET
	@Path("/init")
	@javax.ws.rs.Produces(MediaType.APPLICATION_JSON)
	public Response initMetric() {

		logger.debug("connecting to kie-server");
		logger.debug("url:" + kieServerUrl);
		logger.debug("username:" + kieServerUsername);
		logger.debug("pw:" + kieServerPassword);

		try {
			KieServicesConfiguration config = KieServicesFactory.newRestConfiguration(kieServerUrl, kieServerUsername,
					kieServerPassword);
			config.setMarshallingFormat(MarshallingFormat.JSON);
			client = KieServicesFactory.newKieServicesClient(config);
			queryClient = client.getServicesClient(QueryServicesClient.class);
			queryDefinition = QueryDefinition.builder().name(ACTIVE_INSTANCES_QUERY_NAME).expression(SQL_EXPRESSION)
					.source(KIE_SERVER_DATASOURCE_JNDI).target("PROCESS").build();
			spec = new QueryFilterSpecBuilder().get();
			QueryParam[] params = new QueryParam[1];
			QueryParam count = new QueryParam("instanceId", "COUNT", Arrays.asList("instanceId"));
			params[0] = count;
			spec.setParameters(params);

			try {
				queryClient.getQuery(ACTIVE_INSTANCES_QUERY_NAME);
			} catch (org.kie.server.api.exception.KieServicesHttpException e) {
				if (e.getHttpCode() == 404) {
					queryClient.registerQuery(queryDefinition);
				}
			}
		} catch (Exception e) {
			error = e;
			e.printStackTrace(System.out);
			client = null;
			queryClient = null;
		}

		if (client != null) {
			return Response.ok().entity("{\"appMetricsInitialized\":\"true\"}").build();
		} else
			return Response.serverError().entity("{\"appMetricsInitialized\":" + "\"" + error.getMessage() + "\"" + "}")
					.build();
	}

	@PostConstruct
	public void initBean() {

		// not going to need this after all

	}

}
