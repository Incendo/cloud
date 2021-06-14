import ca.stellardrift.build.configurate.ConfigFormats
import ca.stellardrift.build.configurate.catalog.PolyglotVersionCatalogExtension

enableFeaturePreview("VERSION_CATALOGS")

plugins {
    id("ca.stellardrift.polyglot-version-catalogs") version "5.0.0"
}

extensions.configure<PolyglotVersionCatalogExtension> {
    from(ConfigFormats.YAML, file("../gradle/libs.versions.yml"))
}
