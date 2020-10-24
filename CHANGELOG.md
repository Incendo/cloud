# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
 - Added ExampleVelocityPlugin
 - Added CloudInjectionModule to cloud-velocity
 - Added PlayerArgument to cloud-velocity
 - Added TextColorArgument to minecraft-extras
 - Added LocationArgument to cloud-bukkit
 - Added ServerArgument to cloud-velocity
 - Added LockableCommandManager to cloud-core

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
