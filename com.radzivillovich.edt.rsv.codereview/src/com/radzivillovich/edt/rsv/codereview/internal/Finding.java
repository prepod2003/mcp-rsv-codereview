package com.radzivillovich.edt.rsv.codereview.internal;

/**
 * Одно замечание код-ревью, разобранное из JSON-отчёта движка BSL Language Server.
 * Нумерация строк/символов — 0-based (LSP-стиль), как у движка. Перевод в 1-based — при создании маркера.
 */
public final class Finding {

    /** Абсолютный путь к .bsl-файлу (нормализованный, без префикса file://). */
    public final String absolutePath;
    public final int line;          // 0-based, начало
    public final int character;     // 0-based, начало
    public final int endLine;       // 0-based, конец
    public final int endCharacter;  // 0-based, конец
    public final String severity;   // Error | Warning | Information | Hint
    public final String code;
    public final String message;
    public final String href;       // ссылка на описание диагностики (может быть null)

    public Finding(String absolutePath, int line, int character, int endLine, int endCharacter,
                   String severity, String code, String message, String href) {
        this.absolutePath = absolutePath;
        this.line = line;
        this.character = character;
        this.endLine = endLine;
        this.endCharacter = endCharacter;
        this.severity = severity;
        this.code = code;
        this.message = message;
        this.href = href;
    }
}
