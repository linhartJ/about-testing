import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
    kotlin("jvm") version "1.4.31"
}

group = "org.jlinhart"

val junitVersion = "5.7.1"
val kotlinVersion = "1.4.31"
val mockitoVersion = "3.9.0"
val mockitoKotlinVersion = "2.2.0"
val kotlinTestVersion = "1.4.31"

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")

    // needed for all the basic junit stuff - like @Test annotation
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    // needed for test execution - without this you will not be able to execute the test
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    // needed for parametrized tests
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
    // nice library providing you with some mocking options and annotations for testing units with dependencies
    testImplementation("org.mockito:mockito-core:$mockitoVersion")
    // this contains MockitoExtension - mockito needs it in jUnit 5 (jupiter) environment
    testImplementation("org.mockito:mockito-junit-jupiter:$mockitoVersion")
    // need to make some mockito stuff kotlin-friendly
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:$mockitoKotlinVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test:$kotlinTestVersion")
}

repositories {
    mavenCentral()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

tasks.withType<Test> {
    useJUnitPlatform()
}
