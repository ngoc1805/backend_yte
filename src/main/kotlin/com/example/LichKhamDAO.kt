package com.example

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import java.time.format.DateTimeFormatter
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.schema.*
import java.time.LocalDate
import java.time.LocalTime


@Serializable
data class LichKham(
    val idLichKham: Int ? = null,
    val idBenhNhan: String,
    val idBacSi: String,
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
            set(row.idBenhNhan, lichKham.idBenhNhan)
            set(row.idBacSi, lichKham.idBacSi)
            set(row.ngayKham, parsedNgayKham)
            set(row.gioKham, parsedGioKham)
            set(row.trangThai, lichKham.trangThai)
        } as Int
    }
    fun getLichHenByIdNguoiDung(idBenhNhan: String): List<LichKham> {
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd") // Format lưu trong DB
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")   // Format giờ lưu trong DB

        return database.from(LichKhamTable)
            .select()
            .orderBy(LichKhamTable.idLichKham.desc())
            .where { (LichKhamTable.idBenhNhan eq idBenhNhan) and
                    ((LichKhamTable.trangThai eq "Đã lên lịch") or (LichKhamTable.trangThai eq "Đã thanh toán") or (LichKhamTable.trangThai eq "y/c thanh toán")) }
            .map { row ->
                LichKham(
                    idLichKham = row[LichKhamTable.idLichKham] ?: 0,
                    idBenhNhan = row[LichKhamTable.idBenhNhan] ?: "",
                    idBacSi = row[LichKhamTable.idBacSi] ?: "",
                    ngayKham = row[LichKhamTable.ngayKham]?.format(dateFormatter) ?: "",
                    gioKham = row[LichKhamTable.gioKham]?.format(timeFormatter) ?: "",
                    trangThai = row[LichKhamTable.trangThai] ?: ""
                )
            }
    }
    fun getallLichHenByIdNguoiDung(idBenhNhan: String): List<LichKham> {
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd") // Format lưu trong DB
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")   // Format giờ lưu trong DB

        return database.from(LichKhamTable)
            .select()
            .orderBy(LichKhamTable.idLichKham.desc())
            .where { LichKhamTable.idBenhNhan eq idBenhNhan }
            .map { row ->
                LichKham(
                    idLichKham = row[LichKhamTable.idLichKham] ?: 0,
                    idBenhNhan = row[LichKhamTable.idBenhNhan] ?: "",
                    idBacSi = row[LichKhamTable.idBacSi] ?: "",
                    ngayKham = row[LichKhamTable.ngayKham]?.format(dateFormatter) ?: "",
                    gioKham = row[LichKhamTable.gioKham]?.format(timeFormatter) ?: "",
                    trangThai = row[LichKhamTable.trangThai] ?: ""
                )
            }
    }
    fun capNhatTrangThai(lichKhamId: Int, trangThai: String): Boolean {
        val updatedRows = database.update(LichKhamTable) {
            set(LichKhamTable.trangThai, trangThai)
            where {
                LichKhamTable.idLichKham eq lichKhamId
            }
        }
        return updatedRows > 0 // Trả về true nếu có ít nhất 1 dòng được cập nhật
    }
    fun kiemTraLichKham(
        idBacSi: String,
        ngayKham: String,
        gioKham: String
    ): Boolean {
        val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val timeFormatter = DateTimeFormatter.ofPattern("H:mm")

        val parsedNgayKham = LocalDate.parse(ngayKham, dateFormatter)
        val parsedGioKham = LocalTime.parse(gioKham, timeFormatter)

        val count = database.from(LichKhamTable)
            .select()
            .where {
                (LichKhamTable.idBacSi eq idBacSi) and
                        (LichKhamTable.ngayKham eq parsedNgayKham) and
                        (LichKhamTable.gioKham eq parsedGioKham) and
                        (LichKhamTable.trangThai eq "Đã lên lịch")
            }
            .totalRecords // Tổng số bản ghi khớp điều kiện

        return count == 0 // Trả về true nếu không có bản ghi nào, false nếu có
    }

    fun getLichKhamByIdBacSiToday(idBacSi: String): List<LichKham> {
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd") // Định dạng ngày lưu trong DB
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")  // Định dạng giờ lưu trong DB

        val today = LocalDate.now()  // Lấy ngày hôm nay

        return database.from(LichKhamTable)
            .select()
            .where {
                (LichKhamTable.idBacSi eq idBacSi) and
                        (LichKhamTable.ngayKham eq today) and
                        ((LichKhamTable.trangThai eq "Đã lên lịch") or (LichKhamTable.trangThai eq "Đã thanh toán") or (LichKhamTable.trangThai eq "y/c thanh toán"))
            }
            .orderBy(LichKhamTable.gioKham.asc())
            .map { row ->
                LichKham(
                    idLichKham = row[LichKhamTable.idLichKham] ?: 0,
                    idBenhNhan = row[LichKhamTable.idBenhNhan] ?: "",
                    idBacSi = row[LichKhamTable.idBacSi] ?: "",
                    ngayKham = row[LichKhamTable.ngayKham]?.format(dateFormatter) ?: "",
                    gioKham = row[LichKhamTable.gioKham]?.format(timeFormatter) ?: "",
                    trangThai = row[LichKhamTable.trangThai] ?: ""
                )
            }

    }

    // Lịch khám toàn bộ của bác sĩ
    fun getLichKhamToanBoCuaBacSi(idBacSi: String): List<LichKham> {
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd") // Format lưu trong DB
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")   // Format giờ lưu trong DB

        return database.from(LichKhamTable)
            .select()
            .where { (LichKhamTable.idBacSi eq idBacSi) and (LichKhamTable.trangThai eq "Đã lên lịch") }
            .orderBy(LichKhamTable.ngayKham.desc())  // Sắp xếp theo ngày khám (có thể thêm điều kiện theo yêu cầu)
            .map { row ->
                LichKham(
                    idLichKham = row[LichKhamTable.idLichKham] ?: 0,
                    idBenhNhan = row[LichKhamTable.idBenhNhan] ?: "",
                    idBacSi = row[LichKhamTable.idBacSi] ?: "",
                    ngayKham = row[LichKhamTable.ngayKham]?.format(dateFormatter) ?: "",
                    gioKham = row[LichKhamTable.gioKham]?.format(timeFormatter) ?: "",
                    trangThai = row[LichKhamTable.trangThai] ?: ""
                )
            }
    }
    fun getIdBenhNhanByIdLichKham(idLichKhamList: List<Int>): List<String> {

        return database.from(LichKhamTable)
            .select(LichKhamTable.idBenhNhan)
            .where { (LichKhamTable.idLichKham inList idLichKhamList) and (LichKhamTable.trangThai eq "Đã thanh toán") }
            .map { row -> row[LichKhamTable.idBenhNhan] ?: "" }
    }
    fun getIdLichKhamByIdBenhNhanWithStatus(idBenhNhan: String): List<Int> {
        return database.from(LichKhamTable)
            .select(LichKhamTable.idLichKham)
            .where {
                (LichKhamTable.idBenhNhan eq idBenhNhan) and
                        ((LichKhamTable.trangThai eq "Đã hoàn tất") )
            }
            .map { row -> row[LichKhamTable.idLichKham] ?: 0 } // Trả về danh sách idLichKham
    }
    fun getLichHenByIdLichKham(idLichKham: Int): LichKham? {
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd") // Format ngày lưu trong DB
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")   // Format giờ lưu trong DB

        return database.from(LichKhamTable)
            .select()
            .where { LichKhamTable.idLichKham eq idLichKham }
            .map { row ->
                LichKham(
                    idLichKham = row[LichKhamTable.idLichKham] ?: 0,
                    idBenhNhan = row[LichKhamTable.idBenhNhan] ?: "",
                    idBacSi = row[LichKhamTable.idBacSi] ?: "",
                    ngayKham = row[LichKhamTable.ngayKham]?.format(dateFormatter) ?: "",
                    gioKham = row[LichKhamTable.gioKham]?.format(timeFormatter) ?: "",
                    trangThai = row[LichKhamTable.trangThai] ?: ""
                )
            }.firstOrNull() // Lấy bản ghi đầu tiên (hoặc null nếu không tìm thấy)
    }




}
//-----------------------------------------------------------------------------------
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

