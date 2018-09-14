package net.sandius.rembulan.lib;

import java.io.IOException;

public class ClosedFileException extends IOException {

  private static final long serialVersionUID = 1L;

  public ClosedFileException() {
    super("attempt to use a closed file");
  }

}
