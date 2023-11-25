package com.example.domain.service

import io.minio.*
import io.minio.errors.ErrorResponseException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream

class MinioVideoService(private val client: MinioClient) {
    private val contentType = "video/mp4"
    private val bucketName = "video-bucket"
    private val uploadPartSize = 1024L * 1024L * 10L

    suspend fun upload(objectName: String, inputStream: InputStream) = withContext(Dispatchers.IO) {
        val bucketExists = client.bucketExists(
            BucketExistsArgs.builder().bucket(bucketName).build()
        )
        if (!bucketExists) {
            client.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build())
        }
        val uArgs = PutObjectArgs.builder()
            .bucket(bucketName)
            .`object`(objectName)
            .stream(inputStream, -1, uploadPartSize)
            .contentType(contentType)
            .build()
        client.putObject(uArgs)
        inputStream.close()
    }

    suspend fun getObjectStats(objectName: String): StatObjectResponse? = withContext(Dispatchers.IO) {
        try {
            return@withContext client.statObject(
                StatObjectArgs.builder()
                    .bucket(bucketName)
                    .`object`(objectName)
                    .build()
            )
        } catch (e: ErrorResponseException) {
            return@withContext null
        }
    }

    suspend fun getObjectWithRange(objectName: String, offset: Long, length: Long): InputStream? =
        withContext(Dispatchers.IO) {
            try {
                return@withContext client.getObject(
                    GetObjectArgs.builder()
                        .bucket(bucketName)
                        .`object`(objectName)
                        .offset(offset)
                        .length(length)
                        .build()
                )
            } catch (e: ErrorResponseException) {
                return@withContext null
            }
        }
}
