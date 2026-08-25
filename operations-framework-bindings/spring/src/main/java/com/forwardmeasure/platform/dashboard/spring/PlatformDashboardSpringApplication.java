package com.forwardmeasure.platform.dashboard.spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Spring Boot platform-dashboard host. */
@SpringBootApplication(scanBasePackages = "com.forwardmeasure.platform.dashboard")
public class PlatformDashboardSpringApplication {
  public static void main(String[] arguments) {
    SpringApplication.run(PlatformDashboardSpringApplication.class, arguments);
  }
}
