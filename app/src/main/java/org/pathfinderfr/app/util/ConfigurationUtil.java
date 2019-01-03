package org.pathfinderfr.app.util;

import android.content.Context;
import android.content.ContextWrapper;

import java.io.IOException;
import java.util.Properties;

public class ConfigurationUtil {

    private static final String TEMPLATE_NAME = "templates.properties";
    private static ConfigurationUtil instance;

    private Properties properties;

    private ConfigurationUtil(Context context) {
        properties = new Properties();
        try {
            properties.load(context.getAssets().open(TEMPLATE_NAME));
        } catch (IOException e) {
            System.out.println(String.format("Properties %s couldn't be found!",TEMPLATE_NAME));
        }
    }

    public static synchronized ConfigurationUtil getInstance(Context context) {
        if(instance == null) {
            instance = new ConfigurationUtil(context);
        }
        return instance;
    }

    public static ConfigurationUtil getInstance() {
        if(instance == null) {
            throw new IllegalStateException("Configuration has not been properly loaded yet!");
        }
        return instance;
    }

    public Properties getProperties() {
        return properties;
    }

}
