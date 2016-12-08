import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import me.champeau.gradle.JMHPlugin
import me.champeau.gradle.JMHPluginExtension
import net.minecraftforge.gradle.user.ReobfMappingType
import net.minecraftforge.gradle.user.ReobfTaskFactory
import net.minecraftforge.gradle.user.patcherUser.forge.ForgeExtension
import net.minecraftforge.gradle.user.patcherUser.forge.ForgePlugin
import nl.javadude.gradle.plugins.license.LicenseExtension
import nl.javadude.gradle.plugins.license.LicensePlugin
import org.ajoberstar.grgit.Grgit
import org.ajoberstar.grgit.operation.DescribeOp
import org.eclipse.jgit.errors.RepositoryNotFoundException
import org.gradle.api.JavaVersion
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Task
import org.gradle.api.internal.HasConvention
import org.gradle.api.plugins.BasePluginConvention
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.tasks.Jar
import org.gradle.language.jvm.tasks.ProcessResources
import org.gradle.plugins.ide.eclipse.EclipsePlugin
import org.gradle.plugins.ide.idea.IdeaPlugin
import org.gradle.plugins.ide.idea.model.IdeaModel
import org.gradle.script.lang.kotlin.*
import org.spongepowered.asm.gradle.plugins.MixinExtension
import org.spongepowered.asm.gradle.plugins.MixinGradlePlugin
import kotlin.apply
import kotlin.reflect.KFunction1

// Gradle repositories and dependencies
buildscript {
    repositories {
        mavenCentral()
        jcenter()
        maven {
            setUrl("http://files.minecraftforge.net/maven")
        }
        maven {
            setUrl("http://repo.spongepowered.org/maven")
        }
        maven {
            setUrl("https://plugins.gradle.org/m2/")
        }
    }
    dependencies {
        classpath("net.minecraftforge.gradle:ForgeGradle:2.2-SNAPSHOT")
        classpath("org.ajoberstar:grgit:1.4.+")
        classpath("org.spongepowered:mixingradle:0.4-SNAPSHOT")
        classpath("com.github.jengelman.gradle.plugins:shadow:1.2.3")
        classpath("gradle.plugin.nl.javadude.gradle.plugins:license-gradle-plugin:0.13.1")
        classpath("me.champeau.gradle:jmh-gradle-plugin:0.3.1")
    }
}

apply {
    plugin<ForgePlugin>()
    plugin<EclipsePlugin>()
    plugin<IdeaPlugin>()
    plugin<ShadowPlugin>()
    plugin<MixinGradlePlugin>()
    plugin<LicensePlugin>()
    plugin<JMHPlugin>()
}

//it can't be named forgeVersion because ForgeExtension has property named forgeVersion
val theForgeVersion = properties["forgeVersion"] as String
val licenseYear = properties["licenseYear"] as String
val projectName = properties["projectName"] as String

val sourceSets = the<JavaPluginConvention>().sourceSets
val mainSourceSet = sourceSets.getByName("main")
val minecraft = the<ForgeExtension>()

defaultTasks = listOf("licenseFormat", "build")

version = getModVersion()
group = "cubichunks"

configure<IdeaModel> {
    module.apply {
        inheritOutputDirs = true
    }
    module.isDownloadJavadoc = true
    module.isDownloadSources = true
}

configure<BasePluginConvention> {
    archivesBaseName = "CubicChunks"
}

configure<JavaPluginConvention> {
    setSourceCompatibility(JavaVersion.VERSION_1_8)
    setTargetCompatibility(JavaVersion.VERSION_1_8)
}

configure<MixinExtension> {
    add(mainSourceSet, "cubicchunks.mixins.refmap.json")
}

configure<ForgeExtension> {
    version = theForgeVersion
    runDir = "run"
    mappings = "stable_29"

    replace("@@VERSION@@", project.version)
    replaceIn("cubicchunks/CubicChunks.java")

    val args = listOf(
            "-Dfml.coreMods.load=cubicchunks.asm.CubicChunksCoreMod", //the core mod class, needed for mixins
            "-Dmixin.env.compatLevel=JAVA_8", //needed to use java 8 when using mixins
            "-Dmixin.debug.verbose=true", //verbose mixin output for easier debugging of mixins
            "-Dmixin.debug.export=true", //export classes from mixin to runDirectory/.mixin.out
            "-Dcubicchunks.debug=true", //various debug options of cubic chunks mod. Adds items that are not normally there!
            "-XX:-OmitStackTraceInFastThrow", //without this sometimes you end up with exception with empty stacktrace
            "-Dmixin.checks.interfaces=true", //check if all interface methods are overriden in mixin
            "-Dfml.noGrab=false", //change to disable Minecraft taking control over mouse
            "-ea" //enable assertions
    )

    clientJvmArgs.addAll(args)
    serverJvmArgs.addAll(args)
}

