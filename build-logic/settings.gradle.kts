import ca.stellardrift.build.configurate.ConfigFormats
import ca.stellardrift.build.configurate.catalog.PolyglotVersionCatalogExtension

plugins {
    id("ca.stellardrift.polyglot-version-catalogs") version "6.0.1"
}

extensions.configure<PolyglotVersionCatalogExtension> {
    from(ConfigFormats.YAML, file("../gradle/libs.versions.yml"))
}
