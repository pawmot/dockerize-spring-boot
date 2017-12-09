![Travis badge](https://travis-ci.org/pawmot/dockerize-spring-boot.svg?branch=master) [![MIT Licence](https://badges.frapsoft.com/os/mit/mit.png?v=103)](https://opensource.org/licenses/mit-license.php)
# Dockerize Spring Boot
## Gradle plugin

To use this plugin apply it in the gradle project/module that uses Spring Boot Gradle plugin (versions 1.x or 2.x):

```
buildscript {
    repositories {
        mavenCentral()
        maven { url "https://plugins.gradle.org/m2/" } 
    }
    dependencies {
        classpath "org.springframework.boot:spring-boot-gradle-plugin:$spring_boot_version"
        classpath "gradle.plugin.com.pawmot:dockerize-spring-boot:$plugin_version"
    }
}

apply plugin: 'org.springframework.boot'
apply plugin: 'com.pawmot.dockerize-spring-boot'

docker {
    imageName 'some_image_name'
    tag project.version // or a variable or literal
}
```

The `tag` property is optional - omitting will result in an image with tag `latest`

_NB_: The above `build.gradle` file is not complete, only relevant parts are shown.

### Dockerfile

The dockerfile to be used by the plugin should be located in `src/docker` folder and should refer to the built Spring Boot jar as `app.jar` (e.g. `ADD app.jar app.jar`).
