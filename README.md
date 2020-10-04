
<div align="center">  
 <img src="icons/cloud_spooky.png" width="300px"/>  
</div>  
  
# cloud command framework ![license](https://img.shields.io/github/license/Sauilitired/cloud.svg) ![build](https://github.com/Sauilitired/cloud/workflows/Java%20CI%20with%20Maven/badge.svg) [![CodeFactor](https://www.codefactor.io/repository/github/sauilitired/cloud/badge)](https://www.codefactor.io/repository/github/sauilitired/cloud)

Cloud is a general-purpose Java command dispatcher &amp; framework. It allows programmers to define command chains that are then parsed and invoked from user-
supplied string inputs, to execute pre-defined actions.

Cloud commands consist out of deterministic command chains are the arguments are strongly typed. When you write a command, you know exactly
what type of data you will get to work with and you won't need to spend hours debugging command contexts while crying profusely. The experience
of using the framework is like floating on a fluffy cloud in heaven. Its feature set is derived from Already existing Command Frameworks, while being less restrictive, opinionated and confusing.  

Cloud allows for commands to be defined using builder patterns, like this:
```java
mgr.command(mgr.commandBuilder("give")
               .withSenderType(Player.class)
               .argument(EnumArgument.of(Material.class, "material"))
               .argument(IntegerArgument.of("amount"))
               .handler(c -> {
                   final Material material = c.get("material");
                   final int amount = c.get("amount");
                   final ItemStack itemStack = new ItemStack(material, amount);
                   ((Player) c.getSender()).getInventory().addItem(itemStack);
                   c.getSender().sendMessage("You've been given stuff, bro.");
               }));
```

or using annotated methods, like this:
```java
@CommandPermission("some.permission.node")
@CommandDescription("Test cloud command using @CommandMethod")
@CommandMethod("annotation|a <input> [number]")
private void annotatedCommand(@Nonnull final Player player,
                              @Argument("input") @Completions("one,two,duck") @Nonnull final String input,
                              @Argument(value = "number", defaultValue = "5") @Range(min = "10", max = "100")
                                  final int number) {
    player.sendMessage(ChatColor.GOLD + "Your input was: " + ChatColor.AQUA + input 
                          + ChatColor.GREEN + " (" + number + ")");
}
```
while allowing you to extend and modify the command experience. The framework supports custom (any) command sender types, argument types &amp; parsers,
annotation mappers and preprocessors. Cloud also has an advanced command suggestion system, that allows for context aware command completion and suggestions.

Cloud by default ships with implementations and mappings for the most common Minecraft server platforms, JDA for Discord bots and JLine3 for CLI applications. The core
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
- **cloud-jline**: W.I.P JLine3 implementation of cloud
- **cloud-services**: Services for Cloud
- **cloud-minecraft/cloud-brigadier**: Brigadier mappings for cloud
- **cloud-minecraft/cloud-bukkit**: Bukkit 1.8.8+ implementation of cloud
- **cloud-minecraft/cloud-paper**: Module that extends cloud-bukkit to add special support for Paper 1.8.8+
- **cloud-minecraft/cloud-bungee**: BungeeCord 1.8.8+ implementation of Cloud
- **cloud-minecraft/cloud-velocity**: Velocity v1.1.0 implementation of cloud
- **cloud-minecraft/cloud-cloudburst**: Cloudburst v1.0.0+ implementation of cloud

## links  
  
- Discord: https://discord.gg/KxkjDVg  
  
## develop &amp; build  
  
To clone the repository, use `git clone https://github.com/Sauilitired/cloud.git`.
To then build it, use `mvn clean package`.
  
There is a bash script (`build.sh`) that performs the submodule updating &amp; builds the project.  
Feel free to use this if you want to.  

## use

To use `cloud` you will first need to add it as a dependency to your project.

Snapshot builds of Cloud are available through the [Sonatype OSS Snapshot repository](https://oss.sonatype.org/content/repositories/snapshots).
Release builds of Cloud are available throgh the Maven central repository.

**maven**:
```xml  
<repository>  
 <id>sonatype-snapshots</id>
 <url>https://oss.sonatype.org/content/repositories/snapshots</url>
</repository>  
```

```xml  
<dependency>  
 <groupId>cloud.commandframework</groupId>
 <artifactId>cloud-PLATFORM</artifactId>
 <version>1.0.0-SNAPSHOT</version>
</dependency>
<!-- 
~    Optional: Allows you to use annotated methods
~    to declare commands 
-->
<dependency>  
 <groupId>cloud.commandframework</groupId>
 <artifactId>cloud-annotations</artifactId>
 <version>1.0.0-SNAPSHOT</version>
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
    maven { url = 'https://mvn.intellectualsites.com/content/repositories/snapshots' }
}
```

```groovy
dependencies {
    implementation 'cloud.commandframework:cloud-PLATFORM:0.2.0-SNAPSHOT'
}
```

Replace `PLATFORM` with your platform of choice. We currently support: `bukkit`, `paper`, `bungee` and `velocity`. All modules use the same versions.
More information about the Minecraft specific modules can be found [here](https://github.com/Sauilitired/cloud/tree/master/cloud-minecraft).

## attributions, links &amp; acknowledgements  
  
This library is licensed under the <a href="https://opensource.org/licenses/MIT">MIT</a> license, and the code copyright  belongs to Alexander Söderberg. The implementation is based on a paper written by the copyright holder, and this paper exists under the <a href="https://creativecommons.org/licenses/by/4.0/legalcode">CC Attribution 4</a> license.  
  
The <a href="https://iconscout.com/icons/cloud" target="_blank">Cloud</a> icon was created by by <a href="https://iconscout.com/contributors/oviyan">
Thanga Vignesh P</a> on <a href="https://iconscout.com">Iconscout</a> and Digital rights were purchased under a premium plan.
