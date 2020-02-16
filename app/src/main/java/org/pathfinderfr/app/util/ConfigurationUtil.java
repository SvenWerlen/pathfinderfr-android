package org.pathfinderfr.app.util;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ConfigurationUtil {

    private static final String TEMPLATE_NAME = "templates.properties";
    private static final String[] SOURCES = new String[] { "mj", "mjra", "mca", "mr", "ag", "am" };
    private static ConfigurationUtil instance;

    private Properties properties;

    private ConfigurationUtil(Context context) {
        properties = new Properties();
        try {
            if(context != null) {
                properties.load(context.getAssets().open(TEMPLATE_NAME));
            }
        } catch (IOException e) {
            Log.e(ConfigurationUtil.class.getSimpleName(),
                    String.format("Properties %s couldn't be found!",TEMPLATE_NAME), e);
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


    public String[] getAvailableSources() {
        return SOURCES;
    }


}
