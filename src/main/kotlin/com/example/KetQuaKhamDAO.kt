package com.example

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.ktorm.database.Database
import org.ktorm.dsl.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Serializable
data class KetQuaKham(
    val idKetQua: Int ?= null,
    val idLichKham: Int,
    val nhanXet: String,
    val ngayTraKetQua: String,
    val daThanhToan: Boolean
)

class KetQuaKhamDAO (private val database: Database){
    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    fun getKetQuaKhamByIdLichKham(idLichKham: Int): KetQuaKham?{
       return database.from(KetQuaKhamTable)
           .select()
           .where { KetQuaKhamTable.idLichKham eq idLichKham }
           .map { row ->
               KetQuaKham(
                   idKetQua = row[KetQuaKhamTable.idKetQua],
                   idLichKham = row[KetQuaKhamTable.idLichKham]!!,
                   nhanXet = row[KetQuaKhamTable.nhanXet]!!,
                   ngayTraKetQua = row[KetQuaKhamTable.ngayTraKetQua].toString() ?: "",
                   daThanhToan = row[KetQuaKhamTable.daThanhToan] ?: false
               )
           }.firstOrNull()
    }
    fun addKetQuaKham(ketQuaKham: KetQuaKham):Int{
        val parsedNgayTraKetQua = LocalDate.parse(ketQuaKham.ngayTraKetQua, dateFormatter)
        return database.insertAndGenerateKey(KetQuaKhamTable){row->
            set(row.idLichKham, ketQuaKham.idLichKham)
            set(row.nhanXet, ketQuaKham.nhanXet)
            set(row.ngayTraKetQua,parsedNgayTraKetQua)
            set(row.daThanhToan,ketQuaKham.daThanhToan)
        } as Int
    }
    fun getKetQuaKhamByIdBenhNhan(idBenhNhan: String): List<KetQuaKham> {
        // Bước 1: Lấy danh sách idLichKham theo idBenhNhan
        val idLichKhamList = lichKhamDAO.getIdLichKhamByIdBenhNhanWithStatus(idBenhNhan)

        // Nếu không có idLichKham nào, trả về danh sách rỗng
        if (idLichKhamList.isEmpty()) {
            return emptyList()
        }

        // Bước 2: Lấy danh sách kết quả khám từ bảng ketQuaKham
        return database.from(KetQuaKhamTable)
            .select()
            .orderBy(KetQuaKhamTable.idKetQua.desc())
            .where { KetQuaKhamTable.idLichKham inList idLichKhamList }
            .map { row ->
                KetQuaKham(
                    idKetQua = row[KetQuaKhamTable.idKetQua],
                    idLichKham = row[KetQuaKhamTable.idLichKham]!!,
                    nhanXet = row[KetQuaKhamTable.nhanXet]!!,
                    ngayTraKetQua = row[KetQuaKhamTable.ngayTraKetQua].toString(),
                    daThanhToan = row[KetQuaKhamTable.daThanhToan] ?: false
                )
            }
    }
    fun capNhatTrangThaiThanhToan(idLichKham: Int): Boolean {
        // Cập nhật trường daThanhToan thành true cho idLichKham
        val updatedRows = database.update(KetQuaKhamTable) {
            set(KetQuaKhamTable.daThanhToan, true)
            where { KetQuaKhamTable.idLichKham eq idLichKham }
        }
        return updatedRows > 0 // Trả về true nếu có ít nhất 1 dòng được cập nhật
    }


}
//----------------------------------------------------------------------------------------------------------------------
fun Route.getKetQuaKhamByIdLichKham() {
    route("/get") {
        get("/ketquakham/idlichkham") {
            // Lấy tham số idLichKham từ query string
            val idLichKham = call.request.queryParameters["idlichkham"]?.toIntOrNull()

            // Kiểm tra idLichKham hợp lệ
            if (idLichKham == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    "Thiếu hoặc sai định dạng tham số idLichKham"
                )
                return@get
            }

            // Gọi DAO để lấy kết quả khám
            val ketQuaKham = ketQuaKhamDAO.getKetQuaKhamByIdLichKham(idLichKham)

            if (ketQuaKham == null) {
                // Nếu không có bản ghi, trả về thông báo
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("message" to "Chưa có kết quả khám")
                )
            } else {
                // Nếu có bản ghi, trả về dữ liệu kết quả khám
                call.respond(HttpStatusCode.OK, ketQuaKham)
            }
        }
    }
}
//----------------------------------------------------------------------------------------------------------------------
fun Route.addKetQuaKham() {
    route("/post") {
        post("/add/ketquakham") {
            try {
                // Nhận thông tin từ body request
                val ketQuaKham = call.receive<KetQuaKham>()

                // Thêm mới kết quả khám
                val newId = ketQuaKhamDAO.addKetQuaKham(ketQuaKham)

                // Trả về kết quả thành công
                call.respond(HttpStatusCode.OK, "Thêm kết quả khám thành công")
            } catch (e: Exception) {
                e.printStackTrace()
                // Trả về lỗi nếu xảy ra lỗi
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Lỗi khi thêm kết quả khám: ${e.message}"))
            }
        }
    }
}

//----------------------------------------------------------------------------------------------------------------------
fun Route.getKetQuaKhamByIdBenhNhan() {
    route("/get") {
        get("/ketquakham/idbenhnhan") {
            val idBenhNhan = call.request.queryParameters["idbenhnhan"]

            if (idBenhNhan.isNullOrEmpty()) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    "Thiếu hoặc sai định dạng tham số idBenhNhan"
                )
                return@get
            }

            val ketQuaList = ketQuaKhamDAO.getKetQuaKhamByIdBenhNhan(idBenhNhan)

            if (ketQuaList.isEmpty()) {
                call.respond(
                    HttpStatusCode.NotFound,
                    mapOf("message" to "Không tìm thấy kết quả khám nào")
                )
            } else {
                call.respond(HttpStatusCode.OK, ketQuaList)
            }
        }
    }
}
fun Route.capNhatTrangThaiThanhToan() {
    route("/put") {
        put("/update/ketquakham/thanhtoan") {
            // Lấy tham số idLichKham từ query string
            val idLichKham = call.request.queryParameters["idlichkham"]?.toIntOrNull()

            if (idLichKham == null) {
                call.respond(HttpStatusCode.BadRequest, "Thiếu hoặc sai định dạng tham số idLichKham")
                return@put
            }

            try {
                // Gọi phương thức cập nhật trang thái daThanhToan
                val updated = ketQuaKhamDAO.capNhatTrangThaiThanhToan(idLichKham)

                if (updated) {
                    call.respond(HttpStatusCode.OK, "Cập nhật trạng thái thanh toán thành công.")
                } else {
                    call.respond(HttpStatusCode.NotFound, "Không tìm thấy kết quả khám với idLichKham = $idLichKham.")
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Lỗi khi cập nhật trạng thái thanh toán: ${e.message}")
            }
        }
    }
}


