package com.example.config

import com.example.domain.repository.VideoRepository
import com.example.domain.service.MinioVideoService
import io.minio.MinioClient
import org.jetbrains.exposed.sql.Database

class AppConfig {
    val minioClient = MinioClient.builder()
        .endpoint("http://127.0.0.1:9000")
        .credentials("user", "password")
        .build()
    val database = Database.connect(
        url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
        user = "root",
        driver = "org.h2.Driver",
        password = ""
    )
    val minioVideoService = MinioVideoService(minioClient)
    val videoRepository = VideoRepository(database)
}
