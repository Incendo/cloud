plugins {
    id("cloud.base-conventions")
}

dependencies {
    api(projects.cloudCore)
    api(libs.adventureApi)
    api(libs.adventureTextSerializerPlain)
}
