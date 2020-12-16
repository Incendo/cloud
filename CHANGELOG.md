# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
 - Added `@Suggestions` annotated methods
 - Type safe meta system

### Changed
 - Moved the parser injector registry into CommandManager and added injection to CommandContext
 - Supporting repeating literals or argument names

### Deprecated
 - String keyed command meta

## [1.2.0] - 2020-12-07

### Added
 - Added access to the CloudBrigadierManager from Brigadier-enabled command managers
 - Added parameter injectors (cloud-annotations)
 - Store currently parsing command argument in the command context
 - Added a method to CloudBrigadierManager to enable or disable Brigadier native suggestions for specific argument types
 - Added a method to get the failure reason of SelectorParseExceptions
 - Added some methods to FlagContext to work with flag values as optionals
 - Allow for use of named suggestion providers with `@Flag`s (cloud-annotations)
 - Added `CommandExecutionException` which wraps any exception thrown during the execution of command handlers. Should be
  handled using `CommandManager#registerExceptionHandler`, similar to `NoSuchCommandException`, `ArgumentParseException`, etc.
 - Added registration state to command managers
 - Added ALLOW_UNSAFE_REGISTRATION ManagerSetting to disable state checks when registering commands
 - Added OVERRIDE_EXISTING_COMMANDS ManagerSetting to allow for overriding of existing commands on supported platforms
 
### Changed
 - Allow for use of `@Completions` annotation with argument types other than String
 - Allow for use of a BiFunction<C, E, Component> instead of just a Function<E, Component> in MinecraftExceptionHandler
 
### Deprecated
 - Deprecated LockableCommandManager in favor of CommandManager state
 
### Fixed
 - Use the correct default range for Double and Float parsers in the StandardParserRegistry
 - Fix Bukkit alias command suggestions without Brigadier
 - Fix Bukkit command alias registration when using Brigadier
 - Fixed a bug where providing valid input for an argument caused cloud to no longer make suggestions
 - Detect and throw an exception for ambiguous nodes in more cases
 - CloudBrigadierManager no longer forgets the command sender

## [1.1.0] - 2020-10-24

### Added
 - Added ExampleVelocityPlugin
 - Added CloudInjectionModule to cloud-velocity
 - Added PlayerArgument to cloud-velocity
 - Added TextColorArgument to minecraft-extras
 - Added LocationArgument to cloud-bukkit
 - Added ServerArgument to cloud-velocity
 - Added LockableCommandManager to cloud-core
 - Added VelocityCommandPreprocessor to cloud-velocity
 - Added PlayerArgument to cloud-bungee
 - Added ServerArgument to cloud-bungee
 - Added ExampleBungeePlugin
 - Added CaptionKeys to cloud-bungee
 - Added BungeeCommandPreprocessor to cloud-bungee
 - Added named suggestion providers
 - Added a PircBotX implementation

### Changed
 - Allow for combined presence flags, such that `-a -b -c` is equivalent to `-abc`
 - Allow for class annotations as a default for when an annotation is not present on a method
 - Allow for annotated annotations
 
### Fixed
 - Fix arguments with no required children not being executors (cloud-brigadier)

## [1.0.2] - 2020-10-18

### Fixed
 - Fixed quoted parsing in StringArgument
 - Fixed wrong suggestions following invalid literals
 - Fixes chained optionals not allowing the command to be executed when more than one optional is omitted

### Changed
 - Updated adventure-api from 4.0.0 to 4.1.1
 - Updated Velocity module for breaking API changes (sendMessage needs an Identity)

## [1.0.1] - 2020-10-14

### Changes
 - Switched from a snapshot to a release version of adventure 4.0.0
 - Added `Identity.nil()` when sending adventure messages
