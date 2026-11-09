Have you ever thought it is a hassle to re-install your mod just to change a few magical values? Or have you ever thought that it would be nice if you could monitor the changes happening from your math sorceries?

Well... Then say no more!!

Myriad is a Mindustry java library that aims to be the do it all mod development support, providing the most convenient development QoL features such as runtime data monitoring and experimentation tools. To get started with the basics, you can check out the following guides right here:

- [Getting Started]()
- [Documentation]()
- [Debug Tool]()

---
These library is composed of multiple modules:

- **Acari** - The core module for the runtime tool engine. Requires installation of Myriad:Acari mod to fully utilize.
- **Papilio** - supplementary module for mindustry ui, a direct extension of Arc Scene component library.

## Project layout

```
Myriad/
├── settings.gradle.kts        # includes :myriad-acari and :papilio
├── build.gradle.kts           # shared config (repos, Java 17, Arc/Mindustry, publishing)
├── gradle.properties          # group, version, Mindustry/Arc/Android versions
├── myriad-acari/                     # library + installable mod
│   ├── build.gradle.kts       # mod.hjson generation, deploy/dex, install/runClient
│   ├── assets/                # mod assets (icon.png, ...)
│   └── src/main/java/acari/
└── myriad-papilio/                   # UI library
    ├── build.gradle.kts
    └── src/main/java/papilio/
```

## Building

```
./gradlew :myriad-acari:dex          # build/libs/MyriadAcari.jar (desktop + Android)
./gradlew :myriad-acari:deploy       # desktop-only jar
./gradlew :myriad-acari:install      # copy into your Mindustry mods folder
./gradlew :myriad-acari:runClient    # install and launch Mindustry
./gradlew publishToMavenLocal # publish both libraries locally
```

