package com.radzivillovich.edt.rsv.codereview.internal;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import com.radzivillovich.edt.rsv.codereview.Activator;

/**
 * Создание и очистка маркеров код-ревью. Маркеры нашего типа показываются в панели «Проблемы»
 * и подсветкой в редакторе, не смешиваясь с нативными проверками EDT.
 */
public final class CodeReviewMarkers {

    /** Полный id типа маркера (bundleId + id из plugin.xml). */
    public static final String MARKER_TYPE = Activator.PLUGIN_ID + ".problem"; //$NON-NLS-1$

    private CodeReviewMarkers() {
    }

    /** Удаляет наши маркеры с ресурса (и вложенных). */
    public static void clear(IResource resource) throws CoreException {
        if (resource != null && resource.exists()) {
            resource.deleteMarkers(MARKER_TYPE, true, IResource.DEPTH_INFINITE);
        }
    }

    /**
     * Создаёт маркер по замечанию. Строка переводится из 0-based (движок) в 1-based (Eclipse).
     * Если переданы валидные offset'ы (≥0) — задаёт точную посимвольную область подсветки.
     */
    public static IMarker create(IFile file, Finding finding, int startOffset, int endOffset) throws CoreException {
        IMarker marker = file.createMarker(MARKER_TYPE);
        marker.setAttribute(IMarker.LINE_NUMBER, finding.line + 1);
        marker.setAttribute(IMarker.SEVERITY, mapSeverity(finding.severity));
        marker.setAttribute(IMarker.MESSAGE, buildMessage(finding));
        marker.setAttribute(IMarker.LOCATION, "Строка " + (finding.line + 1));
        if (startOffset >= 0 && endOffset >= startOffset) {
            marker.setAttribute(IMarker.CHAR_START, startOffset);
            marker.setAttribute(IMarker.CHAR_END, endOffset);
        }
        return marker;
    }

    private static String buildMessage(Finding finding) {
        StringBuilder sb = new StringBuilder();
        if (finding.message != null) {
            sb.append(finding.message);
        }
        if (finding.code != null) {
            sb.append(" [").append(finding.code).append(']');
        }
        return sb.toString();
    }

    public static int mapSeverity(String severity) {
        if (severity == null) {
            return IMarker.SEVERITY_INFO;
        }
        switch (severity) {
            case "Error":
                return IMarker.SEVERITY_ERROR;
            case "Warning":
                return IMarker.SEVERITY_WARNING;
            default:
                return IMarker.SEVERITY_INFO;
        }
    }
}
