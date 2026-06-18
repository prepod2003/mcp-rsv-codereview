package com.radzivillovich.edt.rsv.codereview.handlers;

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

import com.radzivillovich.edt.rsv.codereview.jobs.CodeReviewJob;

/**
 * Обработчик команды «Проверить код». Определяет целевой проект и (опционально) выбранные
 * .bsl-модули из выделения навигатора или активного редактора, запускает {@link CodeReviewJob}.
 * Цель резолвится через {@link Adapters} — редактор модуля BSL в EDT использует собственный
 * вход, не обычный IFileEditorInput.
 */
public final class RunCodeReviewHandler extends AbstractHandler {

    private static final String BSL = "bsl"; //$NON-NLS-1$

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        Set<IFile> bslFiles = new LinkedHashSet<>();
        IProject project = collectFromSelection(HandlerUtil.getCurrentSelection(event), bslFiles);
        if (project == null) {
            project = collectFromEditor(HandlerUtil.getActiveEditor(event), bslFiles);
        }
        if (project == null) {
            Shell shell = HandlerUtil.getActiveShell(event);
            MessageDialog.openInformation(shell, "MCP:RSV Code Review",
                    "Не удалось определить модуль или проект для проверки. "
                    + "Откройте BSL-модуль в редакторе или выберите модуль/проект в навигаторе.");
            return null;
        }
        new CodeReviewJob(project, bslFiles).schedule();
        return null;
    }

    private IProject collectFromSelection(ISelection selection, Set<IFile> bslFiles) {
        if (!(selection instanceof IStructuredSelection)) {
            return null;
        }
        IProject project = null;
        for (Object element : ((IStructuredSelection) selection).toList()) {
            IResource resource = toResource(element);
            if (resource == null) {
                continue;
            }
            if (project == null) {
                project = resource.getProject();
            }
            addIfBsl(resource, bslFiles);
        }
        return project;
    }

    private IProject collectFromEditor(IEditorPart editor, Set<IFile> bslFiles) {
        if (editor == null) {
            return null;
        }
        IEditorInput input = editor.getEditorInput();
        IFile file = Adapters.adapt(input, IFile.class);
        if (file == null) {
            IResource resource = Adapters.adapt(input, IResource.class);
            if (resource instanceof IFile) {
                file = (IFile) resource;
            } else if (resource != null) {
                return resource.getProject();
            }
        }
        if (file == null) {
            file = Adapters.adapt(editor, IFile.class);
        }
        if (file == null) {
            return null;
        }
        addIfBsl(file, bslFiles);
        return file.getProject();
    }

    private void addIfBsl(IResource resource, Set<IFile> bslFiles) {
        if (resource instanceof IFile && BSL.equalsIgnoreCase(resource.getFileExtension())) {
            bslFiles.add((IFile) resource);
        }
    }

    private IResource toResource(Object element) {
        IResource resource = Adapters.adapt(element, IResource.class);
        if (resource == null) {
            IFile file = Adapters.adapt(element, IFile.class);
            resource = file;
        }
        return resource;
    }
}
