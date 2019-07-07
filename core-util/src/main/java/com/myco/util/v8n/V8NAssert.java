package com.myco.util.v8n;

import com.myco.util.values.ErrorMessage;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Map;

public class V8NAssert {

  /**
   * Verify the value is not blank/null; if in error, return an ErrorMessage in the Optional wrapper where
   * {@link ErrorMessage#getCode()} returns the given name.
   *
   * @param value
   *          The value to check.
   * @param name
   *          The field/attribute name that will be provided as the code of the ErrorMessage, if the value is invalid.
   *
   * @return null when the given input is valid, an ErrorMessage otherwise.
   */
  public static ErrorMessage hasText(String value, String name) {
    return hasText(value, name, null);
  }

  /**
   * Verify the value is not blank/null; if in error, return an ErrorMessage in the Optional wrapper where
   * {@link ErrorMessage#getCode()} returns the given name.
   *
   * @param value
   *          The value to check.
   * @param name
   *          The field/attribute name that will be provided as the code of the ErrorMessage, if the value is invalid.
   * @param index
   *          for collection based input; the position/index within the collection
   *
   * @return null when the given input is valid, an ErrorMessage otherwise.
   */
  public static ErrorMessage hasText(String value, String name, Object index) {
    boolean valid = StringUtils.hasText(value);
    return valid ? null : new ErrorMessage.Builder().code(name).message("null/blank value").index(index).build();
  }


  /**
   * Verify the given value is not null/blank and that it doesn't include any leading or trailing whitespace.
   *
   * @param value
   *          The value to check.
   * @param name
   *          The field/attribute name that will be provided as the code of the ErrorMessage, if the value is invalid.
   * @return null when the given input is valid, an ErrorMessage otherwise.
   */
  public static ErrorMessage requiredInput(String value, String name) {
    return requiredInput(value, name, null);
  }


  /**
   * Verify the given value is not null/blank and that it doesn't include any leading or trailing whitespace.
   *
   * @param value
   *          The value to check.
   * @param name
   *          The field/attribute name that will be provided as the code of the ErrorMessage, if the value is invalid.
   * @param index
   *          for collection based input; the position/index within the collection
   * @return null when the given input is valid, an ErrorMessage otherwise.
   */
  public static ErrorMessage requiredInput(String value, String name, Object index) {
    boolean hasText = StringUtils.hasText(value);
    boolean valid = hasText && value.length() == value.trim().length();
    return valid ? null
        : new ErrorMessage.Builder().code(name)
            .message(
                !hasText ? "null/blank required input" : "required input must not includes leading/trailing whitespace")
            .index(index).build();

  }

  /**
   * Verify the given collection of values is not empty and that none include any leading or trailing whitespace.
   *
   * @param value
   *          The value to check.
   * @param name
   *          The field/attribute name that will be provided as the code of the ErrorMessage, if the value is invalid.
   * @return null when the given input is valid, an ErrorMessage otherwise.
   */
  public static ErrorMessage requiredInput(Collection<String> value, String name) {
    ErrorMessage.Builder errorMsgBuilder = new ErrorMessage.Builder().code(name).message("input errors detected");
    ErrorMessage emsg = notEmpty(value, name);
    if (emsg == null) {
      // when notEmpty() didn't add an error, check the values in the collection.
      int i = 0;
      for (String val : value) {
        errorMsgBuilder.detail(requiredInput(val, name, i++));
      }
    }
    else {
      errorMsgBuilder.detail(emsg);
    }
    return errorMsgBuilder.buildIfDetailsPresent();
  }


  /**
   * Verify the given value, if not null/blank, doesn't include any leading or trailing whitespace.
   *
   * @param value
   *          The value to check.
   * @param name
   *          The field/attribute name that will be provided as the code of the ErrorMessage, if the value is invalid.
   * @return null when the given input is valid, an ErrorMessage otherwise.
   */
  public static ErrorMessage optionalInput(String value, String name) {
    return optionalInput(value, name, null);
  }


