package org.redhat.gss.microprofile.metrics;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import java.io.File;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.ScopeType;
import org.junit.Test;
import org.junit.runner.RunWith;
import io.restassured.http.Header;

@RunWith(Arquillian.class)

public class MetricsTest {

	@Deployment
	public static WebArchive createDeployment() throws Exception {

		File[] deps = Maven.resolver().loadPomFromFile("pom.xml")
				.importDependencies(ScopeType.COMPILE, ScopeType.RUNTIME).resolve().withTransitivity().asFile();
		WebArchive deployment = ShrinkWrap.create(WebArchive.class);
		deployment.addPackage(MetricsProvider.class.getPackage());
		deployment.addAsLibraries(deps);
		return deployment;
	}

	@Test
	@RunAsClient
	public void testTotalActiveProcessInstancesCountMetric() {

		// We are not testing integration with kie-server
		// We are testing that our metric is published
		
		given()
		  .header(new Header("Accept","application/json"))
		.when()
		  .get("/metrics/application")
		.then()
		  .assertThat()
		  .statusCode(200)
		  .body("totalActiveProcessInstancesCount", is(-1));

	}

}
