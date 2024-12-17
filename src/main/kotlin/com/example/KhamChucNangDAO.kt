package com.example

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.ktorm.database.Database
import org.ktorm.dsl.*
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Serializable
data class KhamChucNang(
    val idKhamChucNang: Int?=null,
    val idLichKham: Int,
    val idChucNang: String,
    val gioKham: String,
    val trangThai: String
)
class KhamChucNangDAO(private val database: Database){
    private val timeFormatter = DateTimeFormatter.ofPattern("H:mm")
    fun addKhamChucNang(khamChucNang: KhamChucNang):Int{
        val parsedGioKham = LocalTime.parse(khamChucNang.gioKham, timeFormatter)
        return database.insertAndGenerateKey(KhamChucNangTable){row ->
            set(row.idLichKham, khamChucNang.idLichKham)
            set(row.idChucNang, khamChucNang.idChucNang)
            set(row.gioKham,parsedGioKham)
            set(row.trangThai,khamChucNang.trangThai)
        }as Int
    }
    fun getKhamChucNangByIdLichKham(idLichKham: Int) :List<KhamChucNang>{
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd") // Format lưu trong DB
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")   // Format giờ lưu trong DB
        return database.from(KhamChucNangTable)
            .select()
            .where { KhamChucNangTable.idLichKham eq idLichKham }
            .map { row ->
                KhamChucNang(
                    idKhamChucNang = row[KhamChucNangTable.idKhamChucNang],
                    idLichKham = row[KhamChucNangTable.idLichKham] ?: 0,
                    idChucNang = row[KhamChucNangTable.idChucNang] ?: "",
                    gioKham = row[KhamChucNangTable.gioKham] ?.format(timeFormatter) ?: "",
                    trangThai = row[KhamChucNangTable.trangThai] ?: ""
                )
            }
    }
    fun getIdLichKhamByIdChucNang(idChucNang: String): List<Int> {
        return database.from(KhamChucNangTable)
            .select(KhamChucNangTable.idLichKham)
            .where { (KhamChucNangTable.idChucNang eq idChucNang) and (KhamChucNangTable.trangThai eq "Đã lên lịch") }
            .map { row -> row[KhamChucNangTable.idLichKham] ?: 0 }
    }

