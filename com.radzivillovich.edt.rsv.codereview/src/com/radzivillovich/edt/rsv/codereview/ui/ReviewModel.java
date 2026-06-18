package com.radzivillovich.edt.rsv.codereview.ui;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;

import com.radzivillovich.edt.rsv.codereview.internal.Finding;

/**
 * Модель результата одного прогона код-ревью для отображения в панели.
 */
public final class ReviewModel {

    /** Одно замечание + связанный маркер (для перехода по двойному клику). */
    public static final class Item {
        public final Finding finding;
        public final IMarker marker;

        public Item(Finding finding, IMarker marker) {
            this.finding = finding;
            this.marker = marker;
        }
    }

    /** Группа замечаний по одному модулю. */
    public static final class FileGroup {
        public final IFile file;
        public final List<Item> items;

        public FileGroup(IFile file, List<Item> items) {
            this.file = file;
            this.items = items;
        }
    }

    /** Итог прогона: что проверяли, группы по модулям, сводка по severity. */
    public static final class Result {
        public final String scopeTitle;
        public final List<FileGroup> files;
        public final int errors;
        public final int warnings;
        public final int infos;

        public Result(String scopeTitle, List<FileGroup> files, int errors, int warnings, int infos) {
            this.scopeTitle = scopeTitle;
            this.files = files;
            this.errors = errors;
            this.warnings = warnings;
            this.infos = infos;
        }

        public int total() {
            return errors + warnings + infos;
        }
    }

    private ReviewModel() {
    }
}
