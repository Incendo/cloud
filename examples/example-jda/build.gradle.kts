plugins {
    application
    id("cloud.base-conventions")
    id("cloud.example-conventions")
    alias(libs.plugins.shadow)
}

application {
    mainClass.set("cloud.commandframework.examples.jda.ExampleBot")
}

dependencies {
    implementation(project(":cloud-jda"))
    implementation("net.dv8tion:JDA:4.2.1_257")
    implementation("org.slf4j:slf4j-simple:1.7.30")
}

tasks {
    assemble {
        dependsOn(shadowJar)
    }
}
