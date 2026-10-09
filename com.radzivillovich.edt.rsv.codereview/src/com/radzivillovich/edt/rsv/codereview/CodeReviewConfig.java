package com.radzivillovich.edt.rsv.codereview;

import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Единый источник конфигурации движка код-ревью.
 *
 * Собирает содержимое {@code .bsl-language-server.json} из настроек компонента
 * (галочки диагностик на странице «Параметры → MCP:RSV → Код-ревью» с учётом
 * значений по умолчанию). Один и тот же конфиг применяют оба пути запуска движка:
 * ревью внутри EDT (панель MCP:RSV) и вызов инструмента {@code code_review} с MCP-сервера.
 *
 * <p>MCP-сервер живёт в отдельном бандле и вызывает {@link #effectiveConfigJson()}
 * рефлексией через бандл компонента — поэтому метод публичный и статический,
 * а сигнатура (имя класса, имя метода, тип возврата {@code String}) — часть контракта.
 */
public final class CodeReviewConfig {

    /**
     * Эффективный конфиг движка в виде JSON-строки: язык + опция «поддержка обычного
     * приложения» + список выключенных диагностик (включённые не упоминаются — движок
     * применяет к ним свои значения по умолчанию).
     */
    public static String effectiveConfigJson() {
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();
        StringBuilder disabled = new StringBuilder();
        java.util.Set<String> off = new java.util.HashSet<>();
        for (Diagnostics.Entry entry : Diagnostics.ALL) {
            if (!store.getBoolean(entry.prefKey())) {
                off.add(entry.code);
                if (disabled.length() > 0) {
                    disabled.append(',');
                }
                disabled.append('"').append(entry.code).append("\":false");
            }
        }
        // Удаление временного файла засчитывается и асинхронными методами платформы
        // (Ждать УдалитьФайлыАсинх / ПереместитьФайлАсинх) — в умолчании движка их нет,
        // и корректный асинхронный код получал «ошибку».
        if (!off.contains(TEMP_FILE_RULE)) {
            if (disabled.length() > 0) {
                disabled.append(',');
            }
            disabled.append('"').append(TEMP_FILE_RULE).append("\":{\"searchDeleteFileMethod\":\"")
                .append(TEMP_FILE_DELETE_METHODS).append("\"}");
        }
        boolean ordinaryAppSupport = store.getBoolean(Diagnostics.PREF_ORDINARY_APP_SUPPORT);
        return "{\"language\":\"ru\",\"diagnostics\":{"
            + "\"ordinaryAppSupport\":" + ordinaryAppSupport + ","
            + "\"parameters\":{" + disabled + "}}}";
    }

    /** Правило «временный файл не удалён». */
    static final String TEMP_FILE_RULE = "MissingTemporaryFileDeletion";

    /** Методы удаления/перемещения файла: умолчание движка + асинхронные варианты платформы. */
    static final String TEMP_FILE_DELETE_METHODS =
        "УдалитьФайлы|DeleteFiles|НачатьУдалениеФайлов|BeginDeletingFiles|ПереместитьФайл|MoveFile"
        + "|УдалитьФайлыАсинх|DeleteFilesAsync|ПереместитьФайлАсинх|MoveFileAsync";

    private CodeReviewConfig() {
    }
}
