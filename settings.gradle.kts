// NexClip - settings.gradle.kts
// 编号60：供应链安全 - 依赖版本锁定

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven("https://maven.arthenica.onl/repository/maven-releases/")
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://maven.arthenica.onl/repository/maven-releases/")
    }
}

rootProject.name = "NexClip"
include(":app")
include(":core:ai")
include(":core:common")
include(":core:export")
include(":core:performance")
include(":core:security")
include(":core:video")
include(":core:vision")
include(":feature:effects")
include(":feature:player")
include(":feature:project")
include(":feature:subtitle")
include(":feature:tracking")
