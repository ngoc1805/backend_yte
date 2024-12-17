package com.example

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.ktorm.database.Database
import org.ktorm.dsl.*

@Serializable
data class  ChucNang(
    val idChucNang: String ?= null,
    val tenChucNang: String,
    val idTaiKhoan: Int,
    val giaKham: Int
)

class ChucNangDAO(private val database: Database) {

    // laays thong tin cac phong chuc nang
    fun getAllChucNang(): List<ChucNang> {
        return database.from(ChucNangTable)
            .select()
            .map { row ->
                ChucNang(
                    idChucNang = row[ChucNangTable.idChucNang] ?: "",
                    tenChucNang = row[ChucNangTable.tenChucNang] ?: "",
                    idTaiKhoan = row[ChucNangTable.idTaiKhoan] ?: 0,
                    giaKham = row[ChucNangTable.giaKham] ?: 0,
                )
            }
    }

    fun getChucNangByIdChucNang(idChucNang: String): ChucNang? {
        // Select the record from the ChucNangTable where idChucNang matches
        val result = database.from(ChucNangTable)
            .select()
            .where { ChucNangTable.idChucNang eq idChucNang }
            .map { row ->
                ChucNang(
                    idChucNang = row[ChucNangTable.idChucNang],
                    tenChucNang = row[ChucNangTable.tenChucNang] ?: "",
                    idTaiKhoan = row[ChucNangTable.idTaiKhoan] ?: 0,
                    giaKham = row[ChucNangTable.giaKham] ?: 0
                )
            }

        // Nếu không có kết quả, trả về null, nếu có kết quả thì trả về ChucNang đầu tiên
        return if (result.isEmpty()) null else result.first()
    }
    fun getChucNangByIdTaiKhoan(idTaiKhoan: Int): ChucNang? {
        return database.from(ChucNangTable)
            .select()
            .where { ChucNangTable.idTaiKhoan eq idTaiKhoan }
            .map { row ->
                ChucNang(
                    idChucNang = row[ChucNangTable.idChucNang] ?: "",
                    tenChucNang = row[ChucNangTable.tenChucNang] ?: "",
                    idTaiKhoan = row[ChucNangTable.idTaiKhoan] ?: 0,
                    giaKham = row[ChucNangTable.giaKham] ?: 0,

                )
            }
            .singleOrNull()
    }
}


//----------------------------------------------------------------------------------------------------------------------
fun Route.getAllChucNang(){
    route("/get"){
        get("/all/chucnang") {
            val allChucNang = chucNangDAO.getAllChucNang()
            call.respond(allChucNang)
        }
    }
}
//----------------------------------------------------------------------------------------------------------------------
fun Route.getChucNangbyIdChucNang(){
    route("/get") {
        get("/chucnang/idchucnang") {
            val idChucNang = call.parameters["idchucnang"]
            if (idChucNang == null) {
                call.respond(HttpStatusCode.BadRequest, "id chuc nang khong duoc de trong")
                return@get
            }
            try {
                val chucNang = chucNangDAO.getChucNangByIdChucNang(idChucNang)
                if (chucNang == null) {
                    call.respond(HttpStatusCode.NotFound, "Khong tim thay ban ghi nao")
                } else {
                    call.respond(HttpStatusCode.OK, chucNang)
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error: ${e.message}")
            }
        }
    }
}
//----------------------------------------------------------------------------------------------------------------------
fun Route.getChucNangByIdTaiKhoan(){
    route("/get") {
        get("/chucnang/idtk") {
            // Lấy tham số idtk từ query
            val idTaiKhoanStr = call.request.queryParameters["idtk"]

            // Kiểm tra nếu idTaiKhoan bị thiếu hoặc rỗng
            if (idTaiKhoanStr.isNullOrEmpty()) {
                call.respond(mapOf("error" to "Id không được để trống"))
                return@get
            }

            // Chuyển đổi idTaiKhoanStr thành Int, nếu không hợp lệ thì trả về lỗi
            val idTaiKhoan = idTaiKhoanStr.toIntOrNull()
            if (idTaiKhoan == null) {
                call.respond(mapOf("error" to "Id không hợp lệ"))
                return@get
            }

            // Lấy thông tin bác sĩ theo idTaiKhoan
            val result = chucNangDAO.getChucNangByIdTaiKhoan(idTaiKhoan)
            if (result == null) {
                call.respond(mapOf("error" to "Không tìm thấy bác sĩ"))
            } else {
                call.respond(result)
            }
        }
    }
}
