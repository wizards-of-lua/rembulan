package net.sandius.rembulan.lib;

import java.nio.file.AccessDeniedException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystemException;
import java.nio.file.NoSuchFileException;
import java.nio.file.NotDirectoryException;
import java.util.HashMap;
import java.util.Map;

import net.sandius.rembulan.LuaRuntimeException;
import net.sandius.rembulan.runtime.ReturnBuffer;

/**
 * Errors of the io library as PUC-Lua reports them: a failure as {@code nil, message, errno}
 * ({@code luaL_fileresult}), with the message and number of the C library ({@code strerror},
 * {@code errno}), and raised errors with their message only.
 *
 * <p>Java names the common errors by their exception type ({@link NoSuchFileException}, ...) and
 * reports the others with the system's message, which on POSIX systems is {@code strerror}'s;
 * the numbers are those of Linux. An error this class doesn't know keeps its message and gets
 * the number of EIO ("Input/output error").</p>
 */
final class IoErrors {

  /** {@code EIO}, for errors without a known number. */
  static final int EIO = 5;

  /** {@code EBADF}: reading from a file opened for writing only, or the other way round. */
  static final String BAD_FILE_DESCRIPTOR = "Bad file descriptor";

  private static final Map<String, Integer> ERRNO_OF_MESSAGE = new HashMap<>();

  static {
    errno(1, "Operation not permitted");
    errno(2, "No such file or directory");
    errno(5, "Input/output error");
    errno(9, BAD_FILE_DESCRIPTOR);
    errno(13, "Permission denied");
    errno(16, "Device or resource busy");
    errno(17, "File exists");
    errno(20, "Not a directory");
    errno(21, "Is a directory");
    errno(22, "Invalid argument");
    errno(24, "Too many open files");
    errno(27, "File too large");
    errno(28, "No space left on device");
    errno(29, "Illegal seek");
    errno(30, "Read-only file system");
    errno(36, "File name too long");
    errno(39, "Directory not empty");
    errno(40, "Too many levels of symbolic links");
  }

  private static void errno(int errno, String message) {
    ERRNO_OF_MESSAGE.put(message, errno);
  }

  private IoErrors() {
    // not to be instantiated
  }

  /**
   * Returns the message of the error {@code ex}, without a file name: as the C library's
   * {@code strerror} gives it for a system error.
   */
  static String message(Throwable ex) {
    if (ex instanceof NoSuchFileException) {
      return "No such file or directory";
    } else if (ex instanceof AccessDeniedException) {
      return "Permission denied";
    } else if (ex instanceof FileAlreadyExistsException) {
      return "File exists";
    } else if (ex instanceof NotDirectoryException) {
      return "Not a directory";
    } else if (ex instanceof DirectoryNotEmptyException) {
      return "Directory not empty";
    } else if (ex instanceof FileSystemException && ((FileSystemException) ex).getReason() != null) {
      return ((FileSystemException) ex).getReason();
    } else {
      String message = ex.getMessage();
      return message != null ? message : ex.getClass().getSimpleName();
    }
  }

  /**
   * Returns the number ({@code errno}) of the error {@code ex}.
   */
  static int errno(Throwable ex) {
    Integer errno = ERRNO_OF_MESSAGE.get(message(ex));
    return errno != null ? errno : EIO;
  }

  /**
   * Sets {@code buffer} to the results of a failed io function: {@code nil}, the message
   * (after the file name {@code fileName} if not {@code null}) and the number of the error
   * {@code ex}, as {@code luaL_fileresult}.
   */
  static void setFailure(ReturnBuffer buffer, Exception ex, String fileName) {
    String message = message(ex);
    buffer.setTo(null, fileName != null ? fileName + ": " + message : message, (long) errno(ex));
  }

  /**
   * Returns the error to raise for {@code ex}.
   */
  static LuaRuntimeException error(Exception ex) {
    return new LuaRuntimeException(message(ex));
  }

  /**
   * Returns the error to raise when the file {@code fileName} can't be opened, as
   * {@code io.lines}, {@code io.input} and {@code io.output} do.
   */
  static LuaRuntimeException cannotOpen(String fileName, Exception ex) {
    return new LuaRuntimeException("cannot open file '" + fileName + "' (" + message(ex) + ")");
  }

}
