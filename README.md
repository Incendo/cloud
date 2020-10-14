
<div align="center">  
 <img src="icons/cloud_spooky.png" width="300px"/>  
</div>  
  
# cloud command framework ![license](https://img.shields.io/github/license/Sauilitired/cloud.svg) ![build](https://github.com/Sauilitired/cloud/workflows/Java%20CI%20with%20Maven/badge.svg) [![CodeFactor](https://www.codefactor.io/repository/github/sauilitired/cloud/badge)](https://www.codefactor.io/repository/github/sauilitired/cloud)

Cloud is a general-purpose Java command dispatcher &amp; framework. It allows programmers to define command chains that are then parsed and invoked from user-
supplied string inputs, to execute pre-defined actions.

Cloud commands consist out of deterministic command chains where the arguments are strongly typed. When you write a command, you know exactly
what type of data you will get to work with and you won't need to spend hours debugging command contexts while crying profusely. The experience
of using the framework is like floating on a fluffy cloud in heaven. Its feature set is derived from Already existing Command Frameworks, while being less restrictive, opinionated and confusing.  

Cloud allows for commands to be defined using builder patterns, like this:
```java
manager.command(
    manager.commandBuilder("command", Description.of("Test cloud command using a builder"), "alias")
           .argument(StringArgument.of("input"))
           .argument(IntegerArgument.newBuilder("number").withMin(1).withMax(100).build())
           .handler(context -> {
                String input = context.get("input");
                int number = context.get("number");
                context.getSender().sendMessage(String.format(
                        "I am %d%% hyped for %s!"
                        number,
                        input
                );
           })
);
```
or using annotated methods, like this:
```java
@CommandDescription("Test cloud command using @CommandMethod")
@CommandMethod("command|alias <input> <number>")
private void yourCommand(
    Player sender,
    @Argument("input") String input,
    @Argument("number") @Range(min = "1", max = "100") int number)
) {
    player.sendMessage(String.format(
        "I am %d%% hyped for %s!"
        number,
        input
    ));
}
```
to allow your users to use commands like `/command cloud 100`.

All this while allowing you to extend and modify the command experience. The framework supports custom (any) command sender
types,
argument types &amp; parsers,
annotation mappers and preprocessors.
Cloud also has an advanced command suggestion system, that allows for context aware
command completion and suggestions.

If using the annotation parsing system, you can create custom annotation mappers to use your own annotation bindings for
command parsing, preprocessing, etc.

Cloud by default ships with implementations and mappings for the most common Minecraft server platforms and JDA and javacord for
Discord bots. The core
module allows you to use Cloud anywhere, simply by implementing the CommandManager for the platform of your choice.