    fun getKhamChucNangByIdChucNang(idChucNang: String): List<KhamChucNang> {
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss") // Format giờ lưu trong DB
        return database.from(KhamChucNangTable)
            .select()
            .where { (KhamChucNangTable.idChucNang eq idChucNang) and (KhamChucNangTable.trangThai eq "Đã lên lịch" ) }
            .map { row ->
                KhamChucNang(
                    idKhamChucNang = row[KhamChucNangTable.idKhamChucNang],
                    idLichKham = row[KhamChucNangTable.idLichKham] ?: 0,
                    idChucNang = row[KhamChucNangTable.idChucNang] ?: "",
                    gioKham = row[KhamChucNangTable.gioKham]?.format(timeFormatter) ?: "",
                    trangThai = row[KhamChucNangTable.trangThai] ?: ""
                )
            }
    }
    fun getKhamChucNangByIdChucNang_idLichKham(idChucNang: String, idLichKham: Int): KhamChucNang? {
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss") // Format giờ lưu trong DB
        return database.from(KhamChucNangTable)
            .select()
            .where { (KhamChucNangTable.idChucNang eq idChucNang) and (KhamChucNangTable.idLichKham eq idLichKham) }
            .map { row ->
                KhamChucNang(
                    idKhamChucNang = row[KhamChucNangTable.idKhamChucNang],
                    idLichKham = row[KhamChucNangTable.idLichKham] ?: 0,
                    idChucNang = row[KhamChucNangTable.idChucNang] ?: "",
                    gioKham = row[KhamChucNangTable.gioKham]?.format(timeFormatter) ?: "",
                    trangThai = row[KhamChucNangTable.trangThai] ?: ""
                )
            }
            .singleOrNull()
    }
    fun updateTrangThaiById(idKhamChucNang: Int) {
        try {
            // Thực hiện cập nhật trạng thái trong cơ sở dữ liệu
            database.update(KhamChucNangTable) {
                set(it.trangThai, "Đã hoàn tất") // Cập nhật trạng thái thành "Đã hoàn thành"
                where {
                    it.idKhamChucNang eq idKhamChucNang // Điều kiện cập nhật theo idKhamChucNang
                }
            }
        } catch (e: Exception) {
            e.printStackTrace() // Log lỗi nếu có
            throw e // Quăng ngoại lệ nếu cần xử lý ở cấp độ cao hơn
        }
    }




}
//----------------------------------------------------------------------------------------------------------------------
fun Route.addKhamChucNang(){
    route("/post"){
        post("/add/khamchucnang") {
            val request = call.receive<KhamChucNang>()
            try {
                val id = khamChucNangDAO.addKhamChucNang(request)
                call.respond(HttpStatusCode.Created, mapOf("id" to id))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Error: ${e.message}")
            }
        }
    }
}
//----------------------------------------------------------------------------------------------------------------------
fun Route.getKhamChucNangByIdLichKham(){
    route("/get"){
        get("/khamchucnang/idlickham") {
            val idLichKham = call.parameters["idlichkham"]?.toIntOrNull()
            if(idLichKham == null){
                call.respond(HttpStatusCode.BadRequest, "Lich kham khong duoc de trong")
                return@get
            }
            try{
                val khamChucNang = khamChucNangDAO.getKhamChucNangByIdLichKham(idLichKham)
                if(khamChucNang.isEmpty()){
                    call.respond(HttpStatusCode.NotFound, "Khong co ban ghi nao")
                }
                else{
                    call.respond(HttpStatusCode.OK, khamChucNang)
                }
            }catch (e: Exception){
                call.respond(HttpStatusCode.InternalServerError, "Error: ${e.message}")
            }
        }
    }
}
//----------------------------------------------------------------------------------------------------------------------
fun Route.getKhamChucNangByIdChucNang() {
    route("/get") {
        get("/khamchucnang/idchucnang") {
            try {
                // Lấy tham số từ query
                val idChucNang = call.request.queryParameters["idchucnang"]

                // Kiểm tra xem idChucNang có hợp lệ không
                if (idChucNang.isNullOrEmpty()) {
                    call.respond(mapOf("error" to "idChucNang không được để trống"))
                    return@get
                }

                // Lấy danh sách khám chức năng từ DAO
                val khamChucNangDAO = KhamChucNangDAO(database)
                val result = khamChucNangDAO.getKhamChucNangByIdChucNang(idChucNang)

                // Kiểm tra nếu không có kết quả
                if (result.isEmpty()) {
                    call.respond(mapOf("error" to "Không tìm thấy khám chức năng cho idChucNang $idChucNang"))
                } else {
                    call.respond(result)
                }

            } catch (e: Exception) {
                // Log lỗi và phản hồi
                e.printStackTrace()
                call.respond(mapOf("error" to "Đã xảy ra lỗi: ${e.message}"))
            }
        }
    }
}
//----------------------------------------------------------------------------------------------------------------------
fun Route.getKhamChucNangByIdChucNangAndLichKham() {
    route("/get") {
        get("/khamchucnang/idchucnang/idlichkham") {
            // Lấy tham số từ query
            val idChucNang = call.request.queryParameters["idchucnang"]
            val idLichKham = call.request.queryParameters["idlichkham"]?.toIntOrNull()

            // Kiểm tra tham số có hợp lệ không
            if (idChucNang.isNullOrEmpty() || idLichKham == null) {
                call.respond(HttpStatusCode.BadRequest, "idChucNang và idLichKham không được để trống")
                return@get
            }
            try {
                // Gọi hàm DAO để lấy kết quả từ database
                val khamChucNangDAO = KhamChucNangDAO(database)
                val result = khamChucNangDAO.getKhamChucNangByIdChucNang_idLichKham(idChucNang, idLichKham)

                // Kiểm tra nếu không có kết quả
                if (result == null) {
                    call.respond(HttpStatusCode.NotFound, "Không tìm thấy khám chức năng cho idChucNang $idChucNang và idLichKham $idLichKham")
                } else {
                    // Trả về kết quả nếu tìm thấy
                    call.respond(HttpStatusCode.OK, result)
                }
            } catch (e: Exception) {
                // Log lỗi và phản hồi
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, "Đã xảy ra lỗi: ${e.message}")
            }
        }
    }
}
//----------------------------------------------------------------------------------------------------------------------
fun Route.updateTrangThaiKhamById() {
    route("/update") {
        get("/trangthai/id") {
            try {
                // Lấy `idKhamChucNang` từ request body hoặc query parameters
                val idKhamChucNang = call.request.queryParameters["id"]?.toIntOrNull()

                if (idKhamChucNang == null) {
                    call.respond(HttpStatusCode.BadRequest, "Thiếu idKhamChucNang hoặc id không hợp lệ")
                    return@get
                }

                // Gọi DAO để cập nhật trạng thái
                khamChucNangDAO.updateTrangThaiById(idKhamChucNang)

                // Phản hồi thành công
                call.respond(HttpStatusCode.OK, "Cập nhật trạng thái thành công cho id $idKhamChucNang")
            } catch (e: Exception) {
                // Xử lý lỗi và phản hồi
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, "Đã xảy ra lỗi: ${e.message}")
            }
        }
    }
}

