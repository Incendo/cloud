
<div align="center">  
 <img src="icons/cloud.svg" width="300px"/>  
</div>  
  
# cloud command framework  
  
[![CodeFactor](https://www.codefactor.io/repository/github/sauilitired/cloud/badge)](https://www.codefactor.io/repository/github/sauilitired/cloud)  
  
This is going to be a general-purpose Java command dispatcher &amp; framework.  
It will allow programmers to define command chains that users can use to execute pre-defined actions.  
  
The library was named cloud because using it makes you feel like you're floating around on a cloud in heaven. It's an experience of pure bliss and joy.  
This is unlike the experience of using **a**ny **c**ommand **f**ramework that currently exists for the JVM, which can be compared to drowning in a pool of lava while watching your family get eaten by a pack of wolves.   
  
Its feature set is derived from **a**lready existing **c**ommand **f**rameworks, while being less restrictive, opinionated and confusing.  
CLOUD is built to be completely deterministic and your commands will behave exactly as you've programmed them to. No fluff and no mess, just a smooth cloud-like experience.  
  
The code is based on a paper that can be found [here](https://github.com/Sauilitired/Sauilitired/blob/master/AS_2020_09_Commands.pdf).  
  
## goals  
  
- Allow for commands to be defined using builder patterns  
- Allow for commands to be defined using annotated methods  
- Allow for command pre-processing  
- Allow for command suggestion outputs  
  
Once the core functionality is present, the framework will offer implementation modules, supporting a wide variety of platforms.  
  
### implementations  
  
- Minecraft:  
  - Generic Brigadier module  
  - Bukkit module  
  - Paper module, with optional Brigadier support  
  - Sponge module  
  - Cloudburst  
  - Bungee module  
  - Velocity module  
  
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
  
## links  
  
- Discord: https://discord.gg/KxkjDVg  
  
## develop &amp; build  
  
To clone the repository, use `git clone --recursive https://github.com/Sauilitired/cloud.git`.  
To then build it, use `mvn clean package`. If you've already cloned the repository without  
doing it recursively, use `git submodule update --remote` to update the submodule. This is  
only needed the first time, as Maven will perform this operation when building.   
  
There is a bash script (`build.sh`) that performs the submodule updating &amp; builds the project.  
Feel free to use this if you want to.  
  
## maven  
  
cloud is available from [IntellectualSites](https://intellectualsites.com)' maven repository:  
  
```xml  
<repository>  
 <id>intellectualsites-snapshots</id>  
 <url>https://mvn.intellectualsites.com/content/repositories/snapshots</url>  
</repository>  
```  
  
```xml  
<dependency>  
 <groupId>com.intellectualsites</groupId>  
 <artifactId></artifactId> <version></version></dependency>  
```  
  
### attributions, links &amp; acknowledgements  
  
This library is licensed under the <a href="https://opensource.org/licenses/MIT">MIT</a> license, and the code copyright  
belongs to Alexander Söderberg. The implementation is based on a paper written by the copyright holder, and this paper exists  
under the <a href="https://creativecommons.org/licenses/by/4.0/legalcode">CC Attribution 4</a> license.  
  
The <a href="https://iconscout.com/icons/cloud" target="_blank">Cloud</a> icon was created by by   
<a href="https://iconscout.com/contributors/oviyan">Thanga Vignesh P</a> on <a href="https://iconscout.com">Iconscout</a>  
and Digital rights were purchased under a premium plan.
