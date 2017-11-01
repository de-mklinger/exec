exec: Execute external processes without hassle
====

[![Maven Central](https://img.shields.io/maven-central/v/de.mklinger.commons/exec.svg)](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22de.mklinger.commons%22%20AND%20a%3A%22exec%22)


What is it?
--

Exec is a tiny Java library with minimal dependencies to let Java
developers execute other programs whenever they need them, embracing
java.lang.Process and ProcessBuilder, bringing them to a higher level.

Exec is in production use in various projects since 2013.


Usage
--

Maven dependency:

```xml
<dependency>
    <groupId>de.mklinger.commons</groupId>
    <artifactId>exec</artifactId>
    <version>0.32</version>
</dependency>
```

The following code example executes the command ```ls -l``` in the directory
```/etc``` storing the output in the string ```output```:

```java
ByteArrayOutputStream stdout = new ByteArrayOutputStream();
new CmdBuilder("ls")
    .arg("-l")
    .directory(new File("/etc"))
    .stdout(stdout)
    .toCmd()
    .execute();
String output = stdout.toString();
```


Ever used java.lang.Process? 
--

It is surprisingly hard to execute a command line program from Java compared
to other programming languages, especially if the program produces output --
whether you are interested in the output or not.

[TBD: talk about jlp being especially bad with stdout/stderr]

[TBD: talk about threads needed to consume stdout/stderr]

[TBD: talk about /dev/null]

[TBD: talk about waiting for a program to end with jlp]


Exec Features
--

### Reversed semantics for stdout and stderr

The semantics for accessing stdout and stderr of the program executed are 
reversed in comparison to java.lang.Process. Instead of pulling the output
of a program using an InputStream, you let Exec push data data to an
OutputStream of your choice or directly to a file. In case of a files, no
additional threads are required. When using streams, Exec does all the work
of using threads to read from the program's output and writing it to your
stream.

### Auto /dev/null

If you don't provide targets for stdout/stderr, the data will automatically
be redirected to ```/dev/null``` (or ```NUL``` on Windows). No need for
additional threads. No stuck programs because of full OS buffers.

[TBD: talk about one-liners for simple calls]

[TBD: talk about sophisticated exception handling]

[TBD: talk about thread safety]

### Java "fork"

Start another Java program using the VM installation your program is 
currently running in. Full support for system properties and memory settings
included.


License
--

Exec is licensed under [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0)
