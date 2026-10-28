pluginManagement { repositories { gradlePluginPortal(); mavenCentral() } }
rootProject.name = "neton-http-ktor"
// Never silently consume sibling checkouts. CI uses published artifacts.
if (providers.gradleProperty("neton.local").orNull == "true") {
    require(!providers.gradleProperty("neton.repository").isPresent)
    includeBuild("../neton")
}