The code is based on a (W.I.P) paper that can be found [here](https://github.com/Sauilitired/Sauilitired/blob/master/AS_2020_09_Commands.pdf).  

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
- **cloud-services**: Services for Cloud
- **cloud-minecraft/cloud-brigadier**: Brigadier mappings for cloud
- **cloud-minecraft/cloud-bukkit**: Bukkit 1.8.8+ implementation of cloud
- **cloud-minecraft/cloud-paper**: Module that extends cloud-bukkit to add special support for Paper 1.8.8+
- **cloud-minecraft/cloud-bungee**: BungeeCord 1.8.8+ implementation of Cloud
- **cloud-minecraft/cloud-velocity**: Velocity v1.1.0 implementation of cloud
- **cloud-minecraft/cloud-cloudburst**: Cloudburst v1.0.0+ implementation of cloud
- **cloud-minecraft/cloud-minecraft-extras**: Opinionated Extra Features for cloud-minecraft
- **cloud-discord/cloud-jda**: JDA v4.2.0_207+ implementation of cloud
- **cloud-discord/cloud-javacord**: Javacord v3.1.1+ implementation of cloud

## links  

- JavaDoc: https://javadoc.commandframework.cloud/
- Wiki: https://github.com/Sauilitired/cloud/wiki
- Discord: https://discord.gg/KxkjDVg  
  
## develop &amp; build  
  
To clone the repository, use `git clone https://github.com/Sauilitired/cloud.git`.

To then build it, use `./gradlew clean build`. If you want to build the examples as well, use `./gradlew clean build
-Pcompile-examples`.
  
There is a bash script (`build.sh`) that performs the submodule updating &amp; builds the project.  
Feel free to use this if you want to.  

## use

To use `cloud` you will first need to add it as a dependency to your project.

Snapshot builds of Cloud are available through the [Sonatype OSS Snapshot repository](https://oss.sonatype.org/content/repositories/snapshots).
Release builds of Cloud are available through the Maven central repository.

**maven**:
```xml
<!-- For snapshot builds ->
<repository>  
 <id>sonatype-snapshots</id>
 <url>https://oss.sonatype.org/content/repositories/snapshots</url>
</repository>  
```

```xml  
<dependency>  
 <groupId>cloud.commandframework</groupId>
 <artifactId>cloud-PLATFORM</artifactId>
 <version>1.1.0-SNAPSHOT</version>
</dependency>
<!-- 
~    Optional: Allows you to use annotated methods
~    to declare commands 
-->
<dependency>  
 <groupId>cloud.commandframework</groupId>
 <artifactId>cloud-annotations</artifactId>
 <version>1.1.0-SNAPSHOT</version>
</dependency>
``` 

If you are shading in cloud, it is highly recommended that you relocate all of our classes to prevent issues
with conflicting dependencies:

```xml
<build>
    <plugins>
         <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-shade-plugin</artifactId>
                <version>3.2.4</version>
                <executions>
                    <execution>
                        <phase>package</phase>
                        <goals>
                            <goal>shade</goal>
                        </goals>
                        <configuration>
                            <createDependencyReducedPom>false</createDependencyReducedPom>
                        </configuration>
                    </execution>
                </executions>
                <configuration>
                    <dependencyReducedPomLocation>${project.build.directory}/dependency-reduced-pom.xml</dependencyReducedPomLocation>
                    <relocations>
                        <relocation>
                            <pattern>cloud.commandframework</pattern>
                            <shadedPattern>YOUR.PACKAGE.HERE.cloud</shadedPattern> <!-- Replace this -->
                        </relocation>
                        <relocation>
                            <pattern>cloud.commandframework.services</pattern>
                            <shadedPattern>YOUR.PACKAGE.HERE.cloud.pipeline</shadedPattern> <!-- Replace this -->
                        </relocation>
                    </relocations>
                </configuration>
            </plugin>
    </plugins>
</build>
```

**gradle**:
```groovy
repositories {
    maven { url = 'https://oss.sonatype.org/content/repositories/snapshots' }
}
```

```groovy
dependencies {
    implementation 'cloud.commandframework:cloud-PLATFORM:1.1.0-SNAPSHOT'
}
```

To shade and relocate cloud use [Gradle Shadow](https://github.com/johnrengelman/shadow).

Replace `PLATFORM` with your platform of choice. We currently support: `bukkit`, `paper`, `bungee` and `velocity`for minecraft and `jda` and `javacord` for discord. All modules use the same versions.
More information about the Minecraft specific modules can be found [here](https://github.com/Sauilitired/cloud/tree/master/cloud-minecraft).

## attributions, links &amp; acknowledgements  
  
This library is licensed under the <a href="https://opensource.org/licenses/MIT">MIT</a> license, and the code copyright  belongs to Alexander Söderberg. The implementation is based on a paper written by the copyright holder, and this paper exists under the <a href="https://creativecommons.org/licenses/by/4.0/legalcode">CC Attribution 4</a> license.  
  
The <a href="https://iconscout.com/icons/cloud" target="_blank">Cloud</a> icon was created by by <a href="https://iconscout.com/contributors/oviyan">
Thanga Vignesh P</a> on <a href="https://iconscout.com">Iconscout</a> and Digital rights were purchased under a premium plan.
