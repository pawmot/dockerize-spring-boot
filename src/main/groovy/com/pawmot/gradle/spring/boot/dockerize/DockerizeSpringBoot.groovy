package com.pawmot.gradle.spring.boot.dockerize

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

class DockerizeSpringBoot implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.extensions.create("docker", DockerizeSpringBootExtension)

        def task = project.task('dockerize')
        task.group = 'build'

        task.dependsOn = [project.tasks.build]

        task.doLast {
            def jarTask = findSpringBootVer1Or2JarTask(project)

            if(!jarTask) {
                throw new RuntimeException("spring boot plugin not detected")
            }

            if(!project.docker.imageName) {
                throw new RuntimeException("imageName property has to be set")
            }

            def jarFilePath = getJarPath(jarTask, project)
            def workingDirectory = new File(project.buildDir.toString(), 'docker')

            project.copy {
                from jarFilePath
                into workingDirectory
                rename { String _ ->
                    "app.jar"
                }
            }

            project.copy {
                from new File(project.projectDir.toString(), 'src/docker/Dockerfile')
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

    private static Task findSpringBootVer1Or2JarTask(Project project) {
        return project.tasks.findByName('bootJar') ?: project.tasks.findByName('bootRepackage')
    }

    private static Object getJarPath(Task t, Project project) {
        if(t.metaClass.respondsTo(t, "getArchivePath")) {
            return t.getArchivePath().absoluteFile
        } else {
            return project.tasks.findByName('jar').archivePath
        }
    }
}

class DockerizeSpringBootExtension {
    String imageName
    String tag = "latest"
}
