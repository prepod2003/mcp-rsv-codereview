package com.radzivillovich.edt.rsv.codereview.internal;

import java.util.ArrayList;
import java.util.List;

/**
 * Перевод позиции (строка, символ) — 0-based, LSP-стиль — в абсолютный offset в тексте документа.
 * Граница строки — по символу {@code \n} (для {@code \r\n} offset попадает перед {@code \r}, что корректно).
 */
public final class LineOffsets {

    private final int[] lineStart;
    private final int length;

    public LineOffsets(String text) {
        this.length = text.length();
        List<Integer> starts = new ArrayList<>();
        starts.add(0);
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '\n') {
                starts.add(i + 1);
            }
        }
        this.lineStart = new int[starts.size()];
        for (int i = 0; i < starts.size(); i++) {
            this.lineStart[i] = starts.get(i);
        }
    }

    /** Абсолютный offset позиции; значения вне диапазона безопасно усекаются. */
    public int offset(int line, int character) {
        if (line < 0) {
            return 0;
        }
        if (line >= lineStart.length) {
            return length;
        }
        int base = lineStart[line];
        int offset = base + Math.max(0, character);
        if (offset > length) {
            offset = length;
        }
        return offset;
    }
}
