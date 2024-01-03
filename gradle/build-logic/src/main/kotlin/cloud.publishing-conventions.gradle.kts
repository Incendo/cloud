plugins {
    id("net.kyori.indra.publishing")
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

    signWithKeyFromProperties("signingKey", "signingPassword")
}
