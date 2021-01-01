import org.gradle.api.Project
import org.gradle.kotlin.dsl.invoke

fun Project.buildGroups(vararg groupNames: String) =
        groupNames.forEach(this::buildGroup)

fun Project.buildGroup(groupName: String) {
    tasks {
        register("build$groupName") {
            group = "cloud"
            rootProject.subprojects
                    .filter { it.projectDir.parentFile.name == "cloud-${groupName.toLowerCase()}" }
                    .map { it.tasks.getByName("build") }
                    .forEach { dependsOn(it) }
        }
        register("install$groupName") {
            group = "cloud"
            rootProject.subprojects
                    .filter { it.projectDir.parentFile.name == "cloud-${groupName.toLowerCase()}" }
                    .map { it.tasks.getByName("publishToMavenLocal") }
                    .forEach { dependsOn(it) }
        }
    }
}
