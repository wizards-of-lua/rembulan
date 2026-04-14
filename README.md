# Rembulan

(*Rembulan* is Javanese/Indonesian for *Moon*.)


## About
> **Important Notice:** This repository is a fork of the [Rembulan repository](https://github.com/mjanicek/rembulan) by [Miroslav Janíček](https://github.com/mjanicek), which appears to have been abandoned. This is **not** an official successor of the original project. It exists solely so I can use it in the [Wizards of Lua](https://codeberg.org/mickkay/Wizards-of-Lua-Fabric) Minecraft mod. However, all changes made to this repo are free to use in accordance with the original license. If you find bugs, feel free to [create an issue in this repo](https://github.com/wizards-of-lua/rembulan/issues).

Rembulan is an implementation of Lua 5.3 for the Java Virtual Machine (JVM), written in
pure Java with minimal dependencies.
The goal of the Rembulan project is to develop a correct, complete and scalable Lua
implementation for running sandboxed Lua programs on the JVM.

Rembulan implements Lua 5.3 as specified by the
[Lua Reference Manual](http://www.lua.org/manual/5.3/manual.html), explicitly attempting to mimic
the behaviour of PUC-Lua whenever possible. This includes language-level features (such
as metamethods and coroutines) and the standard library.

## Using Rembulan

Rembulan requires a Java Runtime Environment (JRE) version 8 or higher.

### Documentation

Generated JavaDocs are available online:

 * [Runtime module](https://mjanicek.github.io/rembulan/apidocs/rembulan-runtime/index.html)
 * [Compiler](https://mjanicek.github.io/rembulan/apidocs/rembulan-compiler/index.html)
 * [Standard Library](https://mjanicek.github.io/rembulan/apidocs/rembulan-stdlib/index.html)

There are also a few short texts in the `doc` folder:

 * [How are coroutines implemented?](doc/CoroutinesOverview.md)
 * [Overview of the Rembulan compiler](doc/CompilerOverview.md)
 * [Rembulan completeness table](doc/CompletenessTable.md)

### Building from source

To build Rembulan, you will need the following:

 * Java Development Kit (JDK) version 8 or higher
 * Gradle version 8.10 or higher

Gradle will pull in the remaining dependencies as part of the build process.

To fetch the latest code on the `master` branch and build it, run

```sh
git clone https://github.com/wizards-of-lua/rembulan.git
cd rembulan    
gradlew clean build publishToMavenLocal
```

This will build all modules, run tests and finally install all artifacts into your local
Maven repository.    

### Using Rembulan from Gradle

First, publish the artifacts to your local Maven repository:

```sh
./gradlew publishToMavenLocal
```

Then add `mavenLocal()` to your repositories and declare the desired dependencies in your
`build.gradle`:

To include the **runtime** as a dependency:

```groovy
repositories {
    mavenLocal()
}

dependencies {
    implementation 'net.wizardsoflua.rembulan:rembulan-runtime:0.4.1'
}
```

To include the **compiler** as a dependency:

```groovy
dependencies {
    implementation 'net.wizardsoflua.rembulan:rembulan-compiler:0.4.1'
}
```

To include the **standard library** as a dependency:

```groovy
dependencies {
    implementation 'net.wizardsoflua.rembulan:rembulan-stdlib:0.4.1'
}
```

Note that `rembulan-compiler` and `rembulan-stdlib` both pull in `rembulan-runtime` as
a dependency, but are otherwise independent.

## Getting started

Rembulan compiles Lua functions into Java classes and loads them into the JVM;
the compiler performs a type analysis of the Lua programs in order to generate a more
tightly-typed code whenever feasible.

Since the JVM does not directly support coroutines, Rembulan treats Lua functions as state
machines and controls their execution (i.e., yields, resumes and pauses) using exceptions.
Since the Rembulan runtime retains control of the control state, this technique is also used
to implement CPU accounting and scheduling of asynchronous operations.

#### Example: Hello world  

The following snippet loads the Lua program `print('hello world!')`, compiles it, loads
it into a (non-sandboxed) state, and runs it:

(From [`rembulan-examples/.../HelloWorld.java`](rembulan-examples/src/main/java/net/sandius/rembulan/examples/HelloWorld.java))

```java
String program = "print('hello world!')";

// initialise state
StateContext state = StateContexts.newDefaultInstance();
Table env = StandardLibrary.in(RuntimeEnvironments.system()).installInto(state);

// compile
ChunkLoader loader = CompilerChunkLoader.of("hello_world");
LuaFunction main = loader.loadTextChunk(new Variable(env), "hello", program);

// execute
DirectCallExecutor.newExecutor().call(state, main);
```

The output (printed to `System.out`) is:

```
hello world!
```

#### Example: CPU accounting

Lua functions can be called in a mode that automatically pauses their execution once the
given number of operations has been performed:

(From [`rembulan-examples/.../InfiniteLoop.java`](rembulan-examples/src/main/java/net/sandius/rembulan/examples/InfiniteLoop.java))

```java
String program = "n = 0; while true do n = n + 1 end";

// initialise state
StateContext state = StateContexts.newDefaultInstance();
Table env = StandardLibrary.in(RuntimeEnvironments.system()).installInto(state);

// compile
ChunkLoader loader = CompilerChunkLoader.of("infinite_loop");
LuaFunction main = loader.loadTextChunk(new Variable(env), "loop", program);

// execute at most one million ops
DirectCallExecutor executor = DirectCallExecutor.newExecutorWithTickLimit(1000000);

try {
    executor.call(state, main);
    throw new AssertionError();  // never reaches this point!
}
catch (CallPausedException ex) {
    System.out.println("n = " + env.rawget("n"));
}
```

Prints:

```
n = 199999
```

The [`CallPausedException`](https://mjanicek.github.io/rembulan/apidocs/rembulan-runtime/net/sandius/rembulan/exec/CallPausedException.html) contains a *continuation* of the call. The call can be resumed:
the pause is transparent to the Lua code, and the loop does not end with an error (it is merely
paused).

#### Further examples

For further examples, see the classes in
[`rembulan-examples/src/main/java/net/sandius/rembulan/examples`](rembulan-examples/src/main/java/net/sandius/rembulan/examples).

### Project structure

Rembulan is a multi-module Gradle build, consisting of the following modules that are deployed
to a Maven repository:

 * `rembulan-runtime` ... the core classes and runtime;
 * `rembulan-compiler` ... a compiler of Lua sources to Java bytecode;
 * `rembulan-stdlib` ... the Lua standard library;
 * `rembulan-standalone` ... standalone REPL, a (mostly) drop-in replacement for the `lua` command from PUC-Lua.

There are also auxiliary modules that are not deployed:

 * `rembulan-tests` ... project test suite, including benchmarks from the Benchmarks Game;
 * `rembulan-examples` ... examples of the Rembulan API.                       

## License

Rembulan is licensed under the Apache License Version 2.0. See the file
[LICENSE.txt](LICENSE.txt) for details.
