plugins {
    id("dev.kikugie.loom-back-compat")
}

version = "${property("mod.version")}+${sc.current.version}"
base.archivesName = property("mod.id") as String

val requiredJava: JavaVersion = when {
    sc.current.parsed >= "26.1" -> JavaVersion.VERSION_25
    sc.current.parsed >= "1.20.5" -> JavaVersion.VERSION_21
    sc.current.parsed >= "1.18" -> JavaVersion.VERSION_17
    sc.current.parsed >= "1.17" -> JavaVersion.VERSION_16
    else -> JavaVersion.VERSION_1_8
}

dependencies {
    minecraft("com.mojang:minecraft:${sc.current.version}")

    // Mojang mappings are applied only where the Minecraft jar is obfuscated.
    // loom-back-compat switches to the no-remap Loom path for 26.1+.
    loomx.applyMojangMappings()

    // InvChat intentionally does not depend on Fabric API at the foundation layer.
    // Fabric Loader itself is version-agnostic and exposes ClientModInitializer.
    modImplementation("net.fabricmc:fabric-loader:${property("deps.fabric_loader")}")
}

loom {
    runConfigs.all {
        preferGradleTask = true
        generateRunConfig = true
        runDirectory = rootProject.file("run/${sc.current.version}")
    }
}

java {
    withSourcesJar()
    sourceCompatibility = requiredJava
    targetCompatibility = requiredJava

    toolchain {
        vendor = JvmVendorSpec.ADOPTIUM
        languageVersion = JavaLanguageVersion.of(requiredJava.majorVersion)
    }
}

tasks {
    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release.set(requiredJava.majorVersion.toInt())
    }

    processResources {
        val props = mapOf(
            "id" to property("mod.id").toString(),
            "name" to property("mod.name").toString(),
            "version" to project.version.toString(),
            "minecraft" to sc.current.version
        )

        props.forEach { (key, value) -> inputs.property(key, value) }
        filesMatching("fabric.mod.json") { expand(props) }
    }

    register<Copy>("buildAndCollect") {
        group = "build"
        description = "Builds this Minecraft target and collects its distributable jars."
        dependsOn(loomx.modJar, loomx.modSourcesJar)
        from(loomx.modJar.flatMap { it.archiveFile }, loomx.modSourcesJar.flatMap { it.archiveFile })
        into(rootProject.layout.buildDirectory.dir("libs/${property("mod.version")}"))
    }
}