configure<LicenseExtension> {
    val ext = (this as HasConvention).convention.extraProperties
    ext["project"] = projectName
    ext["year"] = licenseYear
    exclude("**/*.info")
    exclude("**/package-info.java")
    exclude("**/*.json")
    exclude("**/*.xml")
    exclude("assets/*")
    exclude("cubicchunks/server/chunkio/async/forge/*") // Taken from forge
    header = file("HEADER.txt")
    ignoreFailures = false
    strictCheck = true
    mapping(mapOf("java" to "SLASHSTAR_STYLE"))
}

configure<NamedDomainObjectContainer<ReobfTaskFactory.ReobfTaskWrapper>> {
    create("shadowJar").apply {
        mappingType = ReobfMappingType.SEARGE
    }
}
get<Task>("build")() {
    dependsOn("reobfShadowJar")
}

configure<JMHPluginExtension> {
    iterations = 10
    benchmarkMode = listOf("thrpt")
    batchSize = 16
    timeOnIteration = "1000ms"
    fork = 1
    threads = 1
    timeUnit = "ms"
    verbosity = "NORMAL"
    warmup = "1000ms"
    warmupBatchSize = 16
    warmupForks = 1
    warmupIterations = 10
    profilers = listOf("perfasm")
    jmhVersion = "1.17.1"
}

repositories {
    mavenCentral()
    jcenter()
    maven {
        setUrl("https://oss.sonatype.org/content/groups/public/")
    }
    maven {
        setUrl("http://repo.spongepowered.org/maven")
    }
}

dependencies {
    val jmh = configurations.getByName("jmh")

    compile("com.flowpowered:flow-noise:1.0.1-SNAPSHOT")
    compile("org.mapdb:mapdb:3.0.0-RC2") {
        exclude(mapOf("module" to "guava"))
    }
    testCompile("junit:junit:4.11")
    testCompile("org.hamcrest:hamcrest-junit:2.0.0.0")
    testCompile("it.ozimov:java7-hamcrest-matchers:0.7.0")
    testCompile("org.mockito:mockito-core:2.1.0-RC.2")
    testCompile("org.spongepowered:launchwrappertestsuite:1.0-SNAPSHOT")

    compile("org.spongepowered:mixin:0.5.11-SNAPSHOT") {
        exclude(mapOf("module" to "launchwrapper"))
        exclude(mapOf("module" to "guava"))
        exclude(mapOf("module" to "gson"))
    }

    compile("com.carrotsearch:hppc:0.7.1")

    jmh("org.openjdk.jmh:jmh-generator-annprocess:1.17.1")
}

configurations.getByName("jmh").extendsFrom(configurations.compile)
configurations.getByName("jmh").extendsFrom(configurations.getByName("forgeGradleMc"))
configurations.getByName("jmh").extendsFrom(configurations.getByName("forgeGradleMcDeps"))
configurations.testCompile.extendsFrom(configurations.getByName("forgeGradleGradleStart"))

val jar = get<Jar>("jar")
jar {
    exclude("LICENSE.txt")
    manifest.attributes["FMLAT"] = "cubicchunks_at.cfg"
    manifest.attributes["FMLCorePlugin"] = "cubicchunks.asm.CubicChunksCoreMod"
    manifest.attributes["TweakClass"] = "org.spongepowered.asm.launch.MixinTweaker"
    manifest.attributes["TweakOrder"] = "0"
    manifest.attributes["ForceLoadAsMod"] = "true"
}

val shadowJar = get<ShadowJar>("shadowJar")
shadowJar {
    //MapDB stuff
    relocate("org.mapdb", "cubicchunks.org.mappdb")
    relocate("kotlin", "cubicchunks.org.mappdb.com.google")
    relocate("net.jcip", "cubicchunks.org.mappdb.net.jcip")
    relocate("org.eclipse.collections", "cubicchunks.org.mappdb.org.eclipse.collections")
    relocate("net.jpountz", "cubicchunks.org.mappdb.net.jpountz")

    //MapDB natives. Will it work?
    relocate("win32", "cubicchunks.org.mappdb.win32")
    relocate("linux", "cubicchunks.org.mappdb.linux")
    relocate("darwin", "cubicchunks.org.mappdb.darwin")

    relocate("com.flowpowered", "cubicchunks.com.flowpowered")
    /*
     Mixin shouldn"t be relocated. Mixin dependencies:
     org.spongepowered:mixin:0.5.5-SNAPSHOT
     +--- org.slf4j:slf4j-api:1.7.7
     +--- commons-codec:commons-codec:1.9
     +--- org.ow2.asm:asm-commons:5.0.3
     |    \--- org.ow2.asm:asm-tree:5.0.3
     |         \--- org.ow2.asm:asm:5.0.3
     +--- commons-io:commons-io:2.4
     \--- com.googlecode.jarjar:jarjar:1.1
     */
    classifier = ""
}

