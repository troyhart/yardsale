package com.myco.util.slf4j;

import org.slf4j.MDC;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * Wraps slf4j's {@link MDC} with an {@link AutoCloseable auto closable}, instance based (i.e. non-static) API. This
 * allows for almost brain-less use of the MDC--no need to {@link MDC#remove(String)}. Simply instantiate an instance of
 * this class within a try-with-resources statement and then {@link #put(String, String) put} context entries as you
 * please within the try block. You can manually {@link #remove(String) remove} if you need also. {@link #close()} will
 * remove every <code>put</code> value and will reinstate every value from the parent context.
 *
 * <p>
 * Simple code sample...
 * </p>
 *
 * <pre>
 * try (MdcAutoClosable acMDC = new MdcAutoClosable()) {
 *   acMDC.put("KEY1", "val1").put("KEY2", "val2");
 *
 *   // ...
 *
 *   // You can remove anything you want from the auto-closable context.
 *   // NOTE: upon close() the value
 *   acMDC.remove("WHATEVER");
 *
 * } // acMDC.close() is automatically called
 * </pre>
 *
 * <p>
 * NOTE: this class is not meant to be thread safe; it doesn't make any sense for it to need to be thread safe however
 * because the MDC is a thread bound construct.
 *
 * @author troyh
 */
public final class MdcAutoClosable implements AutoCloseable {
  private Set<String> keys;
  private Map<String, String> overridden;

  /**
   * A new instance with no context entries.
   */
  public MdcAutoClosable() {
    keys = new HashSet<String>();
    overridden = new HashMap<>();
  }

  /**
   * @param context initial context mapping, each entry is {@link #put(String, String)} onto this instance.
   */
  public MdcAutoClosable(Map<String, String> context) {
    this();
    if (context == null) {
      // should use the other constructor if not initializing from another context.
      throw new NullPointerException("Null context.");
    }

    for (Map.Entry<String, String> me : context.entrySet()) {
      // Only valid to invoke from the constructor because this class, and therefore the put() method, is final!!!
      put(me.getKey(), me.getValue());
    }
  }

  /**
   * A new instance with a single initial entry.
   *
   * @param key   initial entry key
   * @param value initial entry value
   */
  public MdcAutoClosable(String key, Object value) {
    this();
    // Only valid to invoke from the constructor because this class, and therefore the put() method, is final!!!
    put(key, value);
  }

  /**
   * Each MDC entry {@link #put(String, String) put via this instance} (but not {@link #remove(String) removed via this
   * instance}) will be {@link MDC#remove(String) removed from the MDC} upon invocation of this method. Also, each value
   * overridden from the parent context will be re-instated.
   */
  @Override
  public void close() {
    if (keys != null) {
      for (String key : keys) {
        // remove entry from overridden as we go...
        String overriddenVal = overridden.remove(key);
        if (overriddenVal == null) {
          MDC.remove(key);
        }
        else {
          // overridden value re-instated.
          MDC.put(key, overriddenVal);
        }
      }
      // pick up the stragglers, which may or may not be present!
      // * see put() and remove() for more details on this point.
      for (String key : overridden.keySet()) {
        MDC.put(key, overridden.get(key));
      }
      keys = null;
      overridden = null;
    }
  }

  /**
   * {@link MDC#put(String, String) Puts} the key/value pair on the {@link MDC}, remembering the key and parent value in
   * order to {@link MDC#remove(String) remove} from the {@link MDC} on {@link #close()} and reinstate the the parent
   * context value.
   *
   * <p>
   * putting a null value is equivalent to {@link #remove(String)}.
   *
   * @param key
   * @param value
   */
  public MdcAutoClosable put(String key, String value) {
    Assert.hasText(key, "Invalid null/blank MDC key!");

    handleOverride(key);

    if (value == null) {
      MDC.remove(key);
    }
    else {
      keys.add(key);
      MDC.put(key, value);
    }

    return this;
  }

  public MdcAutoClosable put(String key, Object value) {
    return put(key, value == null ? null : value.toString());
  }

  /**
   * @param key a context attribute key
   * @return true if the key is defined in the local context.
   */
  public boolean isLocal(String key) {
    return keys.contains(key);
  }

  /**
   * Same as calling {@link MDC#get(String)}.
   *
   * @param key
   * @return the value from {@link MDC#get(String)}
   */
  public String get(String key) {
    return MDC.get(key);
  }

  /**
   * Locally {@link MDC#remove(String) Remove} the key/value pair from the {@link MDC}.
   *
   * @param key
   */
  public MdcAutoClosable remove(String key) {
    handleOverride(key);
    keys.remove(key);
    MDC.remove(key);
    return this;
  }

  private void handleOverride(String key) {
    // only override one time on this closable, so we don't lose the parent context.
    if (!overridden.containsKey(key) && !keys.contains(key)) {
      String prevVal = MDC.get(key);
      if (prevVal != null) {
        overridden.put(key, prevVal);
      }
    }
  }

}
