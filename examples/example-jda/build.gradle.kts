plugins {
    application
    id("com.github.johnrengelman.shadow")
}

application {
    mainClass.set("cloud.commandframework.examples.jda.ExampleBot")
}

repositories {
    jcenter()
}

dependencies {
    implementation(project(":cloud-jda"))
    implementation("net.dv8tion:JDA:4.2.0_212")
    implementation("org.slf4j:slf4j-simple:1.7.30")
}

tasks {
    build {
        dependsOn(shadowJar)
    }
}
