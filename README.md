# MCP:RSV Code Review

Бесплатное автоматическое **код-ревью BSL** прямо в 1C:EDT: стандарты разработки 1С, сложность кода, code smells. Правый клик по модулю или проекту → **MCP:RSV → Проверить код** → замечания в панели Problems и подсветкой в редакторе.

Продукт от **MCP:RSV** — [сайт и платный плагин для ИИ-агентов](https://prepod2003.github.io/edt-ai-1c/).

📖 **Подробная инструкция** — установка, настройка и использование, с примерами и скриншотами: **[Руководство пользователя →](https://prepod2003.github.io/edt-ai-1c/guide.html#code-review)**

## На базе BSL Language Server (1c-syntax)

Движок анализа — open-source **[BSL Language Server](https://github.com/1c-syntax/bsl-language-server)** команды **1c-syntax** (© 2018–present Alexey Sosnoviy, Nikita Fedkin и контрибьюторы), лицензия **GNU LGPL-3.0-or-later**.

Мы упаковываем и интегрируем движок, **не модифицируя** его. Движок поставляется отдельным заменяемым jar-файлом. Тексты лицензий и атрибуция — в каталоге `licenses/` дистрибутива.

- Исходники движка: https://github.com/1c-syntax/bsl-language-server
- Лицензия движка: https://www.gnu.org/licenses/lgpl-3.0.txt

## Лицензия

- **Код этого проекта** (MCP:RSV Code Review) — **GNU GPL-3.0-or-later**, см. файл [`LICENSE`](LICENSE) (пояснение на русском — [`LICENSE.ru.md`](LICENSE.ru.md)). © 2026 Radzivillovich Sergey. Пользоваться можно свободно и бесплатно; любой форк/производное при распространении обязан оставаться открытым под GPL и сохранять авторство.
- **Встроенный движок** BSL Language Server (1c-syntax) — отдельная лицензия **GNU LGPL-3.0-or-later**, на него GPL нашего кода не распространяется. Тексты лицензий и атрибуция — в `com.radzivillovich.edt.rsv.codereview/licenses/` ([THIRD-PARTY.md](com.radzivillovich.edt.rsv.codereview/licenses/THIRD-PARTY.md), LGPL-3.0.txt, GPL-3.0.txt); эти же файлы входят в дистрибутив компонента.

## Установка

1. Скачайте последнюю сборку (один установочный ZIP, всегда актуальная версия):
   **[⬇ MCP-RSV-CodeReview.zip](https://github.com/prepod2003/mcp-rsv-codereview/releases/latest/download/MCP-RSV-CodeReview.zip)**
2. В 1C:EDT: **Help → Install New Software → Add → Archive** → выбрать скачанный ZIP → установить категорию **MCP:RSV Code Review** → перезапустить EDT.
3. Правый клик по модулю/проекту → **MCP:RSV → Проверить код**.

Пошаговая инструкция с примерами и скриншотами — в [руководстве пользователя](https://prepod2003.github.io/edt-ai-1c/guide.html#code-review).

## Системные требования

- 1C:EDT (Java 17+, движок запускается той Java, на которой работает EDT).

## Сборка из исходников

Движок (`bsl-language-server-*-exec.jar`, версия под Java 17 — 0.28.x) положить в
`com.radzivillovich.edt.rsv.codereview/resources/bsl-language-server/`, затем:

```
mvn clean verify
```

Готовый update site — в `repository/target/`.
