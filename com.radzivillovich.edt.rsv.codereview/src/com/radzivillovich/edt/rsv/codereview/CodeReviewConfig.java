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
     * Эффективный конфиг движка в виде JSON-строки: язык + список выключенных диагностик
     * (включённые не упоминаются — движок применяет к ним свои значения по умолчанию).
     */
    public static String effectiveConfigJson() {
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();
        StringBuilder disabled = new StringBuilder();
        for (Diagnostics.Entry entry : Diagnostics.ALL) {
            if (!store.getBoolean(entry.prefKey())) {
                if (disabled.length() > 0) {
                    disabled.append(',');
                }
                disabled.append('"').append(entry.code).append("\":false");
            }
        }
        return "{\"language\":\"ru\",\"diagnostics\":{\"parameters\":{" + disabled + "}}}";
    }

    private CodeReviewConfig() {
    }
}
