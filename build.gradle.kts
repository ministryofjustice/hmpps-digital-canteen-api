plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "11.0.1"
  id("org.openapi.generator") version "7.24.0"
  kotlin("plugin.spring") version "2.4.10"
  id("dev.detekt") version "2.0.0-alpha.5"
}
dependencyCheck {
  skipConfigurations.add("detekt")
}

dependencies {
  implementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter:3.0.0")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-webclient")
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.3")

  testImplementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter-test:2.5.0")
  testImplementation("org.springframework.boot:spring-boot-starter-webflux-test")
  testImplementation("org.wiremock:wiremock-standalone:3.13.2")
  testImplementation("io.swagger.parser.v3:swagger-parser:2.1.43") {
    exclude(group = "io.swagger.core.v3")
  }
}

// detekt must use a specific kotlin version when running, this block ensures it's using the correct version
// this is variation on https://detekt.dev/docs/gettingstarted/gradle/#gradle-runtime-dependencies
configurations.matching { it.name == "detekt" }.all {
  resolutionStrategy.eachDependency {
    if (requested.group == "org.jetbrains.kotlin") {
      useVersion(
        dev.detekt.gradle.plugin
          .getSupportedKotlinVersion(),
      )
    }
  }
}

kotlin {
  jvmToolchain(25)
  compilerOptions {
    freeCompilerArgs.addAll("-Xannotation-default-target=param-property")
  }
}

tasks {
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions.jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_25
  }
}
