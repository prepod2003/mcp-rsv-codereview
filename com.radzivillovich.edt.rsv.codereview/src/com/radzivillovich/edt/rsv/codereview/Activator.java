package com.radzivillovich.edt.rsv.codereview;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * Активатор плагина MCP:RSV Code Review.
 */
public class Activator extends AbstractUIPlugin {

    /** Symbolic name бандла — совпадает с MANIFEST.MF. */
    public static final String PLUGIN_ID = "com.radzivillovich.edt.rsv.codereview"; //$NON-NLS-1$

    private static Activator instance;

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        instance = this;
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        instance = null;
        super.stop(context);
    }

    public static Activator getDefault() {
        return instance;
    }
}
