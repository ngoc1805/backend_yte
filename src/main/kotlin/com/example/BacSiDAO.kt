package com.example

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.ktorm.database.Database
import org.ktorm.dsl.*

@Serializable
data class BacSi(
    val idBacSi: String = "",
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
    fun addBacSi(bacSi: BacSi): BacSi{
        val newId = database.insertAndGenerateKey(BacSiTable){
            set(BacSiTable.hoTen, bacSi.hoTen)
            set(BacSiTable.idTaiKhoan, bacSi.idTaiKhoan)
            set(BacSiTable.khoa, bacSi.khoa)
            set(BacSiTable.giaKham, bacSi.giaKham)
        } as String
        return bacSi.copy(idBacSi = newId)
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
//----------------------------------------------------------------------------------------------------------------------
fun Route.addTaiKhoanAndBacSi() {
    route("/post") {
        post("/add/taikhoan/bacsi") {
            val request = call.receive<Map<String, String>>()
            val tenTk = request["tenTk"]
            val matKhau = request["matKhau"]
            val loaitkString = request["loaiTk"]
            val hoTen = request["hoTen"]
            val khoa = request["khoa"]
            val giaKham = request["giaKham"]?.toIntOrNull()

            // Kiểm tra đầu vào
            if (tenTk.isNullOrEmpty() || matKhau.isNullOrEmpty() || loaitkString.isNullOrEmpty() || hoTen.isNullOrEmpty() || khoa.isNullOrEmpty() || giaKham == null) {
                call.respond(HttpStatusCode.BadRequest, "Các trường bắt buộc không được để trống")
                return@post
            }

            // Kiểm tra xem tài khoản đã tồn tại chưa
            val existingAccount = taiKhoanDAO.getTaiKhoanByTenTK(tenTk)
            if (existingAccount != null) {
                call.respond(HttpStatusCode.NotFound, "Tài khoản đã tồn tại")
                return@post
            }

            // Xác thực loại tài khoản
            val loaitk = try {
                LoaiTaiKhoan.valueOf(loaitkString)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, "Loại tài khoản không hợp lệ")
                return@post
            }

            try {
                // Tạo tài khoản mới
                val newAccount = taiKhoanDAO.createTaiKhoan(tenTk, matKhau, loaitk)

                val bacSi = BacSi(
                    hoTen = hoTen,
                    idTaiKhoan = newAccount.idTaiKhoan,
                    khoa = khoa,
                    giaKham = giaKham
                )
                // Thêm thông tin bác sĩ
                val newBacSi = bacSiDAO.addBacSi(bacSi)

                call.respond(HttpStatusCode.OK, "Tạo tài khoản và người dùng thành công")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Lỗi trong quá trình tạo tài khoản hoặc người dùng: ${e.message}")
            }
        }
    }
}


