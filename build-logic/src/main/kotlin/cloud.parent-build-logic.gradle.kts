if (project.hasProperty("releasePublishing")) {
    plugins.apply("net.kyori.indra.publishing.sonatype")
}
