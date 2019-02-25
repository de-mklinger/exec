Execute external processes from Java
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
    <version>1.3</version>
</dependency>
```

The following code example executes the command ```ls -l``` in the directory
```/etc``` storing the output in the string ```output```:

```java
String output = CmdOutputUtil.executeForStdout(
    new CmdBuilder("ls")
    .arg("-l")
    .directory(new File("/etc")));
```


Ever used java.lang.Process? 
--

It is surprisingly hard to execute a command line program from Java compared
to other programming languages, especially if the program produces output --
whether you are interested in the output or not.

### Stdout, stderr and full buffers

When a program produces output on stdout or stderr, this output is usually
buffered on the OS level up to a certain amount. When the buffer is full
but the program still produces output, it is possible that the program blocks.
To avoid this, it is required to read or discard all output from the program.

### Reading output requires threads

To read output from a program while waiting for it to exit, we need to use
threads in Java. As we have two output streams (stdout and stderr), we need
at least two threads. This library handles all required threading to read
the program's output automatically. Threads are re-used by default.


Exec Features
--

### Reversed semantics for stdout and stderr

The semantics for accessing stdout and stderr of the program executed are 
reversed in comparison to java.lang.Process. Instead of pulling the output
of a program using an InputStream, you let Exec push data to an OutputStream
of your choice or directly to a file. In case of a files, no additional 
threads are required. When using streams, Exec does all the work of using 
threads to read from the program's output and writing it to your stream.

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
