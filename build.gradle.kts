import me.modmuss50.mpp.ReleaseType

plugins {
    id("dev.kikugie.loom-back-compat")
    id("me.modmuss50.mod-publish-plugin")
}

val modId = project.property("mod.id").toString()
val modName = project.property("mod.name").toString()
val modVersion = project.property("mod.version").toString()

version = "$modVersion+${sc.current.version}"
base.archivesName = modId

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

        // JDK 8 does not support javac's --release flag. For the 1.16.5
        // target, sourceCompatibility/targetCompatibility already enforce Java 8.
        if (requiredJava != JavaVersion.VERSION_1_8) {
            options.release.set(requiredJava.majorVersion.toInt())
        }
    }

    processResources {
        val props = mapOf(
            "id" to modId,
            "name" to modName,
            "version" to project.version.toString(),
            "minecraft" to sc.current.version
        )

        props.forEach { (key, value) -> inputs.property(key, value) }
        filesMatching("fabric.mod.json") { expand(props) }

        val mixinJava = "JAVA_${requiredJava.majorVersion}"
        inputs.property("mixinJava", mixinJava)
        filesMatching("invchat.mixins.json") { expand("java" to mixinJava) }
    }

    register<Copy>("buildAndCollect") {
        group = "build"
        description = "Builds this Minecraft target and collects its distributable jars."
        dependsOn(loomx.modJar, loomx.modSourcesJar)
        from(loomx.modJar.flatMap { it.archiveFile }, loomx.modSourcesJar.flatMap { it.archiveFile })
        into(rootProject.layout.buildDirectory.dir("libs/$modVersion"))
    }
}

publishMods {
    val modVersion = project.property("mod.version").toString()
    val minecraftVersion = sc.current.version

    file.set(loomx.modJar.flatMap { it.archiveFile })

    version.set("$modVersion+$minecraftVersion")
    displayName.set("InvChat $modVersion for Minecraft $minecraftVersion")

    changelog.set(
        providers.environmentVariable("RELEASE_CHANGELOG")
            .orElse("InvChat $modVersion")
    )

    type.set(ReleaseType.STABLE)

    modLoaders.add("fabric")

    modrinth {
        accessToken.set(
            providers.environmentVariable("MODRINTH_TOKEN")
        )

        projectId.set("6wyX37re")

        minecraftVersions.add(minecraftVersion)
    }
}