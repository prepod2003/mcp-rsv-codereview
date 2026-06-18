package com.radzivillovich.edt.rsv.codereview.prefs;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.radzivillovich.edt.rsv.codereview.Activator;
import com.radzivillovich.edt.rsv.codereview.Diagnostics;

/**
 * Значения настроек диагностик по умолчанию (профиль из коробки).
 */
public final class PreferenceInitializer extends AbstractPreferenceInitializer {

    @Override
    public void initializeDefaultPreferences() {
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();
        for (Diagnostics.Entry entry : Diagnostics.ALL) {
            store.setDefault(entry.prefKey(), entry.defaultEnabled);
        }
    }
}
