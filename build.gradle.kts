plugins {
    java
    alias(libs.plugins.run.velocity)
}

group = "com.uravgcode"
version = "1.2.1"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

dependencies {
    compileOnly(libs.velocity.api)
    compileOnly(libs.floodgate.api)
    annotationProcessor(libs.velocity.api)
}

tasks {
    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release.set(25)
    }

    runVelocity {
        velocityVersion("4.1.2-SNAPSHOT")
    }
}

val templateSource = file("src/main/templates")
val templateDest = layout.buildDirectory.dir("generated/sources/templates")

val generateTemplates = tasks.register<Copy>("generateTemplates") {
    val props = mapOf("version" to project.version)
    inputs.properties(props)

    from(templateSource)
    into(templateDest)
    expand(props)
}

sourceSets {
    named("main") {
        java.srcDir(generateTemplates.map { it.outputs })
    }
}
