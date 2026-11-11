Have you ever thought it is a hassle to re-install your mod just to change a few magical values? Or have you ever thought that it would be nice if you could monitor the changes happening from your math sorceries?

Well... Then say no more!!

Myriad is a Mindustry java library that aims to be the do it all mod development support, providing the most convenient development QoL features such as runtime data monitoring and experimentation tools. To get started with the basics, you can check out the following guides right here:

- [Getting Started]()
- [Documentation]()
- [Debug Tool]()

## Project layout

These library is composed of multiple modules:

- **Acari** - The core module for the runtime tool engine. Requires installation of [Myriad Acari](./modules/acari/) mod to fully utilize.
- **Papilio** - An extension module for Arc ui and graphics.

---
```
Myriad/
├── settings.gradle.kts        
├── build.gradle.kts           
├── gradle.properties          
├── modules/acari/              # mindustry modding tools
│   ├── build.gradle.kts       
│   ├── assets/                
│   └── src/acari/
└── modules/papilio/            # arc ui extension library
    ├── build.gradle.kts
    └── src/papilio/
```

## Building

```
./gradlew :acari:dex          # desktop + android jar (needs Android SDK)
./gradlew :acari:deploy       # desktop-only jar
./gradlew :acari:install      # copy into your Mindustry mods folder
./gradlew :acari:runClient    # install and launch Mindustry
./gradlew build               # standard build (compile, test, jar + deploy all)
```
