val smithyVersion: String by project

buildscript {
    repositories { mavenLocal() }

    val codegenVersion: String by project
    dependencies {
        classpath("software.amazon.smithy.rust.codegen.server.smithy:codegen-server:$codegenVersion")
        classpath("software.amazon.smithy.rust.codegen:codegen-client:$codegenVersion")
    }
}

plugins { id("software.amazon.smithy") }

dependencies {
    implementation("software.amazon.smithy:smithy-aws-traits:$smithyVersion")
    implementation("software.amazon.smithy:smithy-model:$smithyVersion")
    implementation("software.amazon.smithy:smithy-validation-model:$smithyVersion")
}

smithy { outputDirectory = buildDir.resolve("codegen") }

tasks {
    val srcDir = projectDir.resolve("../")
    val serverSdkCrateName: String by project
    println("#### Executing Server codegen ####")
    val copyServerCrate =
            create<Copy>("copyServerCrate") {
                from("$buildDir/codegen/$serverSdkCrateName/rust-server-codegen")
                into("$srcDir/$serverSdkCrateName")
            }
    println("#### Finished Server codegen to $srcDir/$serverSdkCrateName ####")
    println("----------------------------------------")
    println("#### Executing Client codegen ####")

    val clientSdkCrateName: String by project
    val copyClientCrate =
            create<Copy>("copyClientCrate") {
                from("$buildDir/codegen/$clientSdkCrateName/rust-client-codegen")
                into("$srcDir/$clientSdkCrateName")
            }

    val generateWorkspace = create<Task>("generateWorkspace")

    println("#### Finished Client codegen to $srcDir/$clientSdkCrateName ####")
    getByName("assemble").dependsOn("smithyBuildJar")
    getByName("assemble").finalizedBy(copyServerCrate, copyClientCrate, generateWorkspace)
    val tasks = getByName("assemble").finalizedBy.getDependencies(getByName("assemble"))
    val didWork = getByName("assemble").didWork
    println("#### Task assemble is finalized by $tasks and it worked? $didWork ")
}
