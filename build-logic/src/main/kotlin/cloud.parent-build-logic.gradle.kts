plugins {
    id("net.kyori.indra.publishing.sonatype")
}

System.getenv("SNAPSHOT_PUBLISHING_USERNAME")?.run {
    setProperty("incendoUsername", this)
}
System.getenv("SNAPSHOT_PUBLISHING_PASSWORD")?.run {
    setProperty("incendoPassword", this)
}
