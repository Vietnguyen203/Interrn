// settings.gradle.kts
pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        // Nếu (hiếm) bạn cần plugin từ JitPack thì mới mở dòng dưới:
        // maven(url = "https://jitpack.io")
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // ✅ Kotlin DSL: dùng uri(...) hoặc maven("..."), KHÔNG phải 'url "..."'
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "FoodOrder"
include(":app")
