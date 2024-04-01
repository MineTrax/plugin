package com.xinecraft.minetrax.threads;

import com.xinecraft.minetrax.log4j.ConsoleMessage;
import com.xinecraft.minetrax.Minetrax;
import com.xinecraft.minetrax.utils.HttpUtil;
import org.apache.commons.lang.StringUtils;

import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ConsoleMessageQueueWorker extends Thread {
    private static final char LINE_WRAP_INDENT = '\t';
    private static final long MIN_SLEEP_TIME_MILLIS = 2000;

    private final StringBuilder message = new StringBuilder();
    private final Deque<ConsoleMessage> queue = Minetrax.getPlugin().getConsoleMessageQueue();

    public ConsoleMessageQueueWorker() {
        super("Minetrax - Console Message Queue Worker");
    }

    @Override
    public void run() {
        while (true) {
            try {
                message.setLength(0);
                ConsoleMessage consoleMessage;

                // peek to avoid polling a message that we can't process from the queue
                while ((consoleMessage = queue.peek()) != null) {
                    final String formattedMessage = consoleMessage.toString();
                    message.append(formattedMessage).append("\r\n");
                    // finally poll to actually remove the appended message
                    queue.poll();
                }

                final String m = message.toString();
                if (StringUtils.isNotBlank(m)) {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("api_key", Minetrax.getPlugin().getApiKey());
                    params.put("api_secret", Minetrax.getPlugin().getApiSecret());
                    params.put("log", m);
                    params.put("server_id", Minetrax.getPlugin().getApiServerId());
                    try {
                        HttpUtil.postForm(Minetrax.getPlugin().getApiHost() + "/api/v1/server/console", params);
                    } catch (Exception e) {
                        queue.clear();
                        // Dont print anything here it will create recurring loop
                    }
                }

                // make sure rate isn't less than every MIN_SLEEP_TIME_MILLIS because of rate limitations
                long sleepTimeMS = TimeUnit.SECONDS.toMillis( 0);
                if (sleepTimeMS < MIN_SLEEP_TIME_MILLIS) {
                    sleepTimeMS = MIN_SLEEP_TIME_MILLIS;
                }

                Thread.sleep(sleepTimeMS);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
        }
    }
}
