package com.radzivillovich.edt.rsv.codereview.prefs;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.radzivillovich.edt.rsv.codereview.Activator;
import com.radzivillovich.edt.rsv.codereview.Branding;
import com.radzivillovich.edt.rsv.codereview.Diagnostics;

/**
 * Страница настроек: Параметры → MCP:RSV → Код-ревью.
 * Галочки включения/выключения диагностик + кредит и ссылки (1c-syntax и сайт MCP:RSV).
 */
public final class CodeReviewPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

    private final Map<String, Button> checks = new LinkedHashMap<>();

    public CodeReviewPreferencePage() {
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
        setDescription("Автоматическое код-ревью BSL по стандартам разработки 1С.");
    }

    @Override
    public void init(IWorkbench workbench) {
    }

    @Override
    protected Control createContents(Composite parent) {
        Composite root = new Composite(parent, SWT.NONE);
        root.setLayout(new GridLayout(1, false));
        root.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Link credit = new Link(root, SWT.NONE);
        credit.setText(Branding.creditHtml());
        credit.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        credit.addListener(SWT.Selection, e -> Program.launch(e.text));

        Group group = new Group(root, SWT.NONE);
        group.setText("Диагностики");
        group.setLayout(new GridLayout(1, false));
        group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        IPreferenceStore store = getPreferenceStore();
        for (Diagnostics.Entry entry : Diagnostics.ALL) {
            Button checkbox = new Button(group, SWT.CHECK);
            checkbox.setText(entry.title + "  (" + entry.code + ")");
            checkbox.setSelection(store.getBoolean(entry.prefKey()));
            checks.put(entry.prefKey(), checkbox);
        }

        return root;
    }

    @Override
    protected void performDefaults() {
        IPreferenceStore store = getPreferenceStore();
        for (Diagnostics.Entry entry : Diagnostics.ALL) {
            Button checkbox = checks.get(entry.prefKey());
            if (checkbox != null) {
                checkbox.setSelection(store.getDefaultBoolean(entry.prefKey()));
            }
        }
        super.performDefaults();
    }

    @Override
    public boolean performOk() {
        IPreferenceStore store = getPreferenceStore();
        for (Map.Entry<String, Button> entry : checks.entrySet()) {
            store.setValue(entry.getKey(), entry.getValue().getSelection());
        }
        return super.performOk();
    }
}
