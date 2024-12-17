package com.example

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.ktorm.database.Database
import org.ktorm.dsl.*

@Serializable
data class BacSi(
    val idBacSi: String,
    val hoTen: String,
    val idTaiKhoan: Int,
    val khoa: String,
    val giaKham: Int
)
class BacSiDAO(private val database: Database) {

    //lay danh sach bac si theo khoa
    fun getBacSiByKhoa(khoa: String): List<BacSi> {
        return database.from(BacSiTable)
            .select()
            .where { BacSiTable.khoa eq khoa }
            .map { row ->
                BacSi(
                    idBacSi = row[BacSiTable.idBacSi] ?: "",
                    hoTen = row[BacSiTable.hoTen] ?: "",
                    idTaiKhoan = row[BacSiTable.idTaiKhoan] ?: 0,
                    khoa = row[BacSiTable.khoa] ?: "",
                    giaKham = row[BacSiTable.giaKham] ?: 0
                )
            }
    }
    // lấy danh sách toàn bộ các sĩ
    fun getAllBacSi(): List<BacSi>{
        return database.from(BacSiTable)
            .select()
            .map { row ->
                BacSi(
                    idBacSi = row[BacSiTable.idBacSi] ?: "",
                    hoTen = row[BacSiTable.hoTen] ?: "",
                    idTaiKhoan = row[BacSiTable.idTaiKhoan] ?: 0,
                    khoa = row[BacSiTable.khoa] ?: "",
                    giaKham = row[BacSiTable.giaKham] ?: 0
                )
            }
    }
    fun getBacSiById(idBacSi: String): BacSi?{
        return database.from(BacSiTable)
            .select()
            .where(BacSiTable.idBacSi eq idBacSi)
            .map { row ->
                BacSi(
                    idBacSi = row[BacSiTable.idBacSi] ?: "",
                    hoTen = row[BacSiTable.hoTen] ?: "",
                    idTaiKhoan = row[BacSiTable.idTaiKhoan] ?: 0,
                    khoa = row[BacSiTable.khoa] ?: "",
                    giaKham = row[BacSiTable.giaKham] ?: 0
                )
            }
            .singleOrNull()
    }
    fun getBacSiByIdTK(idTaiKhoan: Int): BacSi? {
        return database.from(BacSiTable)
            .select()
            .where(BacSiTable.idTaiKhoan eq idTaiKhoan)
            .map { row ->
                BacSi(
                    idBacSi = row[BacSiTable.idBacSi] ?: "",
                    hoTen = row[BacSiTable.hoTen] ?: "",
                    idTaiKhoan = row[BacSiTable.idTaiKhoan] ?: 0,
                    khoa = row[BacSiTable.khoa] ?: "",
                    giaKham = row[BacSiTable.giaKham] ?: 0
                )
            }
            .singleOrNull()  // Trả về null nếu không tìm thấy
    }
}

fun Route.getAllBacSi(){

    route("get"){
        get("/bacsi/all"){
            val allBacSi = bacSiDAO.getAllBacSi()
            call.respond(allBacSi)
        }
    }
}
//lấy bs theo id
fun Route.getBacSiById() {
    route("/get") {
        get("/bacsi/id") {
            val bacSiId = call.request.queryParameters["id"]
            if (bacSiId.isNullOrEmpty()) {
                call.respond(mapOf("error" to "Id không được để trống"))
                return@get
            }
            // Chuyển bacSiIdStr thành Int

            if (bacSiId == null) {
                call.respond(mapOf("error" to "Id không hợp lệ"))
                return@get
            }
            val result = bacSiDAO.getBacSiById(bacSiId)
            if (result == null) {
                call.respond(mapOf("error" to "Không tìm thấy người dùng"))
            } else {
                call.respond(result)
            }
        }
    }
}
// lâys bác s theo id tk
fun Route.getBacSiByIdTK() {
    route("/get") {
        get("/bacsi/idtk") {
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
            val result = bacSiDAO.getBacSiByIdTK(idTaiKhoan)
            if (result == null) {
                call.respond(mapOf("error" to "Không tìm thấy bác sĩ"))
            } else {
                call.respond(result)
            }
        }
    }
}

