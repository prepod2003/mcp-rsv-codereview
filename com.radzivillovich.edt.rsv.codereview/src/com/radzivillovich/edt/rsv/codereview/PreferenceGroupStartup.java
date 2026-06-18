package com.radzivillovich.edt.rsv.codereview;

import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.PlatformUI;

import com.radzivillovich.edt.rsv.codereview.prefs.GroupPreferencePage;

/**
 * Размещает страницу настроек «Код-ревью» под группой «MCP:RSV», даже когда основной
 * плагин MCP:RSV не установлен.
 *
 * <p>Декларативно страница ссылается на категорию основного плагина
 * ({@code ...preferences.ToolGroups}). Если основной плагин есть — платформа сама
 * вкладывает страницу в его группу «MCP:RSV» (одна общая группа, без дублей).
 * Если основного плагина нет — категория не находится, и платформа кладёт страницу
 * на верхний уровень дерева настроек. Этот хук в таком случае создаёт собственный
 * узел-группу «MCP:RSV» и переносит страницу под него.
 *
 * <p>Дерево настроек платформа строит заново при каждом запуске, поэтому состояние
 * между сессиями не сохраняется: при следующем старте всё определяется текущим
 * составом установленных плагинов.
 */
public final class PreferenceGroupStartup implements IStartup {

    /** Группа «MCP:RSV», которую создаёт основной плагин. */
    private static final String MAIN_GROUP_ID = "com.radzivillovich.edt.rsv.preferences.ToolGroups";
    /** Собственная группа-контейнер (используется только без основного плагина). */
    private static final String OWN_GROUP_ID = "com.radzivillovich.edt.rsv.codereview.preferences.Group";
    /** Страница «Код-ревью». */
    private static final String PAGE_ID = "com.radzivillovich.edt.rsv.codereview.preferences.CodeReview";

    @Override
    public void earlyStartup() {
        if (!PlatformUI.isWorkbenchRunning()) {
            return;
        }
        PlatformUI.getWorkbench().getDisplay().asyncExec(PreferenceGroupStartup::placeUnderGroup);
    }

    private static void placeUnderGroup() {
        try {
            if (!PlatformUI.isWorkbenchRunning()) {
                return;
            }
            PreferenceManager pm = PlatformUI.getWorkbench().getPreferenceManager();
            if (pm == null) {
                return;
            }
            // Группа основного плагина есть → страница уже вложена в неё декларативно.
            if (findRootNode(pm, MAIN_GROUP_ID) != null) {
                return;
            }
            // Своя группа уже создана (повторный вызов) — ничего не делаем.
            if (findRootNode(pm, OWN_GROUP_ID) != null) {
                return;
            }
            // Страница лежит на верхнем уровне (категория не нашлась) — переносим под свою группу.
            IPreferenceNode page = findRootNode(pm, PAGE_ID);
            if (page == null) {
                return;
            }
            pm.remove(page);
            PreferenceNode group = new PreferenceNode(OWN_GROUP_ID, new GroupPreferencePage());
            group.add(page);
            pm.addToRoot(group);
        } catch (Throwable ignored) {
            // Косметика дерева настроек — сбой не должен мешать работе EDT.
        }
    }

    /** Ищет узел верхнего уровня по точному id (find() платформы бьёт путь по сепаратору — не годится для id с точками). */
    private static IPreferenceNode findRootNode(PreferenceManager pm, String id) {
        for (IPreferenceNode node : pm.getRootSubNodes()) {
            if (id.equals(node.getId())) {
                return node;
            }
        }
        return null;
    }
}
