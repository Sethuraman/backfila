apply(plugin = "kotlin")

dependencies {
  implementation(Dependencies.awsDynamodb)
  implementation(Dependencies.kotlinReflection)
  implementation(Dependencies.kotlinStdLib)
  implementation(Dependencies.guava)
  implementation(Dependencies.guice)
  implementation(Dependencies.moshiCore)
  implementation(Dependencies.moshiKotlin)
  implementation(Dependencies.okio)
  implementation(Dependencies.retrofit)
  implementation(Dependencies.retrofitMock)
  implementation(Dependencies.retrofitMoshi)
  implementation(Dependencies.retrofitWire)
  implementation(Dependencies.wireMoshiAdapter)
  implementation(Dependencies.wireRuntime)

  api(project(":client"))
  // We do not want to leak client-base implementation details to customers.
  implementation(project(":client-base"))

  testImplementation(Dependencies.assertj)
  testImplementation(Dependencies.awsDynamodb)
  testImplementation(Dependencies.junitEngine)
  testImplementation(Dependencies.kotlinTest)

  testImplementation(project(":backfila-embedded"))
  testImplementation(project(":client-testing"))

  // ****************************************
  // For TESTING purposes only. We only want Misk for easy testing.
  // DO NOT turn these into regular dependencies.
  // ****************************************
  testImplementation(Dependencies.misk)
  testImplementation(Dependencies.miskAwsDynamodb)
  testImplementation(Dependencies.miskAwsDynamodbTesting)
  testImplementation(Dependencies.miskInject)
  testImplementation(Dependencies.miskTesting)
  testImplementation(project(":client-misk"))
  // Required until DynamoDBLocal is built with antlr >4.11 which wisp-config pulls in
  testImplementation("org.antlr:antlr4-runtime:4.7.2") {
    setForce(true)
  }
}

val jar by tasks.getting(Jar::class) {
  archiveBaseName.set("backfila-client-dynamodb")
}

if (rootProject.file("hooks.gradle").exists()) {
  apply(from = rootProject.file("hooks.gradle"))
}

apply(from = "$rootDir/gradle-mvn-publish.gradle")
