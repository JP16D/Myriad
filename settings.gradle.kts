if (JavaVersion.current() < JavaVersion.VERSION_17) {
    throw GradleException("JDK 17 is a required minimum version. Yours: ${System.getProperty("java.version")}")
}

rootProject.name = "Myriad"

include(":acari")
project(":acari").projectDir = file("myriad/acari")
include(":papilio")
project(":papilio").projectDir = file("myriad/papilio")
