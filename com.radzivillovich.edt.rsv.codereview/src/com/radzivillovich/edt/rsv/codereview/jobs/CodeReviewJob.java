package com.radzivillovich.edt.rsv.codereview.jobs;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.radzivillovich.edt.rsv.codereview.Activator;
import com.radzivillovich.edt.rsv.codereview.internal.BslLanguageServerRunner;
import com.radzivillovich.edt.rsv.codereview.internal.CodeReviewMarkers;
import com.radzivillovich.edt.rsv.codereview.internal.Finding;
import com.radzivillovich.edt.rsv.codereview.internal.LineOffsets;
import com.radzivillovich.edt.rsv.codereview.ui.CodeReviewView;
import com.radzivillovich.edt.rsv.codereview.ui.ReviewModel;

/**
 * Фоновое задание код-ревью: запускает движок на каталоге {@code src} проекта,
 * строит маркеры с точной областью подсветки и показывает результат в панели MCP:RSV.
 * Scope (выбранные модули) ограничивает отображаемые/маркируемые модули; иначе весь проект.
 */
public final class CodeReviewJob extends Job {

    private static final String TITLE = "MCP:RSV Code Review";

    private final IProject project;
    private final Set<IFile> scope;

    public CodeReviewJob(IProject project, Set<IFile> scope) {
        super("MCP:RSV: анализ кода…");
        this.project = project;
        this.scope = scope;
        setUser(true);
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
        monitor.beginTask("Анализ кода (MCP:RSV Code Review)", IProgressMonitor.UNKNOWN);
        try {
            File srcDir = resolveSrcDir();
            if (srcDir == null) {
                return failure("В проекте не найден каталог src: " + project.getName());
            }
            List<Finding> findings = analyzeScoped(srcDir);
            ReviewModel.Result result = buildResultAndMarkers(findings, monitor);
            showView(result);
            return Status.OK_STATUS;
        } catch (InterruptedException e) {
            return Status.CANCEL_STATUS;
        } catch (Exception e) {
            return failure("Не удалось выполнить код-ревью: " + e.getMessage());
        } finally {
            monitor.done();
        }
    }

    /**
     * Запускает движок с минимальной областью анализа. Без scope — весь src проекта.
     * Со scope — только папки выбранных модулей (для формы это одна папка с одним
     * модулем, для объектного — папка объекта), по разу на каждую различную папку.
     * Рабочий каталог процесса всегда = src проекта (кросс-дисковый фикс путей отчёта).
     * Точную фильтрацию по выбранным модулям делает построение маркеров (по scope).
     */
    private List<Finding> analyzeScoped(File srcDir) throws IOException, InterruptedException {
        BslLanguageServerRunner runner = new BslLanguageServerRunner();
        if (scope == null || scope.isEmpty()) {
            return runner.analyze(srcDir, srcDir);
        }
        LinkedHashSet<File> dirs = new LinkedHashSet<>();
        for (IFile file : scope) {
            IPath loc = file.getLocation();
            if (loc == null) {
                continue;
            }
            File parent = loc.toFile().getParentFile();
            if (parent != null && parent.isDirectory()) {
                dirs.add(parent);
            }
        }
        if (dirs.isEmpty()) {
            return runner.analyze(srcDir, srcDir);
        }
        List<Finding> all = new ArrayList<>();
        for (File dir : dirs) {
            all.addAll(runner.analyze(dir, srcDir));
        }
        return all;
    }

    private File resolveSrcDir() {
        IPath location = project.getLocation();
        if (location == null) {
            return null;
        }
        File src = location.append("src").toFile();
        return src.isDirectory() ? src : null;
    }

