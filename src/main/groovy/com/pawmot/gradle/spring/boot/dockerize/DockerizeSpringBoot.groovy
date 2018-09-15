package com.pawmot.gradle.spring.boot.dockerize

import com.spotify.docker.client.DefaultDockerClient
import com.spotify.docker.client.ProgressHandler
import com.spotify.docker.client.exceptions.DockerException
import com.spotify.docker.client.messages.ProgressMessage
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

import java.util.concurrent.atomic.AtomicReference

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

            def fullImgName = "${project.docker.imageName}:${project.docker.tag}"
            def docker = new DefaultDockerClient("unix:///var/run/docker.sock")
            def imageIdRef = new AtomicReference<String>()
            def errorRef = new AtomicReference<String>()
            docker.build(workingDirectory.toPath(), fullImgName, new ProgressHandler() {
                @Override
                void progress(ProgressMessage message) throws DockerException {
                    if (message.error()) {
                       println(message.error())
                    }

                    def imageId = message.buildImageId()
                    if (imageId != null) {
                        imageIdRef.set(imageId)
                    }

                    if (message.error() != null) {
                        errorRef.set(message.error())
                    }
                }
            })

            while (imageIdRef.get() == null && errorRef.get() == null) {
                sleep(100)
            }

            if (imageIdRef.get() != null) {
                println("Created image $fullImgName with id ${imageIdRef.get()}.")
            } else if (errorRef.get() != null) {
                println("Image creation failed with following error: ${errorRef.get()}.")
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
