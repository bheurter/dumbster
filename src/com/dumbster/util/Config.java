package com.dumbster.util;
/**
 * File copyright 8/8/12 by Stephen Beitzel
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Central class to hold all the configuration of the server.
 *
 * @author Stephen Beitzel &lt;sbeitzel@pobox.com&gt;
 */
public class Config {
    public static final String PROP_NUM_THREADS = "dumbster.numThreads";
    private static final String PROP_MAX_THREADS = "dumbster.maxThread";
    private static final String PROP_DEFAULT_SMTP_PORT = "dumbster.defaultSmtpPort";
    private static final String PROP_SERVER_SOCKET_TIMEOUT = "dumbster.serverSocketTimeout";


    private static final Config CURRENT_CONFIG = new Config();
    private Properties _config;

    private Config() {
        Properties defaultProperties = getDefaultProperties();

        _config = new Properties(defaultProperties);

        try {
            File propertyFile = new File("dumbster.properties");
            if (propertyFile.exists()) {
                FileInputStream fis = new FileInputStream(propertyFile);
                _config.load(fis);
            }
        } catch (IOException ioe) {
            System.out.println("dumbster.properties not loaded");
        }
    }

    private Properties getDefaultProperties() {
        Properties defaultProperties = new Properties();
        defaultProperties.setProperty(PROP_DEFAULT_SMTP_PORT,"25");         //DEFAULT_SMTP_PORT
        defaultProperties.setProperty(PROP_SERVER_SOCKET_TIMEOUT,"5000");   //SERVER_SOCKET_TIMEOUT
        defaultProperties.setProperty(PROP_MAX_THREADS,"10");               //PROP_MAX_THREADS
        defaultProperties.setProperty(PROP_NUM_THREADS,"1");     //DEFAULT_THREADS   // as implemented by rjo

         return defaultProperties;
    }

    public static Config getConfig() {
        return CURRENT_CONFIG;
    }

    private static int getInt(Properties properties, String propertyName) {
        String val = properties.getProperty(propertyName);
        return Integer.parseInt(val);
    }

    public int getNumSMTPThreads() {
        int threadCount = getInt(_config, PROP_NUM_THREADS);
        int maxThread = getInt(_config, PROP_MAX_THREADS);

        threadCount = Math.max(threadCount, 1);
        if (threadCount > maxThread) {
            threadCount = maxThread;
        }
        return threadCount;
    }

    public void setNumSMTPThreads(int count) {
        _config.setProperty(PROP_NUM_THREADS, String.valueOf(count));
    }

    public int getNumPOPThreads() {
        // the initial implementation was to use the same property for both services, so we'll not change that yet.
        return getNumSMTPThreads();
    }

    public void setNumPOPThreads(int count) {
        setNumSMTPThreads(count);
    }

    public int getDefaultSmtpPort() {
        return getInt(_config, PROP_DEFAULT_SMTP_PORT);

    }

    public int getServerSocketTimeout() {
        return getInt(_config, PROP_SERVER_SOCKET_TIMEOUT);

    }
}
