## Лабораторная работа № 3 по курсу ИППРПО
- Студент группы ИТП-31 Крогаль Кирилл Дмитриевич
- Вариант № 6 Анализатор логов Git
- Статус прохождения тестов: [![CI](https://github.com/KrogalKirill/ISDST-Lab3/actions/workflows/ci.yml/badge.svg)](https://github.com/KrogalKirill/ISDST-Lab3/actions/workflows/ci.yml)

Описание: Приложение запускает git log в текущем репозитории и анализирует вывод, чтобы построить статистику по коммитам.

Функционал:

Выполнение команды git log
Задание 1 (Агрегация): подсчитать общее количество коммитов на каждого автора и вывести топ-3
Задание 2 (Поиск): найти все коммиты (хэш и сообщение), содержащие ключевые слова (например, "FIX:", "BUG:", "HOTFIX:"), список слов берется из app.properties.
Задание 3 (Сортировка): вывести список всех авторов (уникальных) в алфавитном порядке.
Формат входных данных:

a1b2c3d|Alice|FIX: Critical bug in login
e4f5g6h|Bob|Initial commit
i7j8k9l|Alice|Update README
Формат выходных данных: JSON-отчет в stdout

{
  "top_authors": [
    {"name": "Alice", "commits": 2},
    {"name": "Bob", "commits": 1}
  ],
  "found_keywords": [
    {"hash": "a1b2c3d", "message": "FIX: Critical bug in login"}
  ],
  "all_authors": ["Alice", "Bob"]
}
Пример файла конфигурации: (app.properties)

Ключевые слова для поиска (через запятую):
git.search.keywords=FIX:,BUG:,HOTFIX:

Формат вывода: JSON или PLAINTEXT:
output.format=JSON



Доп. задачи к CI/CD:

Публикация JAR: собрать jar и опубликовать как артефакт
Сборка на разных ОС: настроить matrix для сборки на ubuntu-latest, windows-latest и macos-latest


## Инструкция для развертки приложения

Для начала необходимо открыть командную строку для этого необходимо нажать комбинацию клавиш

- WIN + R

И в открывшемся окне написать 

```bash
cmd
```

Далее необходимо создать директорию для хранения проекта, для этого в открывшейся командной строке необходимо написать

```bash
mkdir lab3
```

И перейти в неё, написав

```bash
cd lab3
```

Далее нужно скачать проект с удаленного репозитория, для этого в командную строку нужно ввести

```bash
git clone https://github.com/KrogalKirill/ISDST-Lab3.git
```

И перейти в репозиторий проекта

```bash
cd ISDST-Lab3
```

Перед запуском приложения необходимо создать и заполнить файл app.properties, для этого необходимл ввести

```bash
echo git.search.keywords=Initialization,Fill,HOTFIX > app.properties && echo output.format=JSON >> app.properties
```

Теперь необходимо выполнить команду 

```bash
gradlew build
```

После загрузки необходимых файлов, проект готов к запуску, сделать это можно командой

```bash
gradlew run
```
