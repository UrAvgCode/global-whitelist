pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven(uri("https://repo.papermc.io/repository/maven-public/"))
        maven("https://repo.opencollab.dev/main/")
    }
}

rootProject.name = "global-whitelist"
