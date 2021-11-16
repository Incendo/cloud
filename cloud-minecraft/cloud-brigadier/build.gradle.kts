dependencies {
    implementation(projects.cloudCore)
    /* Needs to be provided by the platform */
    compileOnly("com.mojang", "brigadier", Versions.brigadier)
    testImplementation("com.mojang", "brigadier", Versions.brigadier)
}
