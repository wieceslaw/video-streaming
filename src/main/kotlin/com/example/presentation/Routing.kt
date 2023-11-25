package com.example.presentation

import com.example.config.AppConfig
import com.example.domain.model.Video
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.partialcontent.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.PipedInputStream
import java.io.PipedOutputStream

fun Application.configureRouting(config: AppConfig) {
    val streamPartSize = 1024L * 1024L
    val minioVideoService = config.minioVideoService
    val videoRepository = config.videoRepository

    install(PartialContent)
    install(ContentNegotiation) {
        json()
    }

    routing {
        staticResources("/", "static") {
            default("static/index.html")
        }
        resource("/view", "static/view.html")
        resource("/upload", "static/form.html")

        get("/download/{video-id}") {
            val objectName = call.parameters.getOrFail("video-id")
            val objectStat = minioVideoService.getObjectStats(objectName)
            if (objectStat == null) {
                call.respond(HttpStatusCode.NotFound)
                return@get
            }
            val objectSize = objectStat.size()
            val range = call.request.ranges()?.ranges?.get(0)
            if (range !is ContentRange.TailFrom) {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }
            val rangeBegin = range.from
            val rangeEnd = Math.min(objectSize, rangeBegin + streamPartSize)
            val inputStream = minioVideoService.getObjectWithRange(objectName, rangeBegin, rangeEnd - rangeBegin)
            if (inputStream == null) {
                call.respond(HttpStatusCode.NotFound)
                return@get
            }
            val bytes = inputStream.readBytes()
            val contentLength = bytes.size.toString()
            val contentRange = "bytes ${rangeBegin}-${rangeEnd - 1}/${objectSize}"
            call.response.header(HttpHeaders.AcceptRanges, "bytes")
            call.response.header(
                HttpHeaders.ContentDisposition,
                ContentDisposition.Attachment.withParameter(
                    ContentDisposition.Parameters.FileName, objectName
                ).toString()
            )
            call.response.header(HttpHeaders.ContentLength, contentLength)
            call.response.header(HttpHeaders.ContentRange, contentRange)
            call.respondBytes(bytes, ContentType.Video.MP4, HttpStatusCode.PartialContent)
        }

        post("/upload") {
            val pis = PipedInputStream()
            val pos = PipedOutputStream()
            pos.connect(pis)
            val multipart = call.receiveMultipart()
            val filename = call.parameters.getOrFail("filename")
            val videoId = videoRepository.create(Video(name = filename))
            launch { minioVideoService.upload(videoId.toString(), pis) }
            multipart.forEachPart { part ->
                if (part is PartData.FileItem) {
                    withContext(Dispatchers.IO) {
                        part.streamProvider().copyTo(pos)
                    }
                }
                part.dispose()
            }
            pos.close()
            call.respond(HttpStatusCode.OK, videoId)
        }

        get("/videos") {
            val limit = call.parameters["limit"]?.toIntOrNull() ?: 100
            val offset = call.parameters["offset"]?.toLongOrNull() ?: 0
            call.respond(videoRepository.readAll(limit, offset))
        }
    }
}
