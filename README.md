# Video streaming

## Requirements
- docker, docker-compose
- kotlin
- gradle

## Description
Минимальный рабочий пример загрузки/просмотра видео файлов:
- реализована работа только с mp4 файлами (пример минимальный)
- загрузка больших файлов от клиента в S3 частями (multipart form data)
- стриминг больших файлов, используя Content-Range заголовок
- endpoint для просмотра списка сохраненных в H2 видео-файлов

## Build
```shell
gradle build
```

## Run
```shell
docker-compose -f docker/docker-compose.yaml up -d
gradle run
```
