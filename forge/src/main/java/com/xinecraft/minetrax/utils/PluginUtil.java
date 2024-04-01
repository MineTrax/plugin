package com.xinecraft.minetrax.utils;

import com.xinecraft.minetrax.Minetrax;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredListener;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URLClassLoader;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("unchecked")
public class PluginUtil {

    public static void unloadPlugin(Plugin plugin) {
        String name = plugin.getName();
        PluginManager pluginManager = Bukkit.getPluginManager();
        SimpleCommandMap commandMap = null;
        List<Plugin> plugins = null;
        Map<String, Plugin> names = null;
        Map<String, Command> commands = null;
        Map<Event, SortedSet<RegisteredListener>> listeners = null;

        boolean reloadListeners = true;
        pluginManager.disablePlugin(plugin);

        try {
            Field pluginsField = Bukkit.getPluginManager().getClass().getDeclaredField("plugins");
            pluginsField.setAccessible(true);
            plugins = (List<Plugin>) pluginsField.get(pluginManager);

            Field lookupNamesField = Bukkit.getPluginManager().getClass().getDeclaredField("lookupNames");
            lookupNamesField.setAccessible(true);
            names = (Map<String, Plugin>) lookupNamesField.get(pluginManager);

            try {
                Field listenersField = Bukkit.getPluginManager().getClass().getDeclaredField("listeners");
                listenersField.setAccessible(true);
                listeners = (Map<Event, SortedSet<RegisteredListener>>) listenersField.get(pluginManager);
            } catch (Exception e) {
                reloadListeners = false;
            }

            Field commandMapField = Bukkit.getPluginManager().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            commandMap = (SimpleCommandMap) commandMapField.get(pluginManager);

            Field knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
            knownCommandsField.setAccessible(true);
            commands = (Map<String, Command>) knownCommandsField.get(commandMap);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        pluginManager.disablePlugin(plugin);

        if (plugins != null && plugins.contains(plugin))
            plugins.remove(plugin);

        if (names != null && names.containsKey(name))
            names.remove(name);

        if (listeners != null && reloadListeners) {
            for (SortedSet<RegisteredListener> set : listeners.values()) {
                set.removeIf(value -> value.getPlugin() == plugin);
            }
        }

        if (commandMap != null) {
            for (Iterator<Map.Entry<String, Command>> it = commands.entrySet().iterator(); it.hasNext();) {
                Map.Entry<String, Command> entry = it.next();
                if (entry.getValue() instanceof PluginCommand) {
                    PluginCommand c = (PluginCommand) entry.getValue();
                    if (c.getPlugin() == plugin) {
                        c.unregister(commandMap);
                        it.remove();
                    }
                }
            }
        }

        ClassLoader cl = plugin.getClass().getClassLoader();
        if (cl instanceof URLClassLoader) {
            try {
                ((URLClassLoader) cl).close();
            } catch (IOException ex) {
                Logger.getLogger(PluginUtil.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        System.gc();
    }

    /**
     * Check whether or not the given plugin is installed and enabled
     * @param pluginName The plugin name to check
     * @return Whether or not the plugin is installed and enabled
     */
    public static boolean checkIfPluginEnabled(String pluginName) {
        return checkIfPluginEnabled(pluginName, true);
    }

    /**
     * Check whether or not the given plugin is installed and enabled
     * @param pluginName The plugin name to check
     * @param startsWith Whether or not to to {@link String#startsWith(String)} checking
     * @return Whether or not the plugin is installed and enabled
     */
    public static boolean checkIfPluginEnabled(String pluginName, boolean startsWith) {
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            boolean match = startsWith
                    ? plugin.getName().toLowerCase().startsWith(pluginName.toLowerCase())
                    : plugin.getName().equalsIgnoreCase(pluginName);
            if (match) {
                if (plugin.isEnabled()) {
                    return true;
                } else {
                    Minetrax.getPlugin().getLogger().info("Plugin " + plugin.getName() + " found but wasn't enabled. Returning false.");
                    return false;
                }
            }
        }
        return false;
    }

    public static boolean pluginHookIsEnabled(String pluginName) {
        return pluginHookIsEnabled(pluginName, true);
    }

    public static boolean pluginHookIsEnabled(String pluginName, boolean startsWith) {
        return checkIfPluginEnabled(pluginName, startsWith);
    }

    public static Plugin getPlugin(String pluginName) {
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins())
            if (plugin.getName().toLowerCase().startsWith(pluginName.toLowerCase())) return plugin;
        return null;
    }

}
