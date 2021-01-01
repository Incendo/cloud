dependencies {
    implementation(project(":cloud-core"))
    /* Needs to be provided by the platform */
    compileOnly("com.mojang", "brigadier", Versions.brigadier)
}
