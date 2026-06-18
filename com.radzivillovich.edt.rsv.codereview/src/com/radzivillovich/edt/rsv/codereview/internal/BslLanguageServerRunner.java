package com.radzivillovich.edt.rsv.codereview.internal;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.FileLocator;
import org.osgi.framework.Bundle;

import com.radzivillovich.edt.rsv.codereview.Activator;
import com.radzivillovich.edt.rsv.codereview.CodeReviewConfig;

/**
 * Запуск встроенного движка BSL Language Server отдельным процессом (CLI-режим {@code analyze}).
 * Java берётся та, на которой работает EDT ({@code java.home}). Конфигурация генерируется из настроек.
 */
public final class BslLanguageServerRunner {

    private static final String JAR_FOLDER = "resources/bsl-language-server"; //$NON-NLS-1$
    private static final long TIMEOUT_MINUTES = 10;

    /**
     * Анализирует каталог {@code analyzeDir} и возвращает разобранные замечания.
     * Рабочий каталог процесса задаётся отдельно ({@code workingDir}) — обычно это
     * каталог src проекта, тогда как {@code analyzeDir} может быть сужен до папки
     * конкретного модуля (на крупных конфигурациях это убирает разбор всего проекта).
     */
    public List<Finding> analyze(File analyzeDir, File workingDir) throws IOException, InterruptedException {
        return JsonReportParser.parse(runEngine(analyzeDir, workingDir));
    }

    /**
     * Запускает движок и возвращает СЫРОЙ текст JSON-отчёта. Единая точка
     * переиспользования для внешних потребителей: главный плагин MCP:RSV Server
     * вызывает этот метод через рефлексию — чтобы запуск движка (и фикс области
     * анализа) жил в ОДНОМ месте, а не дублировался в двух плагинах.
     */
    public String analyzeToJson(File analyzeDir, File workingDir) throws IOException, InterruptedException {
        return Files.readString(runEngine(analyzeDir, workingDir), StandardCharsets.UTF_8);
    }

    /** Единая точка запуска движка отдельным процессом: возвращает путь к JSON-отчёту. */
    private Path runEngine(File analyzeDir, File workingDir) throws IOException, InterruptedException {
        File javaExe = resolveJava();
        File engineJar = resolveEngineJar();

        Path work = Files.createTempDirectory("mcp-rsv-codereview"); //$NON-NLS-1$
        Path reportsDir = work.resolve("reports");
        Files.createDirectories(reportsDir);
        Path config = writeConfig(work);

        List<String> command = new ArrayList<>();
        command.add(javaExe.getAbsolutePath());
        command.add("-jar");
        command.add(engineJar.getAbsolutePath());
        command.add("analyze");
        // ВАЖНО: -c (--configuration) — собственная опция команды analyze, не корневой.
        // Должна идти ПОСЛЕ "analyze", иначе конфиг не применится (диагностики не отключатся).
        command.add("-c");
        command.add(config.toString());
        command.add("-s");
        command.add(analyzeDir.getAbsolutePath());
        command.add("-r");
        command.add("json");
        command.add("-o");
        command.add(reportsDir.toString());

        ProcessBuilder pb = new ProcessBuilder(command);
        // -s сужается до папки целевого модуля, а рабочий каталог процесса остаётся
        // каталогом src проекта. Движок приводит пути файлов отчёта относительно
        // рабочего каталога; если он на другом диске, чем проект (C: vs D:), анализ
        // падает с "'other' has different root". Каталог на диске проекта это снимает,
        // а пути в отчёте остаются абсолютными.
        pb.directory(workingDir);
        pb.redirectErrorStream(true);
        pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
        Process process = pb.start();
        boolean finished = process.waitFor(TIMEOUT_MINUTES, TimeUnit.MINUTES);
        if (!finished) {
            process.destroyForcibly();
            throw new IOException("Анализ кода превысил лимит времени (" + TIMEOUT_MINUTES + " мин).");
        }

        Path report = reportsDir.resolve("bsl-json.json");
        if (!Files.exists(report)) {
            throw new IOException("Движок не сформировал отчёт (код возврата " + process.exitValue() + ").");
        }
        return report;
    }

    private File resolveJava() throws IOException {
        String javaHome = System.getProperty("java.home");
        if (javaHome == null) {
            throw new IOException("Не удалось определить java.home среды EDT.");
        }
        File bin = new File(javaHome, "bin");
        File exe = new File(bin, isWindows() ? "java.exe" : "java");
        if (!exe.isFile()) {
            throw new IOException("Не найден исполняемый файл Java: " + exe.getAbsolutePath());
        }
        return exe;
    }

    private File resolveEngineJar() throws IOException {
        Bundle bundle = Activator.getDefault().getBundle();
        Enumeration<URL> entries = bundle.findEntries(JAR_FOLDER, "*.jar", false);
        if (entries == null || !entries.hasMoreElements()) {
            throw new IOException("В компоненте не найден jar движка BSL Language Server (" + JAR_FOLDER + ").");
        }
        URL fileUrl = FileLocator.toFileURL(entries.nextElement());
        File jar = new File(fileUrl.getPath());
        if (!jar.isFile()) {
            throw new IOException("Недоступен jar движка: " + jar.getAbsolutePath());
        }
        return jar;
    }

    /** Генерирует {@code .bsl-language-server.json} из настроек (единый конфиг для EDT и MCP). */
    private Path writeConfig(Path dir) throws IOException {
        Path config = dir.resolve(".bsl-language-server.json");
        Files.writeString(config, CodeReviewConfig.effectiveConfigJson(), StandardCharsets.UTF_8);
        return config;
    }

    private static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }
}
