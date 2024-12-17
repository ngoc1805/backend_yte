package com.example

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.ktorm.database.Database
import org.ktorm.dsl.*

@Serializable
data class ThongBao(
    val idThongBao: Int? = null,
    val idBenhNhan: String,
    val noiDung: String,
    val daXem: Boolean = false,
    val daNhan: Boolean = false,
    val thoiGian: kotlinx.datetime.LocalDateTime? = null,
    val duongDan: String? = null
)

class ThongBaoDAO(private val database: Database) {
    // Thêm thông báo mới
    fun themThongBao(thongBao: ThongBao): Int {
        return database.insertAndGenerateKey(ThongBaoTable) { row ->
            set(row.idBenhNhan, thongBao.idBenhNhan)
            set(row.noiDung, thongBao.noiDung)
            set(row.daXem, thongBao.daXem)
            set(row.daNhan, thongBao.daNhan)
            set(row.duongDan, thongBao.duongDan) // Không cần truyền thoiGian
        } as Int
    }
    // Lấy thông báo theo ID bệnh nhân
    fun getThongBaoByIdBenhNhan(idBenhNhan: String): List<ThongBao> {
        return database.from(ThongBaoTable)
            .select()
            .orderBy(ThongBaoTable.idThongBao.desc())
            .where { ThongBaoTable.idBenhNhan eq idBenhNhan }
            .map { row ->
                ThongBao(
                    idThongBao = row[ThongBaoTable.idThongBao],
                    idBenhNhan = row[ThongBaoTable.idBenhNhan]!!,
                    noiDung = row[ThongBaoTable.noiDung]!!,
                    daXem = row[ThongBaoTable.daXem] ?: false,
                    daNhan = row[ThongBaoTable.daNhan] ?: false,
                    thoiGian = kotlinx.datetime.LocalDateTime.parse(
                        row[ThongBaoTable.thoiGian]?.toString() ?: "1970-01-01T00:00:00"
                    ),
                    duongDan = row[ThongBaoTable.duongDan]
                )
            }
    }

    // Cập nhật trạng thái `daXem`
    fun capNhatTrangThaiDaXem(idThongBao: Int, daXem: Boolean) :Boolean {
       val updateRow = database.update(ThongBaoTable) {
            set(it.daXem, daXem)
            where { it.idThongBao eq idThongBao }
        }
        return updateRow > 0
    }
    // cap nhat trang thai da nhan
    fun capNhatTrangThaiDaNhan(idBenhNhan: String): Int {
        return database.update(ThongBaoTable) {
            set(it.daNhan, true)
            where { it.idBenhNhan eq idBenhNhan }
        }
    }
    // kiem tra so da nhan là false
    fun kiemTraThongBaoChuaNhan(idBenhNhan: String): Boolean {
        val count = database.from(ThongBaoTable)
            .select()
            .where { ThongBaoTable.idBenhNhan eq idBenhNhan and (ThongBaoTable.daNhan eq false) }
            .totalRecords // Tổng số bản ghi chưa nhận

        return count > 0 // Trả về true nếu có thông báo chưa nhận, false nếu không
    }

}

fun Route.addThongBao(){
    route("/post"){
        post("/add/thongbao") {
            try {
                val requestBody = call.receive<String>()
                val thongBao = Json.decodeFromString<ThongBao>(requestBody)

                val id = thongBaoDAO.themThongBao(thongBao)
                call.respond(HttpStatusCode.Created, "Thông báo mới được thêm với ID: $id")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Dữ liệu đầu vào không hợp lệ: ${e.message}")
            }
        }
    }
}
//--------------------------------------------------------------------------------------------
fun Route.getThongBao() {
    route("/get") {
        get("/thongbao/idBenhNhan") {
            try {
                // Lấy `idBenhNhan` từ URL parameters
                val idBenhNhan = call.parameters["idBenhNhan"]
                if (idBenhNhan.isNullOrEmpty()) {
                    call.respond(HttpStatusCode.BadRequest, "idBenhNhan không được để trống.")
                    return@get
                }

                // Sử dụng DAO để lấy danh sách thông báo
                val thongBaoList = thongBaoDAO.getThongBaoByIdBenhNhan(idBenhNhan)
                if (thongBaoList.isEmpty()) {
                    call.respond(HttpStatusCode.NotFound, "Không tìm thấy thông báo cho idBenhNhan: $idBenhNhan")
                } else {
                    call.respond(HttpStatusCode.OK, thongBaoList)
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Lỗi hệ thống: ${e.message}")
            }
        }
    }
}
//---------------------------------------------------------------------------------------------------------------------------------
fun Route.updateDaXem(){
    route("/put"){
        put("/thongbao/daxem") {
            val idThongBao = call.parameters["idThongBao"]?.toIntOrNull()
            val request = call.receive<Map<String,String>>()
            val daXem = request["daXem"].toBoolean()

            if(idThongBao == null  ){
                call.respond(HttpStatusCode.BadRequest, " khong duoc de trong")
                return@put
            }
            try {
                val update = thongBaoDAO.capNhatTrangThaiDaXem(idThongBao,daXem)
                if(update){
                    call.respond(HttpStatusCode.OK,"da cap nhat thanh cong thong bao co id $idThongBao")
                }else{
                    call.respond(HttpStatusCode.NotFound,"khong tim thay thong bao co id $idThongBao")
                }
            }catch (e: Exception){
                call.respond(HttpStatusCode.InternalServerError, "Error: ${e.message}")
            }
        }
    }
}
//------------------------------------------------------------------------------------------------------------------------------
fun Route.updateAllDaNhanByIdBenhNhan() {
    route("/put") {
        put("/thongbao/danhan") {
            try {
                // Lấy `idBenhNhan` từ URL parameters
                val idBenhNhan = call.parameters["idBenhNhan"]
                if (idBenhNhan.isNullOrEmpty()) {
                    call.respond(HttpStatusCode.BadRequest, "idBenhNhan không được để trống.")
                    return@put
                }

                // Sử dụng DAO để cập nhật tất cả thông báo có idBenhNhan
                val updatedCount = thongBaoDAO.capNhatTrangThaiDaNhan(idBenhNhan)
                if (updatedCount > 0) {
                    call.respond(HttpStatusCode.OK, "$updatedCount thông báo đã được cập nhật thành 'daNhan = true'.")
                } else {
                    call.respond(HttpStatusCode.NotFound, "Không có thông báo nào cần cập nhật cho idBenhNhan: $idBenhNhan.")
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Lỗi hệ thống: ${e.message}")
            }
        }
    }
}
//---------------------------------------------------------------------------------------------------------------------------------
fun Route.kiemTraThongBaoChuaNhan() {
    route("/get") {
        get("/thongbao/chuanhan") {
            try {
                // Lấy `idBenhNhan` từ URL parameters
                val idBenhNhan = call.parameters["idBenhNhan"]
                if (idBenhNhan.isNullOrEmpty()) {
                    call.respond(HttpStatusCode.BadRequest, "idBenhNhan không được để trống.")
                    return@get
                }

                // Sử dụng DAO để kiểm tra số lượng thông báo chưa nhận
                val isDaNhan = thongBaoDAO.kiemTraThongBaoChuaNhan(idBenhNhan)
                call.respond(HttpStatusCode.OK, isDaNhan)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Lỗi hệ thống: ${e.message}")
            }
        }
    }
}



