import java.util.Properties

// Acari: runtime tool engine. Published as a library AND shipped as an installable mod.
// - `jar`      plain library jar (what gets published)
// - `deploy`   desktop mod jar (classes + assets + generated mod.hjson)
// - `dex`      final cross-platform mod jar (desktop + Android dex)

/* ------------------------------------------------------------------------------------------------
 * Mod jars
 * ---------------------------------------------------------------------------------------------- */

val projectName = rootProject.name.replace(Regex("[^a-zA-Z0-9]"), "")
val sdkVersion = project.property("sdkVersion").toString()
val sdkAPI = project.property("sdkAPI").toString()
val mindustryVersion = project.property("mindustryVersion").toString()

val isWindows = System.getProperty("os.name").lowercase().contains("windows")

val sdkRoot: String? = System.getenv("ANDROID_HOME")
    ?: System.getenv("ANDROID_SDK_ROOT")
    ?: rootProject.file("local.properties").takeIf { it.exists() }?.let { f ->
        Properties().apply { f.inputStream().use(::load) }.getProperty("sdk.dir")
    }

val deploy = tasks.register<Jar>("deploy") {
    group = "myriad"
    description = "Assembles the desktop mod jar."

    archiveFileName = "${projectName}Desktop.jar"
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    from(sourceSets.main.map { it.output })
    from({ configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) } })
    from("assets")
    from(tasks.named("hjson"))
}

val dex = tasks.register<Jar>("dex") {
    group = "myriad"
    description = "Assembles the final mod jar: desktop classes plus Android dex."

    archiveFileName = "$projectName.jar"
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    val deployJar = deploy.flatMap { it.archiveFile }
    val dexedJar = temporaryDir.resolve("Dexed.jar")
    val compileClasspath = configurations.compileClasspath
    val runtimeClasspath = configurations.runtimeClasspath

    dependsOn(deploy)
    inputs.file(deployJar)

    doFirst {
        val sdk = sdkRoot?.let(::File)?.takeIf { it.exists() }
            ?: throw GradleException("No Android SDK found. Set ANDROID_HOME or sdk.dir in local.properties.")

        val platforms = File(sdk, "platforms")
        val androidJar = File(platforms, "android-$sdkVersion/android.jar").takeIf { it.exists() }
            ?: platforms.listFiles().orEmpty().sortedDescending()
                .map { File(it, "android.jar") }.firstOrNull { it.exists() }
            ?: throw GradleException("No android.jar found. Install an Android platform in $platforms.")

        val d8Name = if (isWindows) "d8.bat" else "d8"
        val d8 = File(sdk, "build-tools").listFiles().orEmpty().sortedDescending()
            .map { File(it, d8Name) }.firstOrNull { it.exists() }?.absolutePath
            ?: d8Name // fall back to PATH

        val cmd = mutableListOf(d8, "--min-api", sdkAPI, "--output", dexedJar.path, deployJar.get().asFile.path, "--lib", androidJar.path)
        (compileClasspath.get().files + runtimeClasspath.get().files).forEach { cmd += listOf("--classpath", it.path) }
        if (isWindows) cmd.addAll(0, listOf("cmd", "/c"))

        dexedJar.parentFile.mkdirs()
        val exit = ProcessBuilder(cmd).directory(temporaryDir).inheritIO().start().waitFor()
        if (exit != 0) throw GradleException("d8 failed with code $exit")
    }

    from(zipTree(deployJar))
    from({ zipTree(dexedJar) })
}

/* ------------------------------------------------------------------------------------------------
 * Local testing: copy the mod into a Mindustry install and launch it
 * ---------------------------------------------------------------------------------------------- */

val mindustryDir: Provider<File> = providers.gradleProperty("mindustryPath")
    .map(::File)
    .orElse(rootProject.layout.projectDirectory.dir("run").asFile)

val isSteam = mindustryDir.map { File(it, "Mindustry.exe").exists() }

tasks.register<Copy>("install") {
    group = "myriad"
    description = "Copies the mod jar into the Mindustry mods folder."

    from(dex)
    into(mindustryDir.zip(isSteam) { dir, steam -> File(dir, if (steam) "saves/mods" else "mods") })
}

val installClient = tasks.register("installClient") {
    group = "myriad"
    description = "Downloads the Mindustry client $mindustryVersion into the run directory if needed."

    val target = mindustryDir.zip(isSteam) { dir, steam ->
        if (steam) File(dir, "Mindustry.exe") else File(dir, "client-$mindustryVersion.jar")
    }
    val steam = isSteam
    outputs.file(target)

    doLast {
        val file = target.get()
        if (steam.get() || (file.exists() && file.length() > 0)) return@doLast

        file.parentFile.mkdirs()
        logger.lifecycle("> Downloading Mindustry $mindustryVersion...")
        uri("https://github.com/Anuken/Mindustry/releases/download/$mindustryVersion/Mindustry.jar")
            .toURL().openStream().use { input -> file.outputStream().use(input::copyTo) }
        logger.lifecycle("Installed to '${file.absolutePath}'.")
    }
}

tasks.register("runClient") {
    group = "myriad"
    description = "Installs the mod and starts Mindustry with it."
    dependsOn(installClient, "install")

    val dir = mindustryDir
    val steam = isSteam
    doLast {
        val root = dir.get()
        val client = if (steam.get()) File(root, "Mindustry.exe") else File(root, "client-$mindustryVersion.jar")
        val saves = if (steam.get()) File(root, "saves") else root

        logger.lifecycle("> Starting Mindustry.")
        val cmd = if (steam.get()) listOf(client.path) else listOf("java", "-jar", client.path, "-debug")
        val pb = ProcessBuilder(cmd).directory(saves).inheritIO()
        pb.environment()["MINDUSTRY_DATA_DIR"] = saves.path
        pb.environment()["DEVELOPMENT"] = "true"
        pb.start().waitFor()
    }
}
