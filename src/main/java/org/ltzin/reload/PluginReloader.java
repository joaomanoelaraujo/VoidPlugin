package org.ltzin.reload;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class PluginReloader {

  private static final Logger LOGGER = Logger.getLogger("PluginReloader");

  private PluginReloader() {
  }


  public static Result reload(String pluginName) {
    return reloadChain(Collections.singletonList(pluginName)).get(0);
  }


  public static List<Result> reloadChain(List<String> pluginsInDependencyOrder) {
    long start = System.currentTimeMillis();

    List<ReloadHandle> handles = new ArrayList<>();
    List<Result> failures = new ArrayList<>();

    List<String> reverseOrder = new ArrayList<>(pluginsInDependencyOrder);
    Collections.reverse(reverseOrder);

    for (String name : reverseOrder) {
      try {
        handles.add(disableAndUnload(name));
      } catch (ReloadException ex) {
        failures.add(Result.failure(ex.getMessage()));
      }
    }


    Collections.reverse(handles);

    if (!failures.isEmpty()) {
      LOGGER.severe("[PluginReloader] Falha ao descarregar um ou mais plugins do grupo, revertendo os que ja foram desabilitados...");
      rollbackAll(handles);
      return failures;
    }

    List<Result> results = new ArrayList<>();
    boolean anyFailed = false;

    for (ReloadHandle handle : handles) {
      Result result = anyFailed
              ? Result.failure("Abortado: um plugin anterior no grupo falhou ao habilitar.")
              : loadAndEnable(handle);

      if (!result.isSuccess()) {
        anyFailed = true;
      }
      results.add(result);
    }

    if (anyFailed) {
      LOGGER.severe("[PluginReloader] Reload em grupo falhou, revertendo TODOS os plugins do grupo para a versao anterior...");
      rollbackAll(handles);
    }

    long elapsed = System.currentTimeMillis() - start;
    LOGGER.info("[PluginReloader] Reload em grupo finalizado em " + elapsed + "ms (sucesso=" + !anyFailed + ").");
    return results;
  }


  private static ReloadHandle disableAndUnload(String pluginName) throws ReloadException {
    PluginManager pluginManager = Bukkit.getPluginManager();
    Plugin plugin = pluginManager.getPlugin(pluginName);

    if (plugin == null) {
      throw new ReloadException("Plugin '" + pluginName + "' nao esta carregado no servidor.");
    }
    if (!(plugin instanceof JavaPlugin)) {
      throw new ReloadException("Plugin '" + pluginName + "' nao e um JavaPlugin (nao pode ser recarregado por essa rotina).");
    }

    String exactName = plugin.getName();

    File jarFile;
    try {
      jarFile = getPluginFile((JavaPlugin) plugin);
    } catch (Exception ex) {
      throw new ReloadException("Nao foi possivel localizar o .jar de '" + exactName + "': " + ex.getMessage());
    }

    if (jarFile == null || !jarFile.exists()) {
      throw new ReloadException("O .jar de '" + exactName + "' nao existe mais em disco (" + jarFile + ").");
    }

    File backup;
    try {
      backup = backupJar(jarFile);
    } catch (IOException ex) {
      throw new ReloadException("Falha ao criar backup do .jar de '" + exactName + "': " + ex.getMessage());
    }

    if (plugin instanceof Reloadable) {
      try {
        ((Reloadable) plugin).clearStaticState();
        LOGGER.info("[PluginReloader] clearStaticState() de '" + exactName + "' executado.");
      } catch (Exception ex) {
        LOGGER.log(Level.WARNING, "[PluginReloader] clearStaticState() de '" + exactName + "' lancou excecao, continuando mesmo assim:", ex);
      }
    }

    ClassLoader oldClassLoader = plugin.getClass().getClassLoader();

    try {
      pluginManager.disablePlugin(plugin);
    } catch (Exception ex) {
      throw new ReloadException("onDisable() de '" + exactName + "' lancou excecao, reload abortado: " + ex.getMessage());
    }

    try {
      removeFromPluginManager(pluginManager, plugin);
      removeCommands(exactName, oldClassLoader);
    } catch (Exception ex) {
      LOGGER.log(Level.WARNING, "[PluginReloader] Falha ao limpar estruturas internas do Bukkit para '" + exactName
              + "' (reload continua, mas pode deixar residuo no PluginManager/CommandMap):", ex);
    }

    if (oldClassLoader instanceof URLClassLoader) {
      try {
        ((URLClassLoader) oldClassLoader).close();
      } catch (IOException ex) {
        LOGGER.log(Level.WARNING, "[PluginReloader] Nao foi possivel fechar o classloader antigo de '" + exactName + "':", ex);
      }
    }

    LOGGER.info("[PluginReloader] '" + exactName + "' desabilitado e descarregado.");
    return new ReloadHandle(exactName, jarFile, backup);
  }

  private static Result loadAndEnable(ReloadHandle handle) {
    PluginManager pluginManager = Bukkit.getPluginManager();
    try {
      Plugin reloaded = pluginManager.loadPlugin(handle.jarFile);
      if (reloaded == null) {
        return Result.failure("loadPlugin() retornou null para '" + handle.pluginName + "'.");
      }

      reloaded.onLoad();
      pluginManager.enablePlugin(reloaded);

      if (!reloaded.isEnabled()) {
        return Result.failure("'" + handle.pluginName + "' foi carregado mas nao ficou habilitado "
                + "(onEnable() pode ter sido cancelado -- verifique o console).");
      }

      return Result.success(handle.pluginName);
    } catch (Exception ex) {
      LOGGER.log(Level.SEVERE, "[PluginReloader] Falha ao carregar/habilitar '" + handle.pluginName + "':", ex);
      return Result.failure("Falha ao carregar/habilitar '" + handle.pluginName + "': " + ex.getMessage());
    }
  }

  private static void rollbackAll(List<ReloadHandle> handles) {
    for (ReloadHandle handle : handles) {
      try {
        Files.copy(handle.backupFile.toPath(), handle.jarFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
      } catch (IOException ex) {
        LOGGER.log(Level.SEVERE, "[PluginReloader] FALHA CRITICA ao restaurar backup de '" + handle.pluginName
                + "'. Restaure manualmente: copie " + handle.backupFile + " para " + handle.jarFile, ex);
      }
    }

    for (ReloadHandle handle : handles) {
      Result result = loadAndEnable(handle);
      if (!result.isSuccess()) {
        LOGGER.severe("[PluginReloader] FALHA CRITICA: nao foi possivel reabilitar '" + handle.pluginName
                + "' mesmo apos restaurar o backup. Esse plugin esta OFFLINE -- reinicie o servidor imediatamente.");
      }
    }
  }


  private static File getPluginFile(JavaPlugin plugin) throws Exception {
    Field fileField = JavaPlugin.class.getDeclaredField("file");
    fileField.setAccessible(true);
    return (File) fileField.get(plugin);
  }

  @SuppressWarnings("unchecked")
  private static void removeFromPluginManager(PluginManager pluginManager, Plugin plugin) throws Exception {
    if (!(pluginManager instanceof SimplePluginManager)) {
      return;
    }

    Field pluginsField = SimplePluginManager.class.getDeclaredField("plugins");
    pluginsField.setAccessible(true);
    List<Plugin> plugins = (List<Plugin>) pluginsField.get(pluginManager);
    plugins.remove(plugin);

    Field lookupNamesField = SimplePluginManager.class.getDeclaredField("lookupNames");
    lookupNamesField.setAccessible(true);
    Map<String, Plugin> lookupNames = (Map<String, Plugin>) lookupNamesField.get(pluginManager);
    lookupNames.remove(plugin.getName());
  }

  @SuppressWarnings("unchecked")
  private static void removeCommands(String pluginName, ClassLoader pluginClassLoader) throws Exception {
    PluginManager pluginManager = Bukkit.getPluginManager();
    if (!(pluginManager instanceof SimplePluginManager)) {
      return;
    }

    Field commandMapField = SimplePluginManager.class.getDeclaredField("commandMap");
    commandMapField.setAccessible(true);
    SimpleCommandMap commandMap = (SimpleCommandMap) commandMapField.get(pluginManager);

    Field knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
    knownCommandsField.setAccessible(true);
    Map<String, Command> knownCommands = (Map<String, Command>) knownCommandsField.get(commandMap);

    Iterator<Map.Entry<String, Command>> iterator = knownCommands.entrySet().iterator();
    while (iterator.hasNext()) {
      Command cmd = iterator.next().getValue();
      boolean belongsToPlugin;

      if (cmd instanceof PluginCommand) {
        belongsToPlugin = ((PluginCommand) cmd).getPlugin().getName().equalsIgnoreCase(pluginName);
      } else {

        belongsToPlugin = pluginClassLoader != null && cmd.getClass().getClassLoader() == pluginClassLoader;
      }

      if (belongsToPlugin) {
        cmd.unregister(commandMap);
        iterator.remove();
      }
    }
  }

  private static File backupJar(File jarFile) throws IOException {
    File backupFolder = new File(jarFile.getParentFile(), "reload-backups");
    if (!backupFolder.exists() && !backupFolder.mkdirs()) {
      throw new IOException("nao foi possivel criar a pasta de backup " + backupFolder);
    }

    File backup = new File(backupFolder, jarFile.getName() + "." + System.currentTimeMillis() + ".bak");
    Files.copy(jarFile.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
    return backup;
  }


  private static final class ReloadHandle {
    private final String pluginName;
    private final File jarFile;
    private final File backupFile;

    private ReloadHandle(String pluginName, File jarFile, File backupFile) {
      this.pluginName = pluginName;
      this.jarFile = jarFile;
      this.backupFile = backupFile;
    }
  }

  private static final class ReloadException extends Exception {
    private ReloadException(String message) {
      super(message);
    }
  }

  public static final class Result {
    private final boolean success;
    private final String message;

    private Result(boolean success, String message) {
      this.success = success;
      this.message = message;
    }

    static Result success(String pluginName) {
      return new Result(true, "'" + pluginName + "' recarregado com sucesso.");
    }

    static Result failure(String message) {
      return new Result(false, message);
    }

    public boolean isSuccess() {
      return this.success;
    }

    public String message() {
      return this.message;
    }
  }
}