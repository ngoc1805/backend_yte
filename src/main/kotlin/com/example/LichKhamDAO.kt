package com.example

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import java.time.format.DateTimeFormatter
import org.ktorm.database.Database
import org.ktorm.dsl.insertAndGenerateKey
import org.ktorm.schema.*
import java.time.LocalDate
import java.time.LocalTime


@Serializable
data class LichKham(
    val benhNhanId: Int,
    val bacSiId: Int,
    val ngayKham: String, // Định dạng: "dd/MM/yyyy"
    val gioKham: String,  // Định dạng: "1:00" hoặc "15:00"
    val trangThai: String
)
class LichKhamDAO(private val database: Database) {
    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    private val timeFormatter = DateTimeFormatter.ofPattern("H:mm")

    fun themLichKham(lichKham: LichKham): Int {
        val parsedNgayKham = LocalDate.parse(lichKham.ngayKham, dateFormatter)
        val parsedGioKham = LocalTime.parse(lichKham.gioKham, timeFormatter)

        return database.insertAndGenerateKey(LichKhamTable) { row ->
            set(row.benhNhanId, lichKham.benhNhanId)
            set(row.bacSiId, lichKham.bacSiId)
            set(row.ngayKham, parsedNgayKham)
            set(row.gioKham, parsedGioKham)
            set(row.trangThai, lichKham.trangThai)
        } as Int
    }
}
fun Route.themLichKham(){
    route("/post"){
        post("/add/lichkham") {
            val request = call.receive<LichKham>()
            try {
                val id = lichKhamDAO.themLichKham(request)
                call.respond(HttpStatusCode.Created, mapOf("id" to id))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Error: ${e.message}")
            }
        }
        }
    }
