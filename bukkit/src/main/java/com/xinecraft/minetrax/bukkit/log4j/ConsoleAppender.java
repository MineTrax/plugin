package com.xinecraft.minetrax.bukkit.log4j;

import com.xinecraft.minetrax.bukkit.MinetraxBukkit;
import com.xinecraft.minetrax.common.utils.AnsiColor;
import com.xinecraft.minetrax.common.utils.TimeUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Plugin(name = "Mtx-ConsoleChannel", category = "Core", elementType = "appender", printObject = true)
public class ConsoleAppender extends AbstractAppender {
    private static final PatternLayout PATTERN_LAYOUT;

    static {
        Method createLayoutMethod = null;
        for (Method method : PatternLayout.class.getMethods()) {
            if (method.getName().equals("createLayout")) {
                createLayoutMethod = method;
            }
        }
        if (createLayoutMethod == null) {
            MinetraxBukkit.getPlugin().getLogger().warning("Failed to reflectively find the Log4j createLayout method. The console appender is not going to function.");
            PATTERN_LAYOUT = null;
        } else {
            Object[] args = new Object[createLayoutMethod.getParameterCount()];
            args[0] = "[%d{HH:mm:ss} %level]: %msg";
            if (args.length == 9) {
                // log4j 2.1
                args[5] = true;
                args[6] = true;
            }

            PatternLayout createdLayout = null;
            try {
                createdLayout = (PatternLayout) createLayoutMethod.invoke(null, args);
            } catch (IllegalAccessException | InvocationTargetException e) {
                MinetraxBukkit.getPlugin().getLogger().warning("Failed to reflectively invoke the Log4j createLayout method. The console appender is not going to function.");
            }
            PATTERN_LAYOUT = createdLayout;
        }
    }

    public ConsoleAppender() {
        super("Mtx-ConsoleChannel", null, PATTERN_LAYOUT, false);

        Logger rootLogger = (Logger) LogManager.getRootLogger();
        rootLogger.addAppender(this);
    }


    @Override
    public boolean isStarted() {
        return PATTERN_LAYOUT != null;
    }

    public void shutdown() {
        Logger rootLogger = (Logger) LogManager.getRootLogger();
        rootLogger.removeAppender(this);
    }


    @Override
    public void append(LogEvent event) {
        final MinetraxBukkit plugin = MinetraxBukkit.getPlugin();

        final String eventLevel = event.getLevel().name().toUpperCase();
        String line = event.getMessage().getFormattedMessage();
        line = AnsiColor.convertStringMessage(line, '§');

        // queue final message
        plugin.getConsoleMessageQueue()
                .add(new ConsoleMessage(TimeUtil.timeStamp(), eventLevel, line));
    }
}