val test = get<Test>("test")
test {
    systemProperty("lwts.tweaker", "cubicchunks.tweaker.MixinTweakerServer")
}

val processResources = get<ProcessResources>("processResources")
processResources {
    // this will ensure that this task is redone when the versions change.
    inputs.property("version", project.version)
    inputs.property("mcversion", minecraft.version)

    // replace stuff in mcmod.info, nothing else
    from(sourceSets.getByName("main").resources.srcDirs) {
        include("mcmod.info")

        // replace version and mcversion
        expand(mapOf("version" to project.version, "mcversion" to minecraft.version))
    }

    // copy everything else, thats not the mcmod.info
    from(sourceSets.getByName("main").resources.srcDirs) {
        exclude("mcmod.info")
    }
}

fun getMcVersion(): String {
    if (minecraft.version == null) {
        return theForgeVersion.split("-")[0]
    }
    return minecraft.version
}

task("writeModVersion") {
    dependsOn("build")
    file("VERSION").writeText("VERSION=" + version)
}
//returns version string according to this: http://mcforge.readthedocs.org/en/latest/conventions/versioning/
//format: MCVERSION-MAJORMOD.MAJORAPI.MINOR.PATCH(-final/rcX/betaX)
//rcX and betaX are not implemented yet
fun getModVersion(): String {
    try {
        val git = Grgit.open()
        val describe = DescribeOp(git.repository).call()
        val branch = git.branch.current.name
        return getModVersion_do(describe, branch);
    } catch(ex: RepositoryNotFoundException) {
        logger.error("Git repository not found! Version will be incorrect!")
        return getModVersion_do("v9999.9999-9999-gffffff", "localbuild")
    }
}

fun getModVersion_do(describe: String, branch: String) : String {
    if (branch.startsWith("MC_")) {
        val branchMcVersion = branch.substring("MC_".length)
        if (branchMcVersion != getMcVersion()) {
            logger.warn("Branch version different than project MC version! MC version: " +
                    getMcVersion() + ", branch: " + branch + ", branch version: " + branchMcVersion)
        }
    }

    val versionSuffix = project.property("versionSuffix") as String
    val versionMinorFreeze = project.property("versionMinorFreeze") as String

    //branches "master" and "MC_something" are not appended to version sreing, everything else is
    //only builds from "master" and "MC_version" branches will actually use the correct versioning
    //but it allows to distinguish between builds from different branches even if version number is the same
    val branchSuffix = if (branch == "master" || branch.startsWith("MC_")) "" else ("-" + branch.replace("[^a-zA-Z0-9.-]", "_"))

    val baseVersionRegex = "v[0-9]+\\.[0-9]+"
    val unknownVersion = String.format("%s-UNKNOWN_VERSION%s%s", getMcVersion(), versionSuffix, branchSuffix)
    if (!describe.contains('-')) {
        //is it the "vX.Y" format?
        if (describe.matches(Regex(baseVersionRegex))) {
            return String.format("%s-%s.0.0%s%s", getMcVersion(), describe, versionSuffix, branchSuffix)
        }
        logger.error("Git describe information: \"$describe\" in unknown/incorrect format")
        return unknownVersion
    }
    //Describe format: vX.Y-build-hash
    val parts = describe.split("-")
    if (!parts[0].matches(Regex(baseVersionRegex))) {
        logger.error("Git describe information: \"$describe\" in unknown/incorrect format")
        return unknownVersion
    }
    if (!parts[1].matches(Regex("[0-9]+"))) {
        logger.error("Git describe information: \"$describe\" in unknown/incorrect format")
        return unknownVersion
    }
    val mcVersion = getMcVersion()
    val modAndApiVersion = parts[0].substring(1)
    //next we have commit-since-tag
    val commitSinceTag = Integer.parseInt(parts[1])

    val minorFreeze = if (versionMinorFreeze.isEmpty()) -1 else Integer.parseInt(versionMinorFreeze)

    val minor = if (minorFreeze < 0) commitSinceTag else minorFreeze
    val patch = if (minorFreeze < 0) 0 else (commitSinceTag - minorFreeze)

    val version = String.format("%s-%s.%d.%d%s%s", mcVersion, modAndApiVersion, minor, patch, versionSuffix, branchSuffix)
    return version
}

fun <T : Task> get(name: String): KFunction1<(T.() -> Unit), T> {
    @Suppress("UNCHECKED_CAST")
    return (tasks.getByName(name) as T)::apply
}