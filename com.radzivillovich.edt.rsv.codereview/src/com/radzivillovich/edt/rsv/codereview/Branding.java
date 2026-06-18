package com.radzivillovich.edt.rsv.codereview;

import org.osgi.framework.Version;

/** Брендинг и внешние ссылки продукта — в одном месте, чтобы не расходились по файлам. */
public final class Branding {

    /** Репозиторий движка анализа (open-source, LGPL-3.0). */
    public static final String URL_ENGINE = "https://github.com/1c-syntax/bsl-language-server";
    /** Репозиторий этого компонента. */
    public static final String URL_REPO = "https://github.com/prepod2003/mcp-rsv-codereview";
    /** Сайт продукта MCP:RSV. */
    public static final String URL_SITE = "https://prepod2003.github.io/edt-ai-1c/";

    /** Версия компонента в виде «major.minor.micro» (без qualifier). */
    public static String version() {
        Version v = Activator.getDefault().getBundle().getVersion();
        return v.getMajor() + "." + v.getMinor() + "." + v.getMicro();
    }

    /**
     * HTML-кредит со ссылками для виджета {@link org.eclipse.swt.widgets.Link}: название
     * компонента, бренд MCP:RSV и обязательная по LGPL атрибуция движка BSL Language Server.
     */
    public static String creditHtml() {
        return "<a href=\"" + URL_REPO + "\">MCP:RSV Code Review " + version() + "</a>"
                + "  ·  от бренда <a href=\"" + URL_SITE + "\">MCP:RSV</a>"
                + "  ·  на базе <a href=\"" + URL_ENGINE + "\">BSL Language Server</a> (1c-syntax, LGPL-3.0)";
    }

    private Branding() {
    }
}
