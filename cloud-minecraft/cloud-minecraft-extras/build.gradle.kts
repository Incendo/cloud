plugins {
    id("cloud.base-conventions")
    id("cloud.publishing-conventions")
}

dependencies {
    api(projects.cloudCore)
    api(libs.adventureApi)
    api(libs.adventureTextSerializerPlain)
}
