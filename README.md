# Dockerize Spring Boot
## Gradle plugin

To use this plugin apply it in the gradle project/module that uses Spring Boot:

```
buildscript {
    repositories {
        mavenCentral()
        maven { url "https://plugins.gradle.org/m2/" } 
    }
    dependencies {
        classpath "org.springframework.boot:spring-boot-gradle-plugin:$spring_boot_version"
        classpath 'gradle.plugin.com.pawmot:dockerize-spring-boot:0.1'
    }
}

apply plugin: 'org.springframework.boot'
apply plugin: 'com.pawmot.dockerize-spring-boot'

docker {
    imageName "pawmot/spring5-samples"
    tag project.version
}
```

The `tag` property is optional - omitting will result in an image with tag `latest`

_NB_: The above `build.gradle` file is not complete, only relevant parts shown.