plugins {
    id("net.kyori.indra.publishing")
}

signing {
    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKey, signingPassword)
}

indra {
    github("Incendo", "cloud") {
        ci(true)
    }
    mitLicense()

    configurePublications {
        pom {
            developers {
                city()
                jmp()
            }
        }
    }
}
