package ru.avsh.lib;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Этот класс предназначен для поиска файлов.
 *
 * @author Стаценко Владимир
 * http://www.vova-prog.narod.ru
 */
public class cFileFinder {
    //константы для определения объектов, которые нужно найти
    private final int FILES       = 0;
    private final int DIRECTORIES = 1;
    private final int ALL         = 2;
    //классы для работы с регулярными выражениями
    private Pattern p = null;
    private Matcher m = null;
    //общий размер найденных файлов
    private long totalLength = 0;
    //общее количество найденных файлов
    private long filesNumber = 0;
    //общее количество просмотренных директорий
    private long directoriesNumber = 0;

    /**
     * Этот метод выполняет поиск всех объектов (файлов и директорий),
     * начиная с заданной директории (startPath).
     *
     * @param startPath Начальная директория поиска
     * @return Список (List) найденных объектов
     */
    public List<File> findAll(String startPath) {
        return find(startPath, "", ALL);
    }

    /**
     * Этот метод выполняет поиск объектов (файлов и директорий),
     * которые соответствуют заданному регулярному выражению (mask),
     * начиная с заданной директории (startPath).
     *
     * @param startPath Начальная директория поиска
     * @param mask      регулярное выражение, которому должны соответствовать имена найденный объектов
     * @return Список (List) найденных объектов
     */
    public List<File> findAll(String startPath, String mask) {
        return find(startPath, mask, ALL);
    }

    /**
     * Этот метод выполняет поиск всех файлов,
     * начиная с заданной директории (startPath).
     *
     * @param startPath Начальная директория поиска
     * @return Список (List) найденных объектов
     */
    public List<File> findFiles(String startPath) {
        return find(startPath, "", FILES);
    }

    /**
     * Этот метод выполняет поиск файлов,
     * которые соответствуют заданному регулярному выражению (mask),
     * начиная с заданной директории (startPath).
     *
     * @param startPath Начальная директория поиска
     * @param mask      регулярное выражение, которому должны соответствовать имена найденный объектов
     * @return Список (List) найденных объектов
     */
    public List<File> findFiles(String startPath, String mask) {
        return find(startPath, mask, FILES);
    }

    /**
     * Этот метод выполняет поиск всех директорий (папок),
     * начиная с заданной директории (startPath).
     *
     * @param startPath Начальная директория поиска
     * @return Список (List) найденных объектов
     */
    public List<File> findDirectories(String startPath) {
        return find(startPath, "", DIRECTORIES);
    }

    /**
     * Этот метод выполняет поиск директорий (папок),
     * которые соответствуют заданному регулярному выражению (mask),
     * начиная с заданной директории (startPath).
     *
     * @param startPath Начальная директория поиска
     * @param mask      регулярное выражение, которому должны соответствовать имена найденный объектов
     * @return Список (List) найденных объектов
     */
    public List<File> findDirectories(String startPath, String mask) {
        return find(startPath, mask, DIRECTORIES);
    }

    /**
     * Возвращает суммарный размер найденных файлов.
     *
     * @return размер найденных файлов (байт)
     */
    public long getDirectorySize() {
        return totalLength;
    }

    /**
     * Возвращает общее количество найденных файлов.
     *
     * @return количество найденных файлов
     */
    public long getFilesNumber() {
        return filesNumber;
    }

    /**
     * Возвращает общее количество найденных директорий (папок).
     *
     * @return количество найденных директорий (папок)
     */
    public long getDirectoriesNumber() {
        return directoriesNumber;
    }

    /**
     * Проверяет, соответствует ли имя файла заданному регулярному выражению.
     *
     * @param name имя файла
     * @return true, если найденный объект соответствует регулярному выражению
     */
    private boolean accept(String name) {
        //если регулярное выражение не задано...
        if (p == null) {
            //...значит объект подходит
            return true;
        }
        //создаем Matcher
        m = p.matcher(name);
        //выполняем проверку
        return m.matches();
    }

    /**
     * Этот метод выполняет начальные установки поиска, затем вызывает метод search для выполнения поиска.
     *
     * @param startPath  начальная директория для поиска
     * @param mask       регулярное выражение, которому должны соответствовать имена найденный объектов
     * @param objectType объект для поиска
     * @return список (List) найденных объектов
     */
    private List<File> find(String startPath, String mask, int objectType) {
        List<File> res = new ArrayList<>(100);
        //проверка параметров
        if ((startPath != null) && (mask != null)) {
            File topDir = new File(startPath);
            //проверка начальной директории
            if (topDir.exists()) {
                //если задано регулярное выражение, создаем Pattern
                if (!mask.equals("")) {
                    p = Pattern.compile(mask, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
                }
                //обнуляем все счетчики
                filesNumber       = 0;
                directoriesNumber = 0;
                totalLength       = 0;
                //выполняем поиск
                search(topDir, res, objectType);
                //присваиваем null шаблону, т.к. при следующем вызове find...
                //регулярное выражение может быть не задано
                p = null;
            }
        }
        //возвращаем результат
        return res;
    }

    /**
     * Этот метод выполняет поиск объектов заданного типа.
     * Если, в процессе поиска, встречает вложенную директорию (папку), то рекурсивно вызывает сам себя.
     *
     * @param topDirectory текущая директория
     * @param res          результат поиска
     * @param objectType   объект для поиска
     */
    private void search(File topDirectory, List<File> res, int objectType) {
        //получаем список всех объектов в текущей директории
        File[] list = topDirectory.listFiles();
        if (list != null) {
            //просматриваем все объекты по-очереди
            for (File file : list) {
                //если это директория (папка)...
                if (file.isDirectory()) {
                    //...выполняем проверку на соответствие типу объекта
                    // и регулярному выражению...
                    if ((objectType != FILES) && accept(file.getName())) {
                        //...добавляем текущий объект в список результатов,
                        //и обновляем значения счетчиков
                        directoriesNumber++;
                        res.add(file);
                    }
                    //выполняем поиск во вложенных директориях
                    search(file, res, objectType);
                }
                //если это файл
                else {
                    //...выполняем проверку на соответствие типу объекта
                    // и регулярному выражению...
                    if ((objectType != DIRECTORIES) && accept(file.getName())) {
                        //...добавляем текущий объект в список результатов,
                        //и обновляем значения счетчиков
                        filesNumber++;
                        totalLength += file.length();
                        res.add(file);
                    }
                }
            }
        }
    }
}