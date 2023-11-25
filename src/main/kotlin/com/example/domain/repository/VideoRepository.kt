package com.example.domain.repository

import com.example.domain.model.Video
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

internal object Videos : Table() {
    val id = integer("id").autoIncrement()
    val name = varchar("name", length = 255)

    override val primaryKey = PrimaryKey(id)
}

class VideoRepository(private val database: Database) {
    init {
        transaction(database) {
            SchemaUtils.create(Videos)
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    suspend fun create(video: Video): Int = dbQuery {
        Videos.insert {
            it[name] = video.name
        }[Videos.id]
    }

    suspend fun read(id: Int): Video? = dbQuery {
        Videos.select { Videos.id eq id }
            .map { Video(it[Videos.id], it[Videos.name]) }
            .singleOrNull()
    }

    suspend fun readAll(limit: Int, offset: Long): List<Video> = dbQuery {
        Videos.selectAll()
            .limit(limit, offset)
            .map { Video(it[Videos.id], it[Videos.name]) }
    }
}
