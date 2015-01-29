package org.bc.hello;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import java.util.HashMap;

import org.bc.hello.health.TemplateHealthCheck;
import org.bc.hello.resources.HelloResource;
import org.bc.server.Quasar;

public class HelloApplication extends Application<HelloConfiguration> {

	static HashMap<String, String> users = null;
	Quasar quasar = null;

	public HelloApplication(HashMap<String, String> userLogins, Quasar quasar) {
		users = userLogins;
		this.quasar = quasar;
	}

	@Override
	public String getName() {
		return "Hello";
	}

	@Override
	public void initialize(Bootstrap<HelloConfiguration> bootstrap) {
	}

	@Override
	public void run(HelloConfiguration configuration, Environment environment) {
		environment.jersey().register(
				new HelloResource(configuration.template,
						configuration.defaultName, users, this.quasar));
		environment.healthChecks().register("template",
				new TemplateHealthCheck(configuration.template));
	}

}
