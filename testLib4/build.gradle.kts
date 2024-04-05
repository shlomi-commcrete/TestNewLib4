import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
}

val buildConfigProperties: Properties by extra { Properties() }
buildConfigProperties.load(FileInputStream(project.file("buildconfig.default.properties")))
println("file Load " + project.path)

if (project.file("buildconfig.local.properties").exists()) {
    println("buildconfig.local.properties Create")
    buildConfigProperties.load(FileInputStream(project.file("buildconfig.local.properties")))
}
val ABI_FILTERS: List<String> by extra(listOf("arm64-v8a", "x86_64", "armeabi-v7a", "x86"))

println("ABI_FILTERS " + buildConfigProperties["abi.filters"])
println("repo.dir " + buildConfigProperties["repo.dir"])

val localProperties = Properties().apply {
    load(project.rootProject.file("local.properties").inputStream())
}

val cmakeDir = localProperties.getProperty("cmake.dir")
// Construct the path to the CMake binary
// Note: Adjust the relative path to the cmake executable as necessary based on your CMake version and OS
val cmakeExecutablePath = "$cmakeDir/bin/cmake"
println("cmake.dir " + "$cmakeDir/bin/cmake")



android {
    namespace = "com.commcrete.testlib4"
    compileSdk = 33

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

tasks.configureEach {
    if (name == "externalNativeBuildDebug") {
        dependsOn(compileCodec2)
    }
    if (name == "externalNativeBuildRelease") {
        dependsOn(compileCodec2)
    }
    if(name == "assembleDebug"){
        dependsOn(compileCodec2)
    }
    if(name == "assembleRelease"){
        dependsOn(compileCodec2)
    }
    if(name == "bundleReleaseAar"){
        dependsOn(compileCodec2)
    }
}

tasks.named("preBuild") {
    dependsOn(compileCodec2)
}

val compileCodec2 by tasks.registering {
    doFirst {
        System.out.println("android.ndkDirectory : " + android.ndkDirectory)
        project.file("build/codec2_build_linux").mkdirs()
        ABI_FILTERS.forEach() { abi ->
            System.out.println("Create abi " + abi)
            project.file("build/codec2_build_android_$abi").mkdirs()
            project.file("build/imported-lib/$abi").mkdirs()
        }

    }
    doLast {
        exec {
            workingDir = project.file("build/codec2_build_linux")
            commandLine(cmakeExecutablePath, "$projectDir/src/codec2")
        }
        exec {
            workingDir = project.file("build/codec2_build_linux")
            commandLine("/usr/bin/make")
        }

        ABI_FILTERS.forEach { abi ->
            println("Handle abi $abi")
            // Configure and generate the makefile for each ABI
            exec {
                workingDir = project.file("build/codec2_build_android_$abi")
                println("workingDir $workingDir")
                println("android.ndkDirectory ${android.ndkDirectory}")

                commandLine = listOf(
                    cmakeExecutablePath,
                    "$projectDir/src/codec2",
                    "-DCMAKE_TOOLCHAIN_FILE=${android.ndkDirectory}/build/cmake/android.toolchain.cmake",
                    "-DUNITTEST=FALSE",
                    "-DGENERATE_CODEBOOK=$projectDir/build/codec2_build_linux/src/generate_codebook",
                    "-DANDROID_NATIVE_API_LEVEL=23",
                    "-DANDROID_ABI=$abi",
                    "-DANDROID_STL=c++_shared"
                )
            }
//            // Build the library using the generated makefile
            exec {
                workingDir = project.file("build/codec2_build_android_$abi")
                commandLine = listOf(
                    cmakeExecutablePath,
                    "--build", "."
                )
            }
//            // Copy the generated library to the appropriate directory
            copy {
                from("$projectDir/build/codec2_build_android_$abi/src/libcodec2.so")
                into("$projectDir/build/imported-lib/$abi")
            }
        }
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}

afterEvaluate {


    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])

                groupId = "com.github.kenmaro3"
                artifactId = "my-library"
                version = "0.0.0"
            }
        }
    }
}