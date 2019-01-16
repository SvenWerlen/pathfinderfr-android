package org.pathfinderfr.app.util;

import android.util.Log;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Log.class})
public class ConfigurationUtilTest {

    @Before
    public void init() {
        PowerMockito.mockStatic(Log.class);
    }

    @Test
    public void uniqueInstance() {
        try {
            ConfigurationUtil cfg = ConfigurationUtil.getInstance();
            assertTrue(false);
        } catch(IllegalStateException exc) {
            assertTrue(true);
        }

        ConfigurationUtil cfg = ConfigurationUtil.getInstance(null);
        assertNotNull(cfg);
        assertEquals(cfg,ConfigurationUtil.getInstance());
    }

    @Test
    public void loadProperties() {
        ConfigurationUtil cfg = ConfigurationUtil.getInstance();
        assertNotNull(cfg.getProperties());
        // cannot load data because of Android implementation (couldn't find a way to load assets)
        assertTrue(cfg.getProperties().isEmpty());
        assertEquals(0, cfg.getSources().length);
    }
}
