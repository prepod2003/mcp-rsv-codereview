package com.radzivillovich.edt.rsv.codereview.prefs;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.radzivillovich.edt.rsv.codereview.Branding;

/**
 * Узел-контейнер «MCP:RSV» в дереве настроек. Используется только когда основной
 * плагин MCP:RSV не установлен — тогда страница «Код-ревью» вкладывается под этот
 * узел (см. {@link com.radzivillovich.edt.rsv.codereview.PreferenceGroupStartup}).
 * Когда основной плагин есть, его группа «MCP:RSV» используется напрямую, а эта не создаётся.
 */
public final class GroupPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

    public GroupPreferencePage() {
        setTitle("MCP:RSV");
        noDefaultAndApplyButton();
    }

    @Override
    public void init(IWorkbench workbench) {
    }

    @Override
    protected Control createContents(Composite parent) {
        Composite root = new Composite(parent, SWT.NONE);
        root.setLayout(new GridLayout(1, false));
        root.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        Link link = new Link(root, SWT.WRAP);
        link.setText("Инструменты MCP:RSV для 1C:EDT.\nНастройки — в подпунктах слева."
                + "\n\n<a href=\"" + Branding.URL_SITE + "\">prepod2003.github.io/edt-ai-1c</a>");
        link.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        link.addListener(SWT.Selection, e -> Program.launch(e.text));

        return root;
    }
}
