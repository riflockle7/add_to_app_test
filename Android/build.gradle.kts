import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import groovy.xml.XmlSlurper
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions

buildscript {

    repositories {
        google()
        gradlePluginPortal()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:8.1.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.21")
    }

    subprojects {
        project.configurations.all {
            resolutionStrategy.eachDependency {
                if (requested.group == "androidx.appcompat" && !requested.name.contains("androidx") ) {
                    useVersion("1.6.1")
                }
                if (requested.group == "androidx.activity" && !requested.name.contains("androidx") ) {
                    useVersion("1.7.0")
                }
                if (requested.group == "androidx.fragment" && !requested.name.contains("androidx") ) {
                    useVersion("1.5.6")
                }
            }
        }
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
    }
}

subprojects {
    afterEvaluate {
        if (project.hasProperty("android")) {
            project.extensions.findByType(LibraryExtension::class)?.let {
                setSubProjectForAGP8(it)
            }
        }
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}

tasks.withType<AbstractCompile> {
    inputs.dir("$rootDir/buildSrc/src/main/java")
}

fun setSubProjectForAGP8(androidExtension: BaseExtension) {
    println("package's namespace: ${androidExtension.namespace}")
    if (androidExtension.namespace == null) {
        val manifest =
            XmlSlurper().parse(project.file(androidExtension.sourceSets.getByName("main").manifest.srcFile))
        val packageName = manifest.getProperty("@package").toString()
        println("Set android namespace to $packageName!")
        androidExtension.namespace = packageName
    }

    androidExtension.setCompileSdkVersion(33)
    androidExtension.defaultConfig.setTargetSdkVersion(33)
    androidExtension.compileOptions.setSourceCompatibility(JavaVersion.VERSION_17)
    androidExtension.compileOptions.setTargetCompatibility(JavaVersion.VERSION_17)
    (androidExtension as? ExtensionAware)?.extensions?.findByType(KotlinJvmOptions::class.java)
        ?.let {
            it.jvmTarget = "${JavaVersion.VERSION_17}"
            println("Set ${androidExtension.namespace}'s Java Version to ${it.jvmTarget}")
        }
}