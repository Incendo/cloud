# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.8.4]

### Fixed
- Core: Flags not tab-completing unless string is empty ([#460](https://github.com/Incendo/cloud/pull/460))
- Core: Parser registry not properly resolving `TypeToken`s ([#454](https://github.com/Incendo/cloud/pull/454))
- Fabric: Pottery pattern registry overriding default string parser for annotations
- Fabric: Log cause of CommandExecutionException in fabric's default exception handler ([#466](https://github.com/Incendo/cloud/pull/466))

### Changed
- Core: Improved string parser supplier argument checking
- Bukkit/Paper: Improve docs around Brigadier support

## [1.8.3]

### Changed
- Fabric: Updated for Minecraft 1.19.4 ([#430](https://github.com/Incendo/cloud/pull/430))

## [1.8.2]

### Fixed
- Fabric: Published jar was not properly remapped and errored on startup in 1.8.1

## [1.8.1]

### Fixed
- Core: Fixed last argument always being treated as greedy in suggestions ([#428](https://github.com/Incendo/cloud/pull/428))
- Core: Remove redundant at literal in docs (formatting fix) ([#427](https://github.com/Incendo/cloud/pull/427))

### Changed
- Core: Improve ParameterInjectorRegistry#registerInjector JavaDoc ([#423](https://github.com/Incendo/cloud/pull/423))
- Fabric: Exposed permissions API in apiElements to match old behavior when consumers update to loom 1.1 (which also fixes api 
  guardian being missing from the classpath)

## [1.8.0]

### Added
- Core: Added `CommandBuilderApplicable` and `Command.Builder#apply(CommandBuilderApplicable)` ([#409](https://github.com/Incendo/cloud/pull/409))
- Core: Expose failure reason when flag parsing fails ([#380](https://github.com/Incendo/cloud/pull/380))
- Core: Allow registering injectors to `ParameterInjectorRegistry` using a class predicate instead of just a class ([#402](https://github.com/Incendo/cloud/pull/402))
- Core: Experimental option to allow flags to be parsed and suggested anywhere after the last literal in a command chain ([#395](https://github.com/Incendo/cloud/pull/395))
- Core: New `Filter` interface used by `FilteringCommandSuggestionProcessor`, allowing custom filter behavior ([#410](https://github.com/Incendo/cloud/pull/410))
- Core: Helper to get the root index help topic ([#415](https://github.com/Incendo/cloud/pull/415))
- Bukkit/Paper: Full support for Minecraft 1.19.3 ([#412](https://github.com/Incendo/cloud/pull/412))

### Fixed
- Core: Greedy arguments are now able to suggest after a space in the input, and will receive the full concatenated input 
  instead of just the queue head ([#414](https://github.com/Incendo/cloud/pull/414))
- Core: Fixed invalid suggestions when parsing of argument fails ([#401](https://github.com/Incendo/cloud/pull/401))
- Core: Fixed commands not being removed from command collection on unregister ([#408](https://github.com/Incendo/cloud/pull/408))
- Annotations: Fix argument annotation name not being processed ([#397](https://github.com/Incendo/cloud/pull/397))
- Brigadier: Further fixes for suggestions on old versions of Minecraft
- Brigadier: Fixed handling of `GREEDY_FLAG_YIELDING` string arguments
- Minecraft-Extras: Fixed wrong color codes in TextColorArgument ([#407](https://github.com/Incendo/cloud/pull/407))
- Minecraft-Extras: Removed double space when paginating with a blank help query
- Javacord: Fixed regex issue with certain prefixes ([#400](https://github.com/Incendo/cloud/pull/400))

### Changed
- Default suggestion processor ignores case when checking starts with ([#410](https://github.com/Incendo/cloud/pull/410))
- Deprecated `newBuilder` argument builder static factories, added `builder` factories to align with conventions for new 
  arguments ([#419](https://github.com/Incendo/cloud/pull/419))
- Core: Improve nullability annotations on generics in `CommandContext` ([#405](https://github.com/Incendo/cloud/pull/405))
- Core: Made injection order of `ParameterInjectorRegistry` consistent ([#402](https://github.com/Incendo/cloud/pull/402))
- Bukkit/Paper: Rewrite Bukkit entity selector arguments using `WrappedBrigadierParser` instead of Bukkit API
- Fabric: Updated for Minecraft 1.19.3, dropping support for previous versions ([#411](https://github.com/Incendo/cloud/pull/411))
- Minecraft: Default suggestion processor now removes the part of a suggestion which is before the last user typed space in 
  the input, allowing for full sentence suggestions more easily, especially combined with other fixes in this update ([#410](https://github.com/Incendo/cloud/pull/410))

## [1.7.1]

### Added
- Bukkit/Paper: Full support for Minecraft 1.19.1 and 1.19.2

### Fixed
- Core: Fix unregistering wrong node in some cases ([#389](https://github.com/Incendo/cloud/pull/389))
- Bukkit/Paper: Fix ItemStackPredicateArgument reflection
- Bukkit/Paper: Fix Brigadier completions on Minecraft 1.13.2
- Bukkit/Paper: Fix unregistering commands with newer versions of Commodore

## [1.7.0]

### Added
- Core: Allow for setting a custom `CaptionVariableReplacementHandler` on the command manager ([#352](https://github.com/Incendo/cloud/pull/352))
- Core: Add `DurationArgument` for parsing `java.time.Duration` ([#330](https://github.com/Incendo/cloud/pull/330)) 
- Core: Add delegating command execution handlers ([#363](https://github.com/Incendo/cloud/pull/363))
- Core: Add `builder()` getter to `Command.Builder` ([#363](https://github.com/Incendo/cloud/pull/363))
- Core: Add flag yielding modes to `StringArgument` and `StringArrayArgument` ([#367](https://github.com/Incendo/cloud/pull/367))
- Core: Add [apiguardian](https://github.com/apiguardian-team/apiguardian) `@API` annotations ([#368](https://github.com/Incendo/cloud/pull/368))
- Core: Deprecate prefixed getters/setters in `CommandManager` ([#377](https://github.com/Incendo/cloud/pull/377))
- Core: Add repeatable flags ([#378](https://github.com/Incendo/cloud/pull/378))
- Annotations: Annotation string processors ([#353](https://github.com/Incendo/cloud/pull/353))
- Annotations: `@CommandContainer` annotation processing ([#364](https://github.com/Incendo/cloud/pull/364))
- Annotations: `@CommandMethod` annotation processing for compile-time validation ([#365](https://github.com/Incendo/cloud/pull/365))
- Add root command deletion support (core/pircbotx/javacord/jda/bukkit/paper) ([#369](https://github.com/Incendo/cloud/pull/369),
  [#371](https://github.com/Incendo/cloud/pull/371))
- Bukkit/Paper: Full support for Minecraft 1.19

### Fixed
- Core: Fix missing caption registration for the regex caption ([#351](https://github.com/Incendo/cloud/pull/351))
- Core: Fix NPE thrown on empty command tree ([#337](https://github.com/Incendo/cloud/issues/337))
- Annotations: Fix MutableCommandBuilder Java/Kotlin interoperability issues ([#342](https://github.com/Incendo/cloud/issues/342))

### Changed
- Fabric: Updated for Minecraft 1.19 (no longer supports older versions) ([#356](https://github.com/Incendo/cloud/pull/356))
- Paper: Improved KeyedWorldArgument suggestions ([#334](https://github.com/Incendo/cloud/pull/334))
- Minecraft-Extras: Support sender-aware description decorators in MinecraftHelp ([#354](https://github.com/Incendo/cloud/pull/354))

## [1.6.2]

### Fixed
- Fix incorrect inputQueue usage in some default argument types

### Changed
- Bukkit: Update for Minecraft 1.18.2

## [1.6.1]

### Fixed
- Fix concurrent cause of execution exception not being wrapped right ([#326](https://github.com/Incendo/cloud/pull/326))
- Paper: Fix handling of empty slash buffer in async suggestion listener ([#327](https://github.com/Incendo/cloud/pull/327))
- Fix StringParser sometimes removing from input queue on failure

## [1.6.0]

### Added
- Kotlin: New module `cloud-kotlin-coroutines`: Support for suspending command handlers in builders and the Kotlin builder DSL
- Kotlin: New module `cloud-kotlin-coroutines-annotations`: Support for suspending annotated command functions using `AnnotationParser<C>.installCoroutineSupport()`
- Flags can be bound to a permission
- Paper: Implement KeyedWorldArgument for matching worlds by their namespaced key
- Annotations: Parser parameter annotations are now also parsed for flags ([#315](https://github.com/Incendo/cloud/pull/315))

### Changed
- Added `executeFuture` to `CommandExecutionHandler` which is now used internally. By default, this delegates to the old 
  `execute` method
- Added `@Liberal` annotation to mark boolean arguments as liberal when using the annotation parser ([#288](https://github.com/Incendo/cloud/pull/288))
- Annotations: Apply builder modifiers from class annotations ([#303](https://github.com/Incendo/cloud/pull/303))
- Annotations: Add default value to `@Argument`, which will force the parser to infer the argument name from the parameter name
- Annotations: `@CommandMethod` can now be used to annotate a class, allowing for a common literal prefix on all annotated command methods in that class. ([#301](https://github.com/Incendo/cloud/pull/301))

### Fixed
- Bukkit: Permission checking and syntax string for Bukkit '/help' command
- And/OrPermission factory method `of` did not preserve the conditional tree
- Formatting of literal arguments with no siblings in StandardCommandSyntaxFormatter
- Replaced improper usages of TypeToken#toString in error messages
- Fixed unhandled exceptions being swallowed when AsynchronousCommandExecutionCoordinator is configured with Synchronous Parsing enabled ([#307](https://github.com/Incendo/cloud/pull/307))

## [1.5.0]

### Added
 - `@Quoted` annotation for the `String` argument ([#239](https://github.com/Incendo/cloud/pull/239))
 - Expose `min` and `max` values on numerous number aruments ([#255](https://github.com/Incendo/cloud/pull/255))
 - `CommandArgument.TypedBuilder`, a variant of `CommandArgument.Builder` designed for subclassing, that returns a self type
 - `MappedArgumentParser` and `ArgumentParser#map` for creating mapped argument parsers
 - Helper methods for mapping values/failures of `ArgumentParseResult`s (`ArgumentParseResult#mapParsedValue`, `ArgumentParseResult#flatMapParsedValue`, and `ArgumentParseResult#mapFailure`)
 - JDA: Role argument parser ([#219](https://github.com/Incendo/cloud/pull/219))
 - Brigadier: `WrappedBrigadierParser` for wrapping Brigadier `ArgumentType`s as cloud `ArgumentParser`s
 - Bukkit: Implement parser for ProtoItemStack ([#257](https://github.com/Incendo/cloud/pull/257))
 - Bukkit: Implement parsers for ItemStackPredicate and BlockPredicate ([#259](https://github.com/Incendo/cloud/pull/259))
 - Bukkit: Support for Mojang mapped servers ([#267](https://github.com/Incendo/cloud/pull/267))
 - Fabric: Fabric implementation added ([#223](https://github.com/Incendo/cloud/pull/223))
 - Minecraft-Extras/Bukkit/Paper/Velocity: `AudienceProvider.nativeAudience`, `MinecraftHelp.createNative` , `BukkitCommandManager.createNative` , `PaperCommandManager.createNative`, `CloudInjectionModule.createNative`  helper methods for `Audience` sender types ([#240](https://github.com/Incendo/cloud/pull/240))

### Changed
 - `Long` argument parser is now public ([#229](https://github.com/Incendo/cloud/pull/229))
 - JDA: Add isolation system to JDA user argument ([#220](https://github.com/Incendo/cloud/pull/220))
 - Bukkit: Use Command instead of TabCompleteEvent on Bukkit
 - Bukkit: Minecraft 1.17 support added

### Deprecated
 - JDA: Deprecated old UserParser that did not take an isolation parameter ([#220](https://github.com/Incendo/cloud/pull/220))
 
### Fixed
 - Tuple implementations now do not throw an error when using the toArray method ([#222](https://github.com/Incendo/cloud/pull/222))
 - Argument parser for `long` types was not registered ([#229](https://github.com/Incendo/cloud/pull/229))

## [1.4.0] - 2021-01-16

### Added
 - Predicate command filters to the help system ([#187](https://github.com/Incendo/cloud/pull/187))
 - Allow flags to contain compound arguments ([#192](https://github.com/Incendo/cloud/pull/192))
 - Allow for components in exceptions and meta data ([#200](https://github.com/Incendo/cloud/pull/200))
 - Location2DArgument ([#201](https://github.com/Incendo/cloud/pull/201))
 - Expose the Command which led to `InvalidCommandSenderException`s ([#204](https://github.com/Incendo/cloud/pull/211))
 - Expose the CommandContext which led to `CommandExecutionException`s ([#204](https://github.com/Incendo/cloud/pull/211))
 - Helper methods for command flags to MutableCommandBuilder ([#205](https://github.com/Incendo/cloud/pull/205))
 - CommandFlag accepting getters to FlagContext ([#206](https://github.com/Incendo/cloud/pull/206))
 - More abstract description concept ([#207](https://github.com/Incendo/cloud/pull/207))
 - Predicate permissions ([#210](https://github.com/Incendo/cloud/pull/210))
 - Injection services ([#211](https://github.com/Incendo/cloud/pull/211))
 - Sponge v7 support ([#212](https://github.com/Incendo/cloud/pull/211))
 - Logical `AND` and `OR` operations for `CommandPermission`s ([#213](https://github.com/Incendo/cloud/pull/213))

### Changed
 - Allow command argument names to include `_` and `-` ([#186](https://github.com/Incendo/cloud/pull/186))
 - Make it easier to use translatable components with MinecraftHelp ([#197](https://github.com/Incendo/cloud/pull/197))
 - Show "No result for query" when a multi-help topic is empty
 - Use the method+field annotation accessor rather than the method accessor when injecting method parameters

### Deprecated
 - Description, and everything using Description directly ([#207](https://github.com/Incendo/cloud/pull/207))
 - ParameterInjectorRegistry#injectors ([#211](https://github.com/Incendo/cloud/pull/211))

### Fixed
 - Issue where suggestions were shown multiple times when using Brigadier ([#184](https://github.com/Incendo/cloud/pull/184))
 - Issue where the command manager was in the wrong state if no commands had been registered ([#196](https://github.com/Incendo/cloud/pull/196))
 - Issues with JDA ([#198](https://github.com/Incendo/cloud/pull/198)) ([#199](https://github.com/Incendo/cloud/pull/199)) ([#214](https://github.com/Incendo/cloud/pull/214))
 - Console suggestions for Bukkit

## [1.3.0] - 2020-12-18

### Added
 - `@Suggestions` annotated methods
 - `@Parser` annotated methods
 - Type safe meta system
 - Allow interception of command builders based on annotations in AnnotationParser
 - Kotlin DSL

### Changed
 - Move the parser injector registry into CommandManager and added injection to CommandContext
 - Supporting repeating literals or argument names
 - Make CommandMeta and FlagContext more Kotlin friendly

### Deprecated
 - String keyed command meta
 - ParameterInjectorRegistry#injectors

### Fixed
 - Fixed issue with task synchronization

## [1.2.0] - 2020-12-07

### Added
 - Access to the CloudBrigadierManager from Brigadier-enabled command managers
 - Parameter injectors (cloud-annotations)
 - Store currently parsing command argument in the command context
 - A method to CloudBrigadierManager to enable or disable Brigadier native suggestions for specific argument types
 - A method to get the failure reason of SelectorParseExceptions
 - Some methods to FlagContext to work with flag values as optionals
 - Allow for use of named suggestion providers with `@Flag`s (cloud-annotations)
 - `CommandExecutionException` which wraps any exception thrown during the execution of command handlers. Should be
  handled using `CommandManager#registerExceptionHandler`, similar to `NoSuchCommandException`, `ArgumentParseException`, etc.
 - Registration state to command managers
 - ALLOW_UNSAFE_REGISTRATION ManagerSetting to disable state checks when registering commands
 - OVERRIDE_EXISTING_COMMANDS ManagerSetting to allow for overriding of existing commands on supported platforms
 
### Changed
 - Allow for use of `@Completions` annotation with argument types other than String
 - Allow for use of a BiFunction<C, E, Component> instead of just a Function<E, Component> in MinecraftExceptionHandler
 
### Deprecated
 - LockableCommandManager in favor of CommandManager state
 
### Fixed
 - Use the correct default range for Double and Float parsers in the StandardParserRegistry
 - Bukkit alias command suggestions without Brigadier
 - Bukkit command alias registration when using Brigadier
 - A bug where providing valid input for an argument caused cloud to no longer make suggestions
 - Detect and throw an exception for ambiguous nodes in more cases
 - CloudBrigadierManager no longer forgets the command sender

## [1.1.0] - 2020-10-24

### Added
 - ExampleVelocityPlugin
 - CloudInjectionModule to cloud-velocity
 - PlayerArgument to cloud-velocity
 - TextColorArgument to minecraft-extras
 - LocationArgument to cloud-bukkit
 - ServerArgument to cloud-velocity
 - LockableCommandManager to cloud-core
 - VelocityCommandPreprocessor to cloud-velocity
 - PlayerArgument to cloud-bungee
 - ServerArgument to cloud-bungee
 - ExampleBungeePlugin
 - CaptionKeys to cloud-bungee
 - BungeeCommandPreprocessor to cloud-bungee
 - Named suggestion providers
 - PircBotX implementation

### Changed
 - Allow for combined presence flags, such that `-a -b -c` is equivalent to `-abc`
 - Allow for class annotations as a default for when an annotation is not present on a method
 - Allow for annotated annotations
 
### Fixed
 - Arguments with no required children not being executors (cloud-brigadier)

## [1.0.2] - 2020-10-18

### Fixed
 - Quoted parsing in StringArgument
 - Wrong suggestions following invalid literals
 - Chained optionals not allowing the command to be executed when more than one optional is omitted

### Changed
 - Update adventure-api from 4.0.0 to 4.1.1
 - Update Velocity module for breaking API changes (sendMessage needs an Identity)

## [1.0.1] - 2020-10-14

### Changes
 - Switch from a snapshot to a release version of adventure 4.0.0
 - Add `Identity.nil()` when sending adventure messages
