# Сторонние компоненты

## BSL Language Server

Компонент **MCP:RSV Code Review** использует движок статического анализа **BSL Language Server**
для проверки кода BSL. Движок поставляется в составе компонента отдельным jar-файлом
(`resources/bsl-language-server/bsl-language-server-0.28.5-exec.jar`) и **не модифицирован**.

- **Компонент:** BSL Language Server, версия **0.28.5**.
- **Авторы:** © 2018–present Alexey Sosnoviy `<labotamy@gmail.com>`, Nikita Fedkin `<nixel2007@gmail.com>` and contributors.
- **Лицензия:** GNU LGPL-3.0-or-later (полные тексты — [LGPL-3.0.txt](LGPL-3.0.txt) и [GPL-3.0.txt](GPL-3.0.txt) в этом каталоге).
- **Исходный код проекта:** https://github.com/1c-syntax/bsl-language-server
- **Исходный код и бинарники версии 0.28.5:** https://github.com/1c-syntax/bsl-language-server/releases/tag/v0.28.5

В соответствии с LGPL-3.0:
- движок не модифицирован нами; его собственные copyright-уведомления сохранены;
- jar лежит отдельным **заменяемым** файлом — пользователь вправе заменить его на свою или
  модифицированную сборку BSL Language Server;
- полные тексты LGPL-3.0 и GPL-3.0 приложены рядом; исходный код движка доступен по ссылке выше.

---

Код самого компонента **MCP:RSV Code Review** распространяется под лицензией
**GNU GPL-3.0-or-later** (см. файл `LICENSE` в корне проекта и `GPL-3.0.txt` рядом) — это
ОТДЕЛЬНАЯ лицензия на наш код, не путать с LGPL встроенного движка.
© 2026 Radzivillovich Sergey. Сайт: https://prepod2003.github.io/edt-ai-1c/
