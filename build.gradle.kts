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

val clothConfigVersion = when {
    sc.current.parsed >= "26.2" -> "26.2.155"
    sc.current.parsed >= "26.1" -> "26.1.154"
    sc.current.parsed >= "1.21.11" -> "21.11.153"
    sc.current.parsed >= "1.21.9" -> "20.0.149"
    sc.current.parsed >= "1.21.6" -> "19.0.147"
    sc.current.parsed >= "1.21.5" -> "18.0.145"
    sc.current.parsed >= "1.21.4" -> "17.0.144"
    sc.current.parsed >= "1.21.2" -> "16.0.141"
    sc.current.parsed >= "1.21" -> "15.0.140"
    sc.current.parsed >= "1.20.5" -> "14.0.139"
    sc.current.parsed >= "1.20.3" -> "13.0.138"
    sc.current.parsed >= "1.20.2" -> "12.0.137"
    sc.current.parsed >= "1.20" -> "11.1.136"
    sc.current.parsed >= "1.19.4" -> "10.1.135"
    sc.current.parsed >= "1.19.3" -> "9.0.94"
    sc.current.parsed >= "1.19" -> "8.3.103"
    sc.current.parsed >= "1.18" -> "6.5.102"
    sc.current.parsed >= "1.17" -> "5.3.63"
    else -> "4.17.101"
}

val fabricApiVersion = when (sc.current.version) {
    "1.17" -> "0.35.2+1.17"
    "1.18" -> "0.44.0+1.18"
    "1.18.2" -> "0.47.10+1.18.2"
    "1.19" -> "0.56.3+1.19"
    "1.20.1" -> "0.92.2+1.20.1"
    else -> null
}

val clothConfigModId = if (sc.current.parsed < "1.18") "cloth-config2" else "cloth-config"

repositories {
    maven("https://maven.shedaniel.me/")
}

dependencies {
    minecraft("com.mojang:minecraft:${sc.current.version}")

    // Mojang mappings are applied only where the Minecraft jar is obfuscated.
    // loom-back-compat switches to the no-remap Loom path for 26.1+.
    loomx.applyMojangMappings()

    // InvChat intentionally does not depend on Fabric API at the foundation layer.
    // Fabric Loader itself is version-agnostic and exposes ClientModInitializer.
    modImplementation("net.fabricmc:fabric-loader:${property("deps.fabric_loader")}")
    
    modApi("me.shedaniel.cloth:cloth-config-fabric:$clothConfigVersion") {
    exclude(group = "net.fabricmc", module = "fabric-loader")

    // Some Cloth Config releases depend on a Fabric API build for a
    // neighboring Minecraft patch version.
    if (fabricApiVersion != null) {
            exclude(group = "net.fabricmc.fabric-api")
        }
    }

    if (fabricApiVersion != null) {
        modImplementation(
            "net.fabricmc.fabric-api:fabric-api:$fabricApiVersion"
        )
    }
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
            "minecraft" to sc.current.version,
            "cloth_config_id" to clothConfigModId
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