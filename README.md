## MIX IDE

This is a fork of [MIX IDE](http://mixide.sourceforge.net/) with some
bug fixes. Perhaps, calling it version 2 is a bit ambitious, but it
was easier this way.

### Fixed bugs

The bugs fixed so far:

- `rI1` was not increasing on `MOVE`
- The register editor was not holding the reference to the registers
  after the reset.
- The memory editor was not updating actual memory.
- The NetBeans (Maven) project is set up to compile everything into one JAR,
  including dependencies.
  
