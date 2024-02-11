import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions

val repositoryURL: String by project
val repositoryUser: String by project
val repositoryPassword: String by project
val kotlinVersion: String by project
val kotlinCoroutines: String by project
val logbackVersion: String by project
val kodeinVersion: String by project
val utilsKotlin: String by project


plugins {
	application
	kotlin("jvm") version "1.9.22"
	java
	`maven-publish`
}

val compileOptions: (KotlinJvmOptions.() -> Unit) = {
	jvmTarget = "17"
	allWarningsAsErrors = true
}

tasks.compileKotlin {
	kotlinOptions {
		compileOptions()
	}
}

tasks.compileTestKotlin {
	kotlinOptions {
		compileOptions()
	}
}

java {
	sourceCompatibility = JavaVersion.VERSION_17
	targetCompatibility = JavaVersion.VERSION_17
	withSourcesJar()
}

group = "br.dev.schirmer"
version = "13.1.0"

sourceSets.main {
	java {
		srcDirs("src")
	}
}

sourceSets.test {
	java {
		srcDirs("test")
	}
}

repositories {
	mavenCentral()

	maven {
		url = uri("$repositoryURL/repository/maven-releases/")
		isAllowInsecureProtocol = true
		credentials {
			username = repositoryUser
			password = repositoryPassword
		}
	}
}

dependencies {
	//kotlin-utils
	implementation("br.dev.schirmer:utils-kotlin:$utilsKotlin")

	//Coroutines
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutines")

	//Log
	implementation("ch.qos.logback:logback-classic:$logbackVersion")

	//Kodein
	implementation("org.kodein.di:kodein-di-jvm:$kodeinVersion")

	//Tests
	testImplementation("org.jetbrains.kotlin:kotlin-test:$kotlinVersion")

}

tasks.publish {
	dependsOn("jar")
}

publishing {
	publications {
		register("mavenJava", MavenPublication::class) {
			artifact("build/libs/${project.name}-$version.jar") {
				artifactId = project.name
				extension = "jar"
			}
			with(pom) {
				developers {
					developer {
						id.set("CSG")
						name.set("Cláudio Schirmer Guedes")
						email.set("claudioschirmer@icloud.com")
					}
				}
				withXml {
					val dependencies = asNode().appendNode("dependencies")
					configurations.implementation.get().allDependencies.forEach {
						val depNode = dependencies.appendNode("dependency")
						depNode.appendNode("groupId", it.group)
						depNode.appendNode("artifactId", it.name)
						depNode.appendNode("version", it.version)
					}
				}
			}
		}
	}

	repositories {
		maven {
			name = "nexus"
			url = uri("$repositoryURL/repository/maven-releases/")
			isAllowInsecureProtocol = true
			credentials {
				username = repositoryUser
				password = repositoryPassword
			}
		}
	}
}