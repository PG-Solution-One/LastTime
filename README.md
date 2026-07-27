# Last Time

[![Version](https://img.shields.io/badge/version-1.0.0-256D4A)](https://github.com/PG-Solution-One/LastTime)
[![Android CI](https://github.com/PG-Solution-One/LastTime/actions/workflows/android-ci.yml/badge.svg)](https://github.com/PG-Solution-One/LastTime/actions/workflows/android-ci.yml)
[![Build APK](https://github.com/PG-Solution-One/LastTime/actions/workflows/build-apk.yml/badge.svg)](https://github.com/PG-Solution-One/LastTime/actions/workflows/build-apk.yml)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.3.21-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)

«Когда последний раз?» — Android-приложение для учёта редких событий:
обслуживания автомобиля, домашних дел, визитов к врачу и других задач,
которые легко забыть.

Приложение показывает, сколько времени прошло после события, рассчитывает
следующую дату и заранее отправляет локальное напоминание. Данные хранятся
только на устройстве в Room (SQLite); аккаунт и подключение к сети не нужны.

## Возможности

- карточки событий с категорией, интервалом и заметкой;
- расчёт прошедшего времени, следующей даты и просрочки;
- редактируемая история выполнений;
- локальные уведомления;
- системная, светлая и тёмная темы;
- русский и английский интерфейс.

## Системные требования

- Android 8.0 (API 26) или новее;
- JDK 17;
- Android SDK 36.1;
- Android Studio или командная строка с Android SDK.

Проект компилируется с SDK 36.1 и использует `targetSdk 36`. Идентификатор
приложения и корневой Kotlin-пакет — `app.lasttime`.

## Сборка

Собрать debug APK:

```bash
./gradlew assembleDebug
```

Готовый файл будет находиться по адресу:
`app/build/outputs/apk/debug/app-debug.apk`.

Собрать release-вариант:

```bash
./gradlew assembleRelease
```

Release APK без настроенной подписи не предназначен для публикации.

## Установка и запуск

Подключить Android-устройство или запустить эмулятор, затем выполнить:

```bash
./gradlew installDebug
adb shell am start -n app.lasttime/.MainActivity
```

Устройство должно отображаться в выводе:

```bash
adb devices
```

В Android Studio приложение можно запустить обычной командой **Run**.

## Тесты и проверки

Локальные unit-тесты:

```bash
./gradlew testDebugUnitTest
```

Инструментальные тесты на подключённом устройстве или эмуляторе:

```bash
./gradlew connectedDebugAndroidTest
```

Проверка форматирования и Android Lint:

```bash
./gradlew ktlintCheck lintDebug
```

Полная проверка, соответствующая CI:

```bash
./gradlew ktlintCheck testDebugUnitTest compileDebugAndroidTestKotlin lintDebug assembleDebug
```

Автоматически исправить форматирование Kotlin:

```bash
./gradlew ktlintFormat
```

Очистить результаты сборки:

```bash
./gradlew clean
```

Посмотреть все доступные задачи:

```bash
./gradlew tasks
```

## Стек

Kotlin, Jetpack Compose, Material 3, Room, WorkManager, DataStore и Navigation
Compose.

Схемы Room находятся в `app/schemas` и хранятся в Git для проверки будущих
миграций базы данных.
