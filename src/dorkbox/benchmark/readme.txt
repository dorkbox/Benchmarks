These tests aren't to describe "real world" performance, but to derive comparisons between different implementations, and because of
something called OSR, you CANNOT depend on these tests to describe what they are testing in the "real world".


see:
http://psy-lob-saw.blogspot.de/2016/02/wait-for-it-counteduncounted-loops.html
http://www.cliffc.org/blog/2011/11/22/what-the-heck-is-osr-and-why-is-it-bad-or-good/

We can, however try to make this as "real world" as possible by telling the JVM to compile the bytecode. Use these VM arguments.
  -XX:-TieredCompilation -Xcomp -server

TODO: replace these tests with JMH for more accurate tests
