package com.radzivillovich.edt.rsv.codereview.internal;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Разбор JSON-отчёта BSL Language Server (репортер {@code json}, файл {@code bsl-json.json}).
 * Формат: {@code {"fileinfos":[{"path":"file:///...","diagnostics":[{...}]}]}}.
 */
public final class JsonReportParser {

    private JsonReportParser() {
    }

    public static List<Finding> parse(Path reportJson) throws IOException {
        List<Finding> findings = new ArrayList<>();
        String text = Files.readString(reportJson, StandardCharsets.UTF_8);
        JsonElement rootEl = JsonParser.parseString(text);
        if (!rootEl.isJsonObject()) {
            return findings;
        }
        JsonObject root = rootEl.getAsJsonObject();
        JsonArray fileInfos = root.getAsJsonArray("fileinfos");
        if (fileInfos == null) {
            return findings;
        }
        for (JsonElement fiEl : fileInfos) {
            JsonObject fi = fiEl.getAsJsonObject();
            String path = optString(fi, "path");
            if (path == null) {
                continue;
            }
            String absolute = toAbsolutePath(path);
            JsonElement diagsEl = fi.get("diagnostics");
            if (diagsEl == null || !diagsEl.isJsonArray()) {
                continue;
            }
            for (JsonElement dEl : diagsEl.getAsJsonArray()) {
                JsonObject d = dEl.getAsJsonObject();
                int line = 0;
                int character = 0;
                int endLine = 0;
                int endCharacter = 0;
                JsonObject range = d.getAsJsonObject("range");
                if (range != null) {
                    JsonObject start = range.getAsJsonObject("start");
                    if (start != null) {
                        line = optInt(start, "line", 0);
                        character = optInt(start, "character", 0);
                    }
                    JsonObject end = range.getAsJsonObject("end");
                    if (end != null) {
                        endLine = optInt(end, "line", line);
                        endCharacter = optInt(end, "character", character);
                    } else {
                        endLine = line;
                        endCharacter = character;
                    }
                }
                String href = null;
                JsonObject codeDesc = d.getAsJsonObject("codeDescription");
                if (codeDesc != null) {
                    href = optString(codeDesc, "href");
                }
                findings.add(new Finding(
                        absolute,
                        line,
                        character,
                        endLine,
                        endCharacter,
                        optString(d, "severity"),
                        optString(d, "code"),
                        optString(d, "message"),
                        href));
            }
        }
        return findings;
    }

    private static String toAbsolutePath(String pathOrUri) {
        try {
            if (pathOrUri.startsWith("file:")) {
                return Paths.get(URI.create(pathOrUri)).toString();
            }
        } catch (RuntimeException ignored) {
            // не валидный URI — вернём как есть
        }
        return pathOrUri;
    }

    private static String optString(JsonObject obj, String key) {
        JsonElement el = obj.get(key);
        return (el != null && !el.isJsonNull()) ? el.getAsString() : null;
    }

    private static int optInt(JsonObject obj, String key, int def) {
        JsonElement el = obj.get(key);
        return (el != null && !el.isJsonNull()) ? el.getAsInt() : def;
    }
}
