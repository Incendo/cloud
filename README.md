<div align="center">
<img src="icons/CloudNew.png" width="300px"/>
<br/>
<h1>cloud command framework</h1>

![license](https://img.shields.io/github/license/incendo/cloud.svg)
[![central](https://img.shields.io/maven-central/v/cloud.commandframework/cloud-core)](https://search.maven.org/search?q=cloud.commandframework)
![build](https://img.shields.io/github/actions/workflow/status/incendo/cloud/build.yml?logo=github)
[![docs](https://img.shields.io/readthedocs/incendocloud?logo=readthedocs)](https://cloud.incendo.org)
</div>

> [!NOTE]
> Cloud 2 is a major update with many significant changes. Cloud 2 is _not_ compatible with version 1.
> You can find the changelog [here](https://cloud.incendo.org/en/latest/cloud-v2/).

Cloud is a general-purpose Java command dispatcher &amp; framework. It allows programmers to define command chains that are then parsed and invoked from user-supplied string inputs, to execute pre-defined actions.

Cloud commands consist out of deterministic chains of strongly typed arguments. When you define a command,
you know exactly what type of data you're going to be working with, and you know that there will be no
ambiguity at runtime. Cloud promotes writing reusable code, making it very easy to define new commands.

Cloud allows you to use command builders, in Java:
```java
manager.command(
    manager.commandBuilder("command", "alias")
        .literal("literal")
        .required("number", integerParser())
        .optional("string", stringParser(), ArgumentDescription.of("A string!"))
        .handler(commandContext -> {
            final int number = commandContext.get("number");
            final String string = commandContext.getOrDefault("string", "");
            // ...
        })
);
```
or in Kotlin:
```kotlin
manager.buildAndRegister("command", aliases = arrayOf("alias")) {
    literal("literal")
    required("number", integerParser())
    optional("string", stringParser()) {
        description { "A string!" }
    }
    handler { context ->
        val number: Int = context["number"]
        val string: String = context.getOrDefault("string", "")
        // ...
    }
}
```

or using annotated methods, in Java:
```java
@CommandMethod("command literal <number> [string]")
public void yourCommand(
        CommandSender sender,
        int number, // @Argument is optional!
        @Argument(value = "string") @Default("string!") String str
) {
    // ...
}
```
or in Kotlin:
```kotlin
@CommandMethod("command literal <number> [string]")
public suspend fun yourCommand(
    sender: CommandSender,
    number: Int, // @Argument is optional!
    @Argument("string") str: String = "string!"
) {
    // ...
}
```

depending on your preference.

Cloud is built to be very customisable, in order to fit your needs. You can inject handlers and processors
along the entire command chain. If the pre-existing command parsers aren't enough for your needs, you're easily
able to create your own parsers. If you use the annotation parsing system, you can also define your own annotations
and register them to further customise the behaviour of the library.

Cloud by default ships with implementations and mappings for the most common Minecraft server platforms, JDA and javacord for
Discord bots, PircBotX for IRC and [cloud-spring](https://github.com/incendo/cloud-spring) for Spring Shell.
The core module allows you to use Cloud anywhere, simply by implementing the CommandManager for the platform of your choice.

## links  

- JavaDoc: https://javadoc.io/doc/cloud.commandframework
- Docs: https://cloud.incendo.org
- Incendo Discord: https://discord.gg/aykZu32

### repositories

- [cloud](https://github.com/incendo/cloud) - main repository
- [cloud-processors](https://github.com/incendo/cloud-processors) - extra pre- & postprocessors
- [cloud-neoforge](https://github.com/incendo/cloud-neoforge) - cloud implementation for neoforge
- [cloud-spring](https://github.com/incendo/cloud-spring) - cloud implementation for spring-shell
- [cloud-build-logic](https://github.com/incendo/cloud-build-logic) - gradle plugin for building cloud
- [cloud-docs](https://github.com/incendo/cloud-docs) - documentation sources
  
## develop &amp; build  
  
To clone the repository, use `git clone https://github.com/Incendo/cloud.git`.

To then build it, use `./gradlew clean build`. If you want to build the examples as well, use `./gradlew clean build
-Pcompile-examples`.

## use

To use `cloud` you will first need to add it as a dependency to your project.

Release builds of Cloud are available through the Maven central repository.
Snapshot builds of Cloud are available through the [Sonatype OSS Snapshot repository](https://oss.sonatype.org/content/repositories/snapshots).

**maven**:
```xml
<dependency>  
 <groupId>cloud.commandframework</groupId>
 <artifactId>cloud-PLATFORM</artifactId>
 <version>2.0.0-SNAPSHOT</version>
</dependency>
<!-- 
~    Optional: Allows you to use annotated methods
~    to declare commands 
-->
<dependency>  
 <groupId>cloud.commandframework</groupId>
 <artifactId>cloud-annotations</artifactId>
 <version>2.0.0-SNAPSHOT</version>
</dependency>
``` 

```xml
<!-- For snapshot builds -->
<repository>
 <id>sonatype-snapshots</id>
 <url>https://oss.sonatype.org/content/repositories/snapshots</url>
</repository>
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
                  <shadedPattern>YOUR.PACKAGE.HERE.shaded.cloud</shadedPattern> <!-- Replace this -->
               </relocation>
               <relocation>
                  <pattern>io.leangen.geantyref</pattern>
                  <shadedPattern>YOUR.PACKAGE.HERE.shaded.typetoken</shadedPattern> <!-- Replace this -->
               </relocation>
            </relocations>
         </configuration>
      </plugin>
   </plugins>
</build>
```

**gradle**:
```kotlin
repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots") // For snapshot builds
}
```

```kotlin
dependencies {
    implementation("cloud.commandframework", "cloud-PLATFORM", "2.0.0-SNAPSHOT")
}
```

To shade and relocate cloud use [Gradle Shadow](https://github.com/johnrengelman/shadow).

Replace `PLATFORM` with your platform of choice. We currently support: `bukkit`, `paper`, `bungee` and `velocity`for minecraft and `jda` and `javacord` for discord. All modules use the same versions.
More information about the Minecraft specific modules can be found [here](https://github.com/Incendo/cloud/tree/master/cloud-minecraft).

## attributions, links &amp; acknowledgements  
  
This library is licensed under the <a href="https://opensource.org/licenses/MIT">MIT</a> license, and the code copyright  belongs to Alexander Söderberg. The implementation is based on a paper written by the copyright holder, and this paper exists under the <a href="https://creativecommons.org/licenses/by/4.0/legalcode">CC Attribution 4</a> license.  
  
The <a href="https://iconscout.com/icons/cloud" target="_blank">Cloud</a> icon was created by <a href="https://iconscout.com/contributors/oviyan">
Thanga Vignesh P</a> on <a href="https://iconscout.com">Iconscout</a> and Digital rights were purchased under a premium plan.
