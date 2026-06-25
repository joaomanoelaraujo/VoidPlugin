package org.ltzin.logger;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginLogger;

import java.util.logging.LogRecord;

public class VLogger extends PluginLogger {

    private final Plugin plugin;
    private final String prefix;
    private final CommandSender sender;

    public VLogger(Plugin plugin) {
        super(plugin);
        this.plugin = plugin;
        this.prefix = "[" + plugin.getDescription().getName() + "] ";
        this.sender = Bukkit.getConsoleSender();
    }

    public VLogger (VLogger parent, String prefix) {
    super(parent.plugin);

    this.plugin = parent.plugin;
    this.prefix = parent.prefix;
    this.sender = Bukkit.getConsoleSender();

    }

    public void run(java.util.logging.Level level, String method, Runnable runnable) {
        try {
            runnable.run();
        }catch (Exception e) {
            this.log(level, prefix + method + " " + e.getMessage(), e);
        }
    }



    @Override
    public void log(LogRecord record) {
    Level level = Level.fromName(record.getLevel().getName());
    if (level == null) {
        return;
    }
    String message = record.getMessage();
    if (message.equals("Default system encoding may have misread config.yml from plugin jar")) {
        return;
    }
    StringBuilder res = new StringBuilder(this.prefix + message);
    if (record.getThrown() != null) {
        res.append("\n").append(record.getThrown().getLocalizedMessage());
        for (StackTraceElement ste : record.getThrown().getStackTrace()) {
            res.append("\n").append(ste.toString());
        }
      }

    this.sender.sendMessage(level.format(res.toString()));
    }

    public VLogger getModule(String module) {
        return new VLogger(this, prefix + module + ": ");
    }

    public VLogger getALogger(){
        return this;
    }

    private enum Level {
        INFO("§a"),
        WARNING("§e"),
        ERROR("§4");

        private final String color;

        Level(String color){
            this.color = color;
        }

        public static Level fromName(String name){
                for(Level level : Level.values()){
                    if (level.name().equalsIgnoreCase(name)){
                        return level;
                    }
                }
                return null;
            }

        public String format(String message) {
            return this.color + message;
        }
    }
}
