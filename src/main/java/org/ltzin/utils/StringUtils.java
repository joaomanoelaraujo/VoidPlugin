package org.ltzin.utils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
  
  private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,###");
  private static final Pattern COLOR_PATTERN = Pattern.compile("(?i)(§)[0-9A-FK-OR]");

  public static String formatNumber(int number) {
    return DECIMAL_FORMAT.format(number);
  }

  public static String formatNumber(long number) {
    return DECIMAL_FORMAT.format(number);
  }

  public static String formatNumber(double number) {
    return DECIMAL_FORMAT.format(number);
  }

  public static String stripColors(final String input) {
    if (input == null) {
      return null;
    }
    
    return COLOR_PATTERN.matcher(input).replaceAll("");
  }

  public static String formatColors(String textToFormat) {
    return translateAlternateColorCodes('&', textToFormat);
  }

  public static String deformatColors(String textToDeFormat) {
    Matcher matcher = COLOR_PATTERN.matcher(textToDeFormat);
    while (matcher.find()) {
      String color = matcher.group();
      textToDeFormat = textToDeFormat.replaceFirst(Pattern.quote(color), Matcher.quoteReplacement("&" + color.substring(1)));
    }
    
    return textToDeFormat;
  }

  public static String translateAlternateColorCodes(char altColorChar, String textToTranslate) {
    Pattern pattern = Pattern.compile("(?i)(" + altColorChar + ")[0-9A-FK-OR]");
    
    Matcher matcher = pattern.matcher(textToTranslate);
    while (matcher.find()) {
      String color = matcher.group();
      textToTranslate = textToTranslate.replaceFirst(Pattern.quote(color), Matcher.quoteReplacement("§" + color.substring(1)));
    }
    
    return textToTranslate;
  }

  public static String getFirstColor(String input) {
    Matcher matcher = COLOR_PATTERN.matcher(input);
    String first = "";
    if (matcher.find()) {
      first = matcher.group();
    }
    
    return first;
  }

  public static String getLastColor(String input) {
    Matcher matcher = COLOR_PATTERN.matcher(input);
    String last = "";
    while (matcher.find()) {
      last = matcher.group();
    }
    
    return last;
  }

  public static String repeat(String repeat, int amount) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < amount; i++) {
      sb.append(repeat);
    }
    
    return sb.toString();
  }
  

  public static <T> String join(T[] array, int index, String separator) {
    StringBuilder joined = new StringBuilder();
    for (int slot = index; slot < array.length; slot++) {
      joined.append(array[slot].toString()).append(slot + 1 == array.length ? "" : separator);
    }
    
    return joined.toString();
  }

  public static <T> String join(T[] array, String separator) {
    return join(array, 0, separator);
  }
  

  public static <T> String join(Collection<T> collection, String separator) {
    return join(collection.toArray(new Object[0]), separator);
  }

  public static String[] split(String toSplit, int length) {
    return split(toSplit, length, false);
  }

  public static String capitalise(String toCapitalise) {
    StringBuilder result = new StringBuilder();
    
    String[] splittedString = toCapitalise.split(" ");
    for (int index = 0; index < splittedString.length; index++) {
      String append = splittedString[index];
      result.append(append.substring(0, 1).toUpperCase()).append(append.substring(1).toLowerCase()).append(index + 1 == splittedString.length ? "" : " ");
    }
    
    return result.toString();
  }
  

  public static String[] split(String toSplit, int length, boolean ignoreIncompleteWords) {
    StringBuilder result = new StringBuilder(), current = new StringBuilder();
    
    char[] arr = toSplit.toCharArray();
    for (int charId = 0; charId < arr.length; charId++) {
      char character = arr[charId];
      if (current.length() == length) {
        if (!ignoreIncompleteWords) {
          List<Character> removedChars = new ArrayList<>();
          for (int l = current.length() - 1; l > 0; l--) {
            if (current.charAt(l) == ' ') {
              current.deleteCharAt(l);
              result.append(current).append("\n");
              Collections.reverse(removedChars);
              current = new StringBuilder(join(removedChars, ""));
              break;
            }
            
            removedChars.add(current.charAt(l));
            current.deleteCharAt(l);
          }
          
          removedChars.clear();
        } else {
          result.append(current).append("\n");
          current = new StringBuilder();
        }
      }
      
      current.append(current.length() == 0 && character == ' ' ? "" : character);
      if (charId + 1 == arr.length) {
        result.append(current).append("\n");
      }
    }
    
    return result.toString().split("\n");
  }
}