  /**
   * Verify the given value, if not null/blank, doesn't include any leading or trailing whitespace.
   *
   * @param value
   *          The value to check.
   * @param name
   *          The field/attribute name that will be provided as the code of the ErrorMessage, if the value is invalid.
   * @param index
   *          for collection based input; the position/index within the collection
   * @return null when the given input is valid, an ErrorMessage otherwise.
   */
  public static ErrorMessage optionalInput(String value, String name, Object index) {
    boolean hasText = StringUtils.hasText(value);
    boolean valid = !hasText || value.length() == value.trim().length();
    return valid ? null
        : new ErrorMessage.Builder().code(name).index(index)
            .message("optional input must not includes leading/trailing whitespace").build();
  }

  /**
   * Verify the given collection of values is does not include any with leading or trailing whitespace.
   *
   * @param value
   *          The value to check.
   * @param name
   *          The field/attribute name that will be provided as the code of the ErrorMessage, if the value is invalid.
   * @return null when the given input is valid, an ErrorMessage otherwise.
   */
  public static ErrorMessage optionalInput(Collection<String> value, String name) {
    if (CollectionUtils.isEmpty(value)) return null;
    ErrorMessage.Builder errorMsgBuilder = new ErrorMessage.Builder().code(name).message("input errors detected");
    int i = 0;
    for (String val : value) {
      errorMsgBuilder.detail(optionalInput(val, name, i++));
    }
    return errorMsgBuilder.buildIfDetailsPresent();
  }


  /**
   * Verify the given boolean value evaluates to true.
   *
   * @param value
   *          The value to check.
   * @param name
   *          The field/attribute name that will be provided as the code of the ErrorMessage, if the value is invalid.
   * @param errorMessage
   *          The error message to include if the given value does not evaluate to true
   * @return null when the given input is valid, an ErrorMessage otherwise.
   */
  public static ErrorMessage isTrue(boolean value, String name, String errorMessage) {
    return value ? null : new ErrorMessage.Builder().code(name).message(errorMessage).build();
  }


  /**
   * Verify the given boolean value evaluates to true.
   *
   * @param value
   *          The value to check.
   * @param name
   *          The field/attribute name that will be provided as the code of the ErrorMessage, if the value is invalid.
   * @param errorMessage
   *          The error message to include if the given value does not evaluate to true
   * @param index
   *          for collection based input; the position/index within the collection
   * @return null when the given input is valid, an ErrorMessage otherwise.
   */
  public static ErrorMessage isTrue(boolean value, String name, String errorMessage, Object index) {
    return value ? null : new ErrorMessage.Builder().code(name).index(index).message(errorMessage).build();
  }


  /**
   * Verify the given map (value) is not empty.
   *
   * @param value
   *          The {@link Map} to check.
   * @param name
   *          The field/attribute name that will be provided as the code of the ErrorMessage, if the value is invalid.
   * @return null when the given input is valid, an ErrorMessage otherwise.
   */
  public static ErrorMessage notEmpty(Map<?, ?> value, String name) {
    return notEmpty(value, name, 0);
  }


  /**
   * Verify the given map (value) is not empty.
   *
   * @param value
   *          The {@link Map} to check.
   * @param name
   *          The field/attribute name that will be provided as the code of the ErrorMessage, if the value is invalid.
   * @param index
   *          for collection based input; the position/index within the collection
   * @return null when the given input is valid, an ErrorMessage otherwise.
   */
  public static ErrorMessage notEmpty(Map<?, ?> value, String name, Object index) {
    return !CollectionUtils.isEmpty(value) ? null
        : new ErrorMessage.Builder().code(name).index(index).message("empty map").build();
  }


