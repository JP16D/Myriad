if (JavaVersion.current() < JavaVersion.VERSION_17) {
    throw GradleException("JDK 17 is a required minimum version. Yours: ${System.getProperty("java.version")}")
}

rootProject.name = "Myriad"

include(":acari")
project(":acari").projectDir = file("modules/acari")
include(":papilio")
project(":papilio").projectDir = file("modules/papilio")