    private ReviewModel.Result buildResultAndMarkers(List<Finding> findings, IProgressMonitor monitor)
            throws CoreException {
        boolean wholeProject = (scope == null || scope.isEmpty());

        // Группировка замечаний по модулям (в пределах scope).
        Map<IFile, List<Finding>> grouped = new LinkedHashMap<>();
        for (Finding finding : findings) {
            IFile file = toWorkspaceFile(finding.absolutePath);
            if (file == null || !file.exists()) {
                continue;
            }
            if (!wholeProject && !scope.contains(file)) {
                continue;
            }
            grouped.computeIfAbsent(file, f -> new ArrayList<>()).add(finding);
        }

        List<ReviewModel.FileGroup> groups = new ArrayList<>();
        int[] counts = new int[3]; // 0=errors, 1=warnings, 2=infos

        IWorkspaceRunnable op = m -> {
            if (wholeProject) {
                CodeReviewMarkers.clear(project);
            } else {
                for (IFile file : scope) {
                    CodeReviewMarkers.clear(file);
                }
            }
            for (Map.Entry<IFile, List<Finding>> entry : grouped.entrySet()) {
                IFile file = entry.getKey();
                LineOffsets offsets = readOffsets(file);
                List<ReviewModel.Item> items = new ArrayList<>();
                for (Finding finding : entry.getValue()) {
                    int start = -1;
                    int end = -1;
                    if (offsets != null) {
                        start = offsets.offset(finding.line, finding.character);
                        end = offsets.offset(finding.endLine, finding.endCharacter);
                        if (end < start) {
                            end = start;
                        }
                    }
                    IMarker marker = CodeReviewMarkers.create(file, finding, start, end);
                    items.add(new ReviewModel.Item(finding, marker));
                    bumpCount(counts, finding.severity);
                }
                groups.add(new ReviewModel.FileGroup(file, items));
            }
        };
        ResourcesPlugin.getWorkspace().run(op, project, IWorkspace.AVOID_UPDATE, monitor);

        return new ReviewModel.Result(scopeTitle(), groups, counts[0], counts[1], counts[2]);
    }

    private void bumpCount(int[] counts, String severity) {
        switch (CodeReviewMarkers.mapSeverity(severity)) {
            case IMarker.SEVERITY_ERROR:
                counts[0]++;
                break;
            case IMarker.SEVERITY_WARNING:
                counts[1]++;
                break;
            default:
                counts[2]++;
                break;
        }
    }

    private LineOffsets readOffsets(IFile file) {
        try {
            IPath location = file.getLocation();
            if (location == null) {
                return null;
            }
            String text = Files.readString(location.toFile().toPath(), StandardCharsets.UTF_8);
            return new LineOffsets(text);
        } catch (Exception e) {
            return null; // без точных offset'ов — маркер по строке
        }
    }

    private String scopeTitle() {
        if (scope == null || scope.isEmpty()) {
            return "Проект: " + project.getName();
        }
        if (scope.size() == 1) {
            IFile file = scope.iterator().next();
            String module = file.getParent() != null ? file.getParent().getName() : file.getName();
            return "Модуль: " + module;
        }
        return "Модулей: " + scope.size();
    }

    private IFile toWorkspaceFile(String absolutePath) {
        IPath path = Path.fromOSString(absolutePath);
        return ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path);
    }

    private void showView(ReviewModel.Result result) {
        Display display = PlatformUI.isWorkbenchRunning() ? PlatformUI.getWorkbench().getDisplay() : null;
        if (display == null) {
            return;
        }
        display.asyncExec(() -> {
            IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            if (window == null) {
                return;
            }
            IWorkbenchPage page = window.getActivePage();
            if (page == null) {
                return;
            }
            try {
                IViewPart part = page.showView(CodeReviewView.ID);
                if (part instanceof CodeReviewView) {
                    ((CodeReviewView) part).setResult(result);
                }
            } catch (Exception ignored) {
                // не удалось открыть панель — результат уже в маркерах/Проблемах
            }
        });
    }

    private IStatus failure(String message) {
        showError(message);
        return new Status(IStatus.ERROR, Activator.PLUGIN_ID, message);
    }

    private void showError(String message) {
        Display display = PlatformUI.isWorkbenchRunning() ? PlatformUI.getWorkbench().getDisplay() : null;
        if (display == null) {
            return;
        }
        display.asyncExec(() -> {
            IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            Shell shell = window != null ? window.getShell() : null;
            MessageDialog.openError(shell, TITLE, message);
        });
    }
}
