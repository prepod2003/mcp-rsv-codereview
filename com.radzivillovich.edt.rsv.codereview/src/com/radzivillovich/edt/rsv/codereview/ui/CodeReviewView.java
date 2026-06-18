package com.radzivillovich.edt.rsv.codereview.ui;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.ViewPart;

import com.radzivillovich.edt.rsv.codereview.Branding;
import com.radzivillovich.edt.rsv.codereview.internal.CodeReviewMarkers;

/**
 * Панель «MCP:RSV Code Review»: брендированное представление результатов код-ревью —
 * дерево «модуль → замечания», сводка, ссылки на движок (1c-syntax) и сайт MCP:RSV.
 * Двойной клик по замечанию — переход к строке в редакторе.
 */
public final class CodeReviewView extends ViewPart {

    public static final String ID = "com.radzivillovich.edt.rsv.codereview.views.CodeReviewView";

    private Label summary;
    private TreeViewer viewer;
    private ReviewModel.Result result;

    @Override
    public void createPartControl(Composite parent) {
        parent.setLayout(new GridLayout(1, false));

        Link banner = new Link(parent, SWT.NONE);
        banner.setText(Branding.creditHtml());
        banner.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        banner.addListener(SWT.Selection, e -> Program.launch(e.text));

        summary = new Label(parent, SWT.NONE);
        summary.setText("Запустите «Проверить код» в редакторе модуля или из навигатора.");
        summary.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        viewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        viewer.setContentProvider(new ContentProvider());
        viewer.setLabelProvider(new ResultLabelProvider());
        viewer.addDoubleClickListener(new OpenListener());

        createToolbar();
    }

    private void createToolbar() {
        IToolBarManager bar = getViewSite().getActionBars().getToolBarManager();
        bar.add(new Action("Свернуть всё") {
            @Override
            public void run() {
                if (viewer != null) {
                    viewer.collapseAll();
                }
            }
        });
        bar.add(new Action("Очистить") {
            @Override
            public void run() {
                clearResult();
            }
        });
    }

    /** Обновить панель результатом прогона (вызывается из UI-потока). */
    public void setResult(ReviewModel.Result newResult) {
        this.result = newResult;
        if (viewer == null || viewer.getControl().isDisposed()) {
            return;
        }
        summary.setText(buildSummary(newResult));
        summary.getParent().layout();
        viewer.setInput(newResult);
        viewer.expandAll();
    }

    private String buildSummary(ReviewModel.Result r) {
        if (r == null) {
            return "—";
        }
        if (r.total() == 0) {
            return r.scopeTitle + "  ·  замечаний не найдено";
        }
        return r.scopeTitle + "  ·  замечаний: " + r.total()
                + "   (ошибок: " + r.errors + ", предупреждений: " + r.warnings + ", информационных: " + r.infos + ")";
    }

    private void clearResult() {
        if (result != null) {
            IWorkspaceRunnable op = m -> {
                for (ReviewModel.FileGroup group : result.files) {
                    if (group.file.exists()) {
                        group.file.deleteMarkers(CodeReviewMarkers.MARKER_TYPE, true, IResource.DEPTH_ZERO);
                    }
                }
            };
            try {
                ResourcesPlugin.getWorkspace().run(op, null);
            } catch (Exception ignored) {
                // очистка маркеров — не критична
            }
        }
        result = null;
        if (viewer != null && !viewer.getControl().isDisposed()) {
            viewer.setInput(null);
            summary.setText("Запустите «Проверить код» в редакторе модуля или из навигатора.");
            summary.getParent().layout();
        }
    }

    @Override
    public void setFocus() {
        if (viewer != null) {
            viewer.getControl().setFocus();
        }
    }

    // --- провайдеры дерева ---

    private static final class ContentProvider implements ITreeContentProvider {
        @Override
        public Object[] getElements(Object input) {
            if (input instanceof ReviewModel.Result) {
                return ((ReviewModel.Result) input).files.toArray();
            }
            return new Object[0];
        }

        @Override
        public Object[] getChildren(Object parent) {
            if (parent instanceof ReviewModel.FileGroup) {
                return ((ReviewModel.FileGroup) parent).items.toArray();
            }
            return new Object[0];
        }

        @Override
        public Object getParent(Object element) {
            return null;
        }

        @Override
        public boolean hasChildren(Object element) {
            return element instanceof ReviewModel.FileGroup && !((ReviewModel.FileGroup) element).items.isEmpty();
        }
    }

    private static final class ResultLabelProvider extends LabelProvider {
        @Override
        public String getText(Object element) {
            if (element instanceof ReviewModel.FileGroup) {
                ReviewModel.FileGroup group = (ReviewModel.FileGroup) element;
                String module = group.file.getParent() != null ? group.file.getParent().getName() : group.file.getName();
                return module + "  (" + group.items.size() + ")";
            }
            if (element instanceof ReviewModel.Item) {
                ReviewModel.Item item = (ReviewModel.Item) element;
                String code = item.finding.code != null ? item.finding.code + " — " : "";
                return "стр. " + (item.finding.line + 1) + "   " + code + item.finding.message;
            }
            return String.valueOf(element);
        }

        @Override
        public Image getImage(Object element) {
            ISharedImages shared = PlatformUI.getWorkbench().getSharedImages();
            if (element instanceof ReviewModel.FileGroup) {
                return shared.getImage(ISharedImages.IMG_OBJ_FILE);
            }
            if (element instanceof ReviewModel.Item) {
                int sev = CodeReviewMarkers.mapSeverity(((ReviewModel.Item) element).finding.severity);
                switch (sev) {
                    case IMarker.SEVERITY_ERROR:
                        return shared.getImage(ISharedImages.IMG_OBJS_ERROR_TSK);
                    case IMarker.SEVERITY_WARNING:
                        return shared.getImage(ISharedImages.IMG_OBJS_WARN_TSK);
                    default:
                        return shared.getImage(ISharedImages.IMG_OBJS_INFO_TSK);
                }
            }
            return null;
        }
    }

    private final class OpenListener implements IDoubleClickListener {
        @Override
        public void doubleClick(DoubleClickEvent event) {
            Object selected = ((IStructuredSelection) event.getSelection()).getFirstElement();
            if (!(selected instanceof ReviewModel.Item)) {
                return;
            }
            IMarker marker = ((ReviewModel.Item) selected).marker;
            if (marker == null || !marker.exists()) {
                return;
            }
            try {
                IWorkbenchPage page = getSite().getPage();
                IDE.openEditor(page, marker);
            } catch (Exception ignored) {
                // не удалось открыть — игнорируем
            }
        }
    }
}
