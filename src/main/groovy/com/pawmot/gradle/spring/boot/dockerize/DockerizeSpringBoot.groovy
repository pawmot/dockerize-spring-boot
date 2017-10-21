package com.pawmot.gradle.spring.boot.dockerize

import org.gradle.api.Plugin
import org.gradle.api.Project

class DockerizeSpringBoot implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.extensions.create("docker", DockerizeSpringBootExtension)

        def task = project.task('dockerize')

        task.dependsOn = [project.tasks.build]

        task.doLast {
            if(!project.tasks.bootJar) {
                throw new RuntimeException("spring boot plugin not detected")
            }

            def jarFilePath = project.tasks.bootJar.getArchivePath().absoluteFile
            def workingDirectory = new File(project.buildDir.toString(), 'docker')

            project.copy {
                from jarFilePath
                into workingDirectory
                rename { String _ ->
                    "app.jar"
                }
            }

            project.copy {
                from new File(project.rootDir.toString(), 'src/docker/Dockerfile')
                into workingDirectory
            }

            project.exec {
                workingDir workingDirectory

                def fullImgName = "${project.docker.imageName}:${project.docker.tag}"
                def args = ['docker', 'build', '-t', fullImgName, workingDirectory]

                commandLine args
            }
        }
    }
}

class DockerizeSpringBootExtension {
    String imageName
    String tag = "latest"
}