  /**
   * Verify the given collection (value) is not empty.
   *
   * @param value
   *          The {@link Collection} to check.
   * @param name
   *          The field/attribute name that will be provided as the code of the ErrorMessage, if the value is invalid.
   * @return null when the given input is valid, an ErrorMessage otherwise.
   */
  public static ErrorMessage notEmpty(Collection<?> value, String name) {
    return notEmpty(value, name, 0);
  }


  /**
   * Verify the given collection (value) is not empty.
   *
   * @param value
   *          The {@link Collection} to check.
   * @param name
   *          The field/attribute name that will be provided as the code of the ErrorMessage, if the value is invalid.
   * @param index
   *          for collection based input; the position/index within the collection
   * @return null when the given input is valid, an ErrorMessage otherwise.
   */
  public static ErrorMessage notEmpty(Collection<?> value, String name, Object index) {
    return !CollectionUtils.isEmpty(value) ? null
        : new ErrorMessage.Builder().code(name).index(index).message("empty collection").build();
  }


  /**
   * Verify the given value is not null.
   *
   * @param value
   *          The value to check.
   * @param name
   *          The field/attribute name that will be provided as the code of the ErrorMessage, if the value is invalid.
   * @return null when the given input is valid, an ErrorMessage otherwise.
   */
  public static ErrorMessage notNull(Object value, String name) {
    return notNull(value, name, 0);
  }


  /**
   * Verify the given value is not null.
   *
   * @param value
   *          The value to check.
   * @param name
   *          The field/attribute name that will be provided as the code of the ErrorMessage, if the value is invalid.
   * @param index
   *          for collection based input; the position/index within the collection
   * @return null when the given input is valid, an ErrorMessage otherwise.
   */
  public static ErrorMessage notNull(Object value, String name, Object index) {
    return (value != null) ? null
        : new ErrorMessage.Builder().code(name).message("required value must not be null").build();
  }

  /**
   * Verify the given tracking number value is a valid with respect to it's size: it must be 12, 15, 20 OR 22 characters
   * long. The value is not required, but if
   *
   * @param value
   *          The value to check.
   * @param name
   *          The field/attribute name that will be provided as the code of the ErrorMessage, if the value is invalid.
   * @return null when the given input is valid, an ErrorMessage otherwise.
   */
  public static ErrorMessage assertTrackingNumberIsLogical(String value, String name) {
    return assertTrackingNumberIsLogical(value, name, 0);
  }

  public static ErrorMessage assertTrackingNumberIsLogical(String value, String name, Object index) {

    ErrorMessage emsg = optionalInput(value, name);
    if (emsg == null && StringUtils.hasText(value)) {
      int valueLength = value.length();
      boolean nonDigitChars = false;
      for (char c : value.toCharArray()) {
        if (!Character.isDigit(c)) {
          nonDigitChars = true;
          break;
        }
      }
      boolean invalidLength = false;
      if (valueLength != 12 && valueLength != 15 && valueLength != 20 && valueLength != 22) {
        invalidLength = true;
      }

      if (nonDigitChars && invalidLength) {
        emsg = new ErrorMessage.Builder().code(name).index(index)
            .message(String.format("%s and %s", INVALID_TRK_NO_LENGTH_MSG, NON_DIGIT_TRK_NO_CHAR_MSG.toLowerCase()))
            .build();
      }
      else if (nonDigitChars) {
        emsg = new ErrorMessage.Builder().code(name).index(index).message(NON_DIGIT_TRK_NO_CHAR_MSG).build();
      }
      else if (invalidLength) {
        emsg = new ErrorMessage.Builder().code(name).index(index).message(INVALID_TRK_NO_LENGTH_MSG).build();
      }
    }
    return emsg;
  }

  private static String INVALID_TRK_NO_LENGTH_MSG = "Invalid length (must be 12, 15, 20 or 22)";
  private static String NON_DIGIT_TRK_NO_CHAR_MSG =
      "Contains 1 or more invalid characters (must include only numeric digits)";
}
