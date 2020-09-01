# commands

This is going to be a general-purpose Java command library. It will allow programmers
to define command chains that users can use to execute pre-defined actions.

The code is based on a paper that can be found [here](https://github.com/Sauilitired/Sauilitired/blob/master/AS_2020_09_Commands.pdf).

## Goals

- Allow for commands to be defined using builder patterns
- Allow for commands to be defined using annotated methods
- Allow for command pre-processing
- Allow for command suggestion outputs

Once the core functionality is present additional goals are:

- Create a Minecraft specific implementation and add appropriate bindings
- Create a Discord implementation
- Create a Java CLI implementation
