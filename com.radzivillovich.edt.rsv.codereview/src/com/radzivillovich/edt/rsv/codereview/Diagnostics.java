package com.radzivillovich.edt.rsv.codereview;

/**
 * Каталог управляемых диагностик код-ревью: код движка + русское название + состояние по умолчанию.
 * Используется страницей настроек (галочки) и при генерации конфигурации движка.
 * Диагностики вне этого каталога используют настройки движка по умолчанию.
 */
public final class Diagnostics {

    /** Префикс ключей в хранилище настроек: {@code diag.<КодДиагностики>}. */
    public static final String PREF_PREFIX = "diag."; //$NON-NLS-1$

    public static final class Entry {
        public final String code;
        public final String title;
        public final boolean defaultEnabled;

        public Entry(String code, String title, boolean defaultEnabled) {
            this.code = code;
            this.title = title;
            this.defaultEnabled = defaultEnabled;
        }

        public String prefKey() {
            return PREF_PREFIX + code;
        }
    }

    /**
     * Управляемый набор диагностик.
     * defaultEnabled=false для шумных/стилевых и контекстно-зависимых проверок: они
     * либо встречаются массово даже в типовых конфигурациях (длина строки, пробелы,
     * буква «ё»), либо требуют контекста всей конфигурации и при ревью одного модуля
     * дают ложные срабатывания (например «неиспользуемая переменная» на свойстве формы
     * «Модифицированность»). Их можно включить галочкой при необходимости.
     */
    public static final Entry[] ALL = {
        // --- Стандарты разработки / реальные замечания (по умолчанию включены) ---
        new Entry("CodeOutOfRegion", "Код вне области", true),
        new Entry("DataExchangeLoading", "Проверка ОбменДанными.Загрузка в начале процедуры", true),
        new Entry("DeprecatedMessage", "Устаревший метод \"Сообщить\"", true),
        new Entry("DeprecatedCurrentDate", "Устаревший метод \"ТекущаяДата\"", true),
        new Entry("DeprecatedMethods8317", "Устаревший метод платформы", true),
        new Entry("MagicNumber", "Магические числа", true),
        new Entry("CommonModuleMissingAPI", "Общий модуль без программного интерфейса", true),
        new Entry("CommonModuleNameServerCall", "Постфикс \"ВызовСервера\" у общего модуля", true),
        new Entry("CompilationDirectiveNeedLess", "Лишняя директива компиляции", true),
        new Entry("EmptyCodeBlock", "Пустой блок кода", true),
        new Entry("MissingCodeTryCatchEx", "Пустой блок \"Исключение\"", true),
        new Entry("CognitiveComplexity", "Когнитивная сложность", true),
        new Entry("CyclomaticComplexity", "Цикломатическая сложность", true),
        new Entry("MethodSize", "Слишком длинный метод", true),
        new Entry("NestedTernaryOperator", "Вложенный тернарный оператор", true),
        new Entry("IfElseIfEndsWithElse", "\"Если…ИначеЕсли\" без завершающего \"Иначе\"", true),
        new Entry("YoLetterUsage", "Использование буквы «ё»", true),
        new Entry("UsingHardcodePath", "Хардкод файловых путей", true),
        new Entry("UsingHardcodeNetworkAddress", "Хардкод сетевых адресов", true),

        // --- Шумные стилевые/форматирование (по умолчанию выключены) ---
        new Entry("LineLength", "Превышение максимальной длины строки", false),
        new Entry("InvalidCharacterInFile", "Недопустимый символ в файле (тире и пр.)", false),
        new Entry("MissingSpace", "Отсутствие пробелов вокруг операторов", false),
        new Entry("SpaceAtStartComment", "Пробел в начале текста комментария", false),
        new Entry("ConsecutiveEmptyLines", "Несколько пустых строк подряд", false),
        new Entry("SemicolonPresence", "Точка с запятой в конце операторов", false),
        new Entry("OneStatementPerLine", "Несколько операторов в одной строке", false),
        new Entry("NumberOfValuesInStructureConstructor", "Много значений в конструкторе Структуры", false),
        new Entry("DuplicateStringLiteral", "Повторяющиеся строковые литералы", false),
        new Entry("NestedFunctionInParameters", "Вложенные вызовы функций в параметрах", false),
        new Entry("MissingParameterDescription", "Нет описания параметра метода", false),

        // --- Контекстно-зависимые: при ревью одного модуля дают ложные срабатывания (выключены) ---
        new Entry("UnusedLocalVariable", "Неиспользуемая локальная переменная", false),
        new Entry("TransferringParametersBetweenClientAndServer", "Передача параметров между клиентом и сервером (Знач)", false),

        // --- Орфография: ложные срабатывания на терминах 1С (выключена) ---
        new Entry("Typo", "Орфография / опечатки", false),
    };

    private Diagnostics() {
    }
}