//-------------------------------------------------------------------------------------------------
fun Route.layLichKhamTheoNguoiDung() {
    route("/get") {
        get("/lichhen") {
            val benhNhanId = call.parameters["benhNhanId"]
            if (benhNhanId == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid benhNhanId")
                return@get
            }

            try {
                val lichHens = lichKhamDAO.getLichHenByIdNguoiDung(benhNhanId)
                if (lichHens.isEmpty()) {
                    call.respond(HttpStatusCode.NotFound, "No appointments found for user $benhNhanId")
                } else {
                    call.respond(HttpStatusCode.OK, lichHens)
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error: ${e.message}")
            }
        }
    }
}
//-----------------------------------------------------------------------------------------------------
fun Route.layallLichKhamTheoNguoiDung() {
    route("/get") {
        get("/all/lichhen") {
            val idBenhNhan = call.parameters["benhNhanId"]
            if (idBenhNhan == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid benhNhanId")
                return@get
            }

            try {
                val lichHens = lichKhamDAO.getallLichHenByIdNguoiDung(idBenhNhan)
                if (lichHens.isEmpty()) {
                    call.respond(HttpStatusCode.NotFound, "No appointments found for user $idBenhNhan")
                } else {
                    call.respond(HttpStatusCode.OK, lichHens)
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error: ${e.message}")
            }
        }
    }
}

//--------------------------------------------------------------------------------------------------------
fun Route.capNhatTrangThaiLichKham() {
    route("/put") {
        put("/update/lichkham") {
            val lichKhamId = call.parameters["lichKhamId"]?.toIntOrNull()
            val request = call.receive<Map<String, String>>()
            val trangThaiMoi = request["trangThai"]

            if (lichKhamId == null || trangThaiMoi.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest, "Invalid request data")
                return@put
            }

            try {
                val updated = lichKhamDAO.capNhatTrangThai(lichKhamId, trangThaiMoi)
                if (updated) {
                    call.respond(HttpStatusCode.OK, "Appointment status updated successfully")
                } else {
                    call.respond(HttpStatusCode.NotFound, "Appointment not found with id $lichKhamId")
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error: ${e.message}")
            }
        }
    }
}
//-------------------------------------------------------------------------------------------------------------------
fun Route.kiemTraLichKham() {
    route("/get") {
        get("/kiemtra/lichkham") {
            val idBacSi = call.parameters["idBacSi"]
            val ngayKham = call.parameters["ngayKham"]
            val gioKham = call.parameters["gioKham"]

            if (idBacSi.isNullOrBlank() || ngayKham.isNullOrBlank() || gioKham.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest, "Thiếu thông tin idBacSi, ngayKham hoặc gioKham")
                return@get
            }

            try {
                val isAvailable = lichKhamDAO.kiemTraLichKham(idBacSi, ngayKham, gioKham)
                call.respond(HttpStatusCode.OK, mapOf("available" to isAvailable))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error: ${e.message}")
            }
        }
    }
}
//----------------------------------------------------------------------------------------------------------------
fun Route.lichKhamHomNay(){
    route("/get"){
        get("/lichkham/idBacSi") {
            val idBacSi = call.parameters["idBacSi"]
            if (idBacSi != null) {
                val lichKhamList = LichKhamDAO(database).getLichKhamByIdBacSiToday(idBacSi)
                if (lichKhamList.isNotEmpty()) {
                    call.respond(HttpStatusCode.OK, lichKhamList)
                } else {
                    call.respond(HttpStatusCode.NoContent, "Không có lịch khám nào")
                }
            } else {
                call.respond(HttpStatusCode.BadRequest, "Thiếu ID bác sĩ")
            }
        }
    }
}
//--------------------------------------------------------------------------------------------------------------------
fun Route.lichKhamToanBoCuaBacSi() {
    route("/get") {
        get("/lichkham/all/idBacSi") {
            // Lấy ID bác sĩ từ URL
            val idBacSi = call.parameters["idBacSi"]

            if (idBacSi != null) {
                // Gọi phương thức DAO để lấy tất cả lịch khám của bác sĩ
                val lichKhamList = LichKhamDAO(database).getLichKhamToanBoCuaBacSi(idBacSi)

                // Kiểm tra nếu có lịch khám
                if (lichKhamList.isNotEmpty()) {
                    // Trả về danh sách lịch khám với mã trạng thái 200 OK
                    call.respond(HttpStatusCode.OK, lichKhamList)
                } else {
                    // Nếu không có lịch khám, trả về mã trạng thái 204 No Content
                    call.respond(HttpStatusCode.NoContent, "Không có lịch khám nào")
                }
            } else {
                // Nếu thiếu ID bác sĩ trong tham số URL, trả về mã trạng thái 400 Bad Request
                call.respond(HttpStatusCode.BadRequest, "Thiếu ID bác sĩ")
            }
        }
    }
}
//----------------------------------------------------------------------------------------------------------------------
fun Route.getLichHenByIdLichKham() {
    route("/get") {
        get("/lichhen/idlichkham") {
            // Lấy tham số idLichKham từ query string
            val idLichKham = call.request.queryParameters["idlichkham"]?.toIntOrNull()

            if (idLichKham == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("message" to "Thiếu hoặc sai định dạng tham số idLichKham")
                )
                return@get
            }

            // Gọi DAO để lấy thông tin lịch khám
            val lichKham = lichKhamDAO.getLichHenByIdLichKham(idLichKham)

            if (lichKham == null) {
                call.respond(
                    HttpStatusCode.NotFound,
                    mapOf("message" to "Không tìm thấy lịch khám với idLichKham = $idLichKham")
                )
            } else {
                call.respond(HttpStatusCode.OK, lichKham)
            }
        }
    }
}



