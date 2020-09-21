
<div align="center">  
 <img src="icons/cloud.svg" width="300px"/>  
</div>  
  
# cloud command framework ![license](https://img.shields.io/github/license/Sauilitired/cloud.svg) ![build](https://github.com/Sauilitired/cloud/workflows/Java%20CI%20with%20Maven/badge.svg) [![CodeFactor](https://www.codefactor.io/repository/github/sauilitired/cloud/badge)](https://www.codefactor.io/repository/github/sauilitired/cloud)

This is going to be a general-purpose Java command dispatcher &amp; framework.  
It will allow programmers to define command chains that users can use to execute pre-defined actions.  
  
The library was named cloud because using it makes you feel like you're floating around on a cloud in heaven. It's an experience of pure bliss and joy.  
This is unlike the experience of using **a**ny **c**ommand **f**ramework that currently exists for the JVM, which can be compared to drowning in a pool of lava while watching your family get eaten by a pack of wolves.   
  
Its feature set is derived from **a**lready existing **c**ommand **f**rameworks, while being less restrictive, opinionated and confusing.  
CLOUD is built to be completely deterministic and your commands will behave exactly as you've programmed them to. No fluff and no mess, just a smooth cloud-like experience.  
  
The code is based on a paper that can be found [here](https://github.com/Sauilitired/Sauilitired/blob/master/AS_2020_09_Commands.pdf).  
  
## goals  
  
- Allow for commands to be defined using builder patterns (Done)
- Allow for commands to be defined using annotated methods (Done)
- Allow for command pre-processing (Done)
- Allow for command suggestion outputs (Done)
  
Once the core functionality is present, the framework will offer implementation modules, supporting a wide variety of platforms.  
  
### implementations  
  
- Minecraft:  
  - Generic Brigadier module (Done)
  - Bukkit module (Done)
  - Paper module, with optional Brigadier support (Done) 
  - Sponge module  
  - Cloudburst  
  - Bungee module (Done)
  - Velocity module (Done)
  
- Create a Discord implementation (JDA)  
- Create a Java CLI implementation (JLine3)  

## nomenclature  
- **sender**: someone who is able to produce input  
- **argument**: an argument is something that can be parsed from a string  
- **required argument**: a required argument is an argument that must be provided by the sender  
- **optional argument**: an optional argument is an argument that can be omitted (may have a default value) 
- **static argument**: a string literal  
- **command**: a command is a chain of arguments and a handler that acts on the parsed arguments
- **command tree**: structure that contains all commands and is used to parse input into arguments

## modules
- **cloud-core**: Core module containing most of the cloud API, and shared implementations
- **cloud-annotations**: Annotation processing code that allows you to use annotated methods rather than builders
- **cloud-jline**: W.I.P JLine3 implementation of cloud
- **cloud-minecraft/cloud-brigadier**: Brigadier mappings for cloud
- **cloud-minecraft/cloud-bukkit**: Bukkit 1.8.8+ implementation of cloud
- **cloud-minecraft/cloud-paper**: Module that extends cloud-bukkit to add special support for Paper 1.8.8+
- **cloud-minecraft/cloud-bungee**: BungeeCord 1.8.8+ implementation of Cloud
- **cloud-minecraft/cloud-velocity**: Velocity v1.1.0 implementation of cloud

## links  
  
- Discord: https://discord.gg/KxkjDVg  
  
## develop &amp; build  
  
To clone the repository, use `git clone --recursive https://github.com/Sauilitired/cloud.git`.  
To then build it, use `mvn clean package`. If you've already cloned the repository without  
doing it recursively, use `git submodule update --remote` to update the submodule. This is  
only needed the first time, as Maven will perform this operation when building.   
  
There is a bash script (`build.sh`) that performs the submodule updating &amp; builds the project.  
Feel free to use this if you want to.  

## use

To use `cloud` you will first need to add it as a dependency to your project. Cloud is available from [IntellectualSites](https://intellectualsites.com)' maven repository.
  
**maven**:
```xml  
<repository>  
 <id>intellectualsites-snapshots</id>  
 <url>https://mvn.intellectualsites.com/content/repositories/snapshots</url>  
</repository>  
```  
  
```xml  
<dependency>  
 <groupId>com.intellectualsites</groupId>  
 <artifactId>cloud-PLATFORM</artifactId>
 <version>0.1.0-SNAPSHOT</version>
</dependency>
<!-- 
~    Optional: Allows you to use the annotated methods
~    to declare commands 
-->
<dependency>  
 <groupId>com.intellectualsites</groupId>  
 <artifactId>cloud-annotations</artifactId>
 <version>0.1.0-SNAPSHOT</version>
</dependency>
``` 

**gradle**:
```groovy
repositories {
    maven { url = 'https://mvn.intellectualsites.com/content/repositories/snapshots' }
}
```

```groovy
dependencies {
    implementation 'com.intellectualsites:cloud-PLATFORM:0.1.0-SNAPSHOT'
}
```

Replace `PLATFORM` with your platform of choice. We currently support: `bukkit`, `paper`, `bungee` and `velocity`. All modules use the same versions.
More information about the Minecraft specific modules can be found [here](https://github.com/Sauilitired/cloud/tree/master/cloud-minecraft).

## attributions, links &amp; acknowledgements  
  
This library is licensed under the <a href="https://opensource.org/licenses/MIT">MIT</a> license, and the code copyright  belongs to Alexander Söderberg. The implementation is based on a paper written by the copyright holder, and this paper exists under the <a href="https://creativecommons.org/licenses/by/4.0/legalcode">CC Attribution 4</a> license.  
  
The <a href="https://iconscout.com/icons/cloud" target="_blank">Cloud</a> icon was created by by <a href="https://iconscout.com/contributors/oviyan">
Thanga Vignesh P</a> on <a href="https://iconscout.com">Iconscout</a> and Digital rights were purchased under a premium plan.
