package org.ltzin.plugin;

import org.ltzin.logger.VLogger;

import java.io.*;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

/**
 * Escreve uma YAML config sem quebra de linhas, preservando comentarios
 * por chave (via {@link YamlEntryInfo} nos campos que a classe Language
 * escreve com base em reflection).
 */
public class VWriter {

  private final VLogger logger;
  private final File file;
  private final String header;
  private final Map<String, Object> keys = new LinkedHashMap<>();

  public VWriter(VLogger logger, File file) {
    this(logger, file, "");
  }

  public VWriter(VLogger logger, File file, String header) {
    this.logger = logger;
    this.file = file;
    this.header = header;
  }

  public void write() {
    try {
      if (this.file.getParentFile() != null && !this.file.getParentFile().exists()) {
        this.file.getParentFile().mkdirs();
      }
      Writer fw = new OutputStreamWriter(new FileOutputStream(this.file), StandardCharsets.UTF_8);
      fw.append(this.toSaveString());
      fw.close();
    } catch (IOException ex) {
      this.logger.log(Level.SEVERE, "Erro ao escrever o arquivo \"" + this.file.getName() + "\": ", ex);
    }
  }

  @SuppressWarnings("unchecked")
  public void set(String path, YamlEntry entry) {
    String[] splitter = path.split("\\.");

    Map<String, Object> currentMap = this.keys;
    for (int slot = 0; slot < splitter.length; slot++) {
      String p = splitter[slot];
      if (slot + 1 == splitter.length) {
        currentMap.put(p, entry);
      } else {
        if (currentMap.containsKey(p)) {
          currentMap = (Map<String, Object>) currentMap.get(p);
        } else {
          currentMap.put(p, new LinkedHashMap<String, Object>());
          currentMap = (Map<String, Object>) currentMap.get(p);
        }
      }
    }
  }

  public String toSaveString() {
    StringBuilder join = new StringBuilder();
    if (!this.header.isEmpty()) {
      for (String split : this.header.split("\n")) {
        for (String annotation : wrap(split, 100)) {
          join.append("# ").append(annotation).append("\n");
        }
      }
    }

    for (Entry<String, Object> entry : this.keys.entrySet()) {
      join.append(toSaveString(entry.getKey(), entry.getValue(), 0));
    }

    return join.toString();
  }

  @SuppressWarnings("unchecked")
  private String toSaveString(String key, Object object, int spaces) {
    StringBuilder join = new StringBuilder();
    if (object instanceof YamlEntry) {
      YamlEntry ye = (YamlEntry) object;
      if (!ye.getAnnotation().isEmpty()) {
        for (String split : ye.getAnnotation().split("\n")) {
          for (String annotation : wrap(split, 100)) {
            join.append(repeat(spaces)).append("# ").append(annotation).append("\n");
          }
        }
      }

      object = ye.getValue();
    }

    join.append(repeat(spaces)).append(key).append(":");
    if (object instanceof String) {
      join.append(" '").append(object.toString().replace("'", "''")).append("'\n");
    } else if (object instanceof Integer) {
      join.append(" ").append(object).append("\n");
    } else if (object instanceof Double) {
      join.append(" ").append(object).append("\n");
    } else if (object instanceof Long) {
      join.append(" ").append(object).append("\n");
    } else if (object instanceof Boolean) {
      join.append(" ").append(object).append("\n");
    } else if (object instanceof List) {
      join.append("\n");
      for (Object obj : (List<?>) object) {
        if (obj instanceof Integer) {
          join.append(repeat(spaces)).append("- ").append(obj).append("\n");
        } else {
          join.append(repeat(spaces)).append("- '").append(obj.toString().replace("'", "''")).append("'\n");
        }
      }
    } else if (object instanceof Map) {
      join.append("\n");
      for (Entry<String, Object> entry : ((Map<String, Object>) object).entrySet()) {
        join.append(toSaveString(entry.getKey(), entry.getValue(), spaces + 1));
      }
    } else if (object instanceof InputStream) {
      join.append("\n");
      try {
        BufferedReader reader = new BufferedReader(new InputStreamReader((InputStream) object, StandardCharsets.UTF_8));
        String line;
        while ((line = reader.readLine()) != null) {
          join.append(repeat(spaces + 1)).append(line).append("\n");
        }
      } catch (IOException ex) {
        this.logger.log(Level.SEVERE, "Erro ao ler a InputStream \"" + key + "\":", ex);
      }
    }

    return join.toString();
  }

  private String repeat(int spaces) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < spaces; i++) {
      sb.append(" ");
    }
    return sb.toString();
  }

  /**
   * Quebra uma string em blocos de ate {@code width} caracteres,
   * respeitando limites de palavra (nao corta no meio de uma palavra).
   */
  private static List<String> wrap(String text, int width) {
    List<String> lines = new java.util.ArrayList<>();
    if (text == null || text.isEmpty()) {
      lines.add("");
      return lines;
    }

    StringBuilder current = new StringBuilder();
    for (String word : text.split(" ")) {
      if (current.length() > 0 && current.length() + 1 + word.length() > width) {
        lines.add(current.toString());
        current = new StringBuilder();
      }
      if (current.length() > 0) {
        current.append(" ");
      }
      current.append(word);
    }
    if (current.length() > 0) {
      lines.add(current.toString());
    }
    return lines;
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.FIELD)
  public @interface YamlEntryInfo {
    String annotation() default "";
  }

  public static class YamlEntry {

    private final String annotation;
    private final Object value;

    public YamlEntry(Object[] array) {
      this.annotation = (String) array[0];
      this.value = array[1];
    }

    public String getAnnotation() {
      return annotation;
    }

    public Object getValue() {
      return value;
    }
  }
}