import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.KtlintExtension
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "11.0.4"
  id("org.openapi.generator") version "7.24.0"
  kotlin("plugin.spring") version "2.4.10"
}

dependencies {
  implementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter:3.0.0")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-webclient")
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.1.0")

  // OpenAPI dependencies
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-api:3.1.0")
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.1.0")
  constraints {
    implementation("org.webjars:swagger-ui:5.32.11")
  }
  implementation("org.springdoc:springdoc-openapi-starter-common:3.1.0")
  constraints {
    implementation("org.webjars:swagger-ui:5.32.11")
  }

  testImplementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter-test:2.5.0")
  testImplementation("org.springframework.boot:spring-boot-starter-webflux-test")
  testImplementation("org.wiremock:wiremock-standalone:3.13.2")
  testImplementation("io.swagger.parser.v3:swagger-parser:2.1.47") {
    exclude(group = "io.swagger.core.v3")
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

// ---------------------------------------------------------------------------
// OpenAPI code generation
// ---------------------------------------------------------------------------

val configValues = mapOf(
  "dateLibrary" to "java8-localdatetime",
  "serializationLibrary" to "jackson",
  "enumPropertyNaming" to "original",
)

val buildDirectory: Directory = layout.buildDirectory.get()

tasks.register("buildBtPinPhoneApiModel", GenerateTask::class) {
  generatorName.set("kotlin")
  inputSpec.set("openapi-specs/bt-pin-phone-api.json")
  outputDir.set("$buildDirectory/generated/btpinphoneapi")
  modelPackage.set("uk.gov.justice.digital.hmpps.digitalcanteenapi.client.btPinPhoneClient.generated")
  configOptions.set(configValues)
  globalProperties.set(mapOf("models" to ""))
}

tasks.register("buildPurchasePinPhoneUiApiModel", GenerateTask::class) {
  generatorName.set("kotlin")
  inputSpec.set("openapi-specs/purchase-pin-phone-ui-api.json")
  outputDir.set("$buildDirectory/generated/purchasePinPhoneUiApi")
  modelPackage.set("uk.gov.justice.digital.hmpps.digitalcanteenapi.client.medusaclient.generated")
  configOptions.set(configValues)
  globalProperties.set(mapOf("models" to ""))
}

tasks.register("buildPrisonApiModel", GenerateTask::class) {
  generatorName.set("kotlin")
  inputSpec.set("openapi-specs/prison-api.json")
  outputDir.set("$buildDirectory/generated/prisonapi")
  modelPackage.set("uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonfinance.generated")
  configOptions.set(configValues)
  globalProperties.set(mapOf("models" to ""))
}

tasks.register("buildPrisonerSearchApiModel", GenerateTask::class) {
  generatorName.set("kotlin")
  inputSpec.set("openapi-specs/prisoner-search-api.json")
  outputDir.set("$buildDirectory/generated/prisonersearchapi")
  modelPackage.set("uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisonerSearch.generated")
  configOptions.set(configValues)
  globalProperties.set(mapOf("models" to ""))
}

tasks.register("buildPrisonerAdjudicationsApiModel", GenerateTask::class) {
  generatorName.set("kotlin")
  inputSpec.set("openapi-specs/manage-adjudications-api.json")
  outputDir.set("$buildDirectory/generated/prisoneradjudicationsapi")
  modelPackage.set("uk.gov.justice.digital.hmpps.digitalcanteenapi.client.prisoneradjudications.generated")
  configOptions.set(configValues)
  globalProperties.set(mapOf("models" to ""))
  skipValidateSpec.set(true)
}

val generatedProjectDirs = listOf("btpinphoneapi", "purchasePinPhoneUiApi", "prisonapi", "prisonersearchapi", "prisoneradjudicationsapi")

kotlin {
  generatedProjectDirs.forEach { generatedProject ->
    sourceSets["main"].apply {
      kotlin.srcDir("$buildDirectory/generated/$generatedProject/src/main/kotlin")
    }
  }
}

tasks {
  withType<KotlinCompile> {
    dependsOn("buildBtPinPhoneApiModel", "buildPurchasePinPhoneUiApiModel", "buildPrisonApiModel", "buildPrisonerSearchApiModel", "buildPrisonerAdjudicationsApiModel")
    compilerOptions.jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_25
  }
}

tasks.named("runKtlintCheckOverMainSourceSet") {
  dependsOn("buildBtPinPhoneApiModel", "buildPurchasePinPhoneUiApiModel", "buildPrisonApiModel", "buildPrisonerSearchApiModel", "buildPrisonerAdjudicationsApiModel")
}

configure<KtlintExtension> {
  filter {
    generatedProjectDirs.forEach { generatedProject ->
      exclude { element ->
        element.file.path.contains("build/generated/$generatedProject/src/main/")
      }
    }
  }
}
