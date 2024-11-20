package com.example

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.ktorm.database.Database
import org.ktorm.dsl.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException





@Serializer(forClass = LocalDate::class)
object LocalDateSerializer {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    override fun serialize(encoder: Encoder, value: LocalDate) {
        encoder.encodeString(value.format(formatter))
    }

    override fun deserialize(decoder: Decoder): LocalDate {
        return LocalDate.parse(decoder.decodeString(), formatter)
    }
}
@Serializable
data class NguoiDung(
    val nguoiDungId: Int = 0,
    val hoten: String,
    val sdt: String,
    @Serializable(with = LocalDateSerializer::class) val ngaysinh: LocalDate,
    val cccd: String,
    val quequan: String?,
    val gioitinh: String?,
    val sodu: Int?,
    val idTaiKhoan: Int
)
class NguoiDungDAO(private val database: Database) {
    // thêm người dùng mới
    fun addNguoiDung(nguoiDung: NguoiDung): NguoiDung{
        val newsId = database.insertAndGenerateKey(NguoiDungTable){
            set(NguoiDungTable.hoten, nguoiDung.hoten)
            set(NguoiDungTable.sdt, nguoiDung.sdt)
            set(NguoiDungTable.ngaysinh, nguoiDung.ngaysinh)
            set(NguoiDungTable.cccd, nguoiDung.cccd)
            set(NguoiDungTable.quequan, nguoiDung.quequan)
            set(NguoiDungTable.gioitinh, nguoiDung.gioitinh)
            set(NguoiDungTable.sodu, nguoiDung.sodu)
            set(NguoiDungTable.idTaiKhoan, nguoiDung.idTaiKhoan)
        } as Int
        return nguoiDung.copy(nguoiDungId = newsId)
    }
    fun getNguoiDungByIdTk(idTaiKhoan: Int): NguoiDung?{
        return database.from(NguoiDungTable)
            .select()
            .where(NguoiDungTable.idTaiKhoan eq idTaiKhoan )
            .map { row ->
                NguoiDung(
                    nguoiDungId = row[NguoiDungTable.nguoiDungId] ?: 0,
                    hoten = row[NguoiDungTable.hoten] ?: "",
                    sdt = row[NguoiDungTable.sdt] ?: "",
                    ngaysinh = row[NguoiDungTable.ngaysinh] ?.let {
                        // Chuyển đổi giá trị từ cơ sở dữ liệu thành LocalDate
                        LocalDate.parse(it.toString())
                    } ?: LocalDate.now(), // Giá trị mặc định nếu null
                    cccd = row[NguoiDungTable.cccd] ?: "",
                    quequan = row[NguoiDungTable.quequan] ?: "",
                    gioitinh = row[NguoiDungTable.gioitinh] ?: "",
                    sodu = row[NguoiDungTable.sodu] ?: 0,
                    idTaiKhoan = row[NguoiDungTable.idTaiKhoan] ?:0
                )
            }
            .singleOrNull()
    }
    fun updateSoDuById(nguoiDungId: Int, sodu: Int) : Boolean{
        val updatedRows = database.update(NguoiDungTable) {
            set(NguoiDungTable.sodu, sodu)
            where { NguoiDungTable.nguoiDungId eq nguoiDungId }
        }
        return updatedRows > 0
    }
}

// lấy thông tin theo id tài khoản
fun Route.getNguoiDungByIdTk() {
    route("/get") {
        get("/nguoidung") {
            try {
                // Nhận tham số từ query
                val idTaiKhoanStr = call.request.queryParameters["idtaikhoan"]

                // Kiểm tra nếu id tài khoản trống
                if (idTaiKhoanStr.isNullOrEmpty()) {
                    call.respond(mapOf("error" to "Id tài khoản không được để trống"))
                    return@get
                }

                // Chuyển đổi chuỗi thành số nguyên
                val idTaiKhoan = idTaiKhoanStr.toIntOrNull()
                if (idTaiKhoan == null) {
                    call.respond(mapOf("error" to "Id tài khoản phải là số hợp lệ"))
                    return@get
                }

                // Lấy dữ liệu từ DAO
                val result = nguoiDungDAO.getNguoiDungByIdTk(idTaiKhoan)
                if (result == null) {
                    call.respond(mapOf("error" to "Không tìm thấy người dùng"))
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
// cập nhật số dư
fun Route.updateSoDu(){
    route("/post"){
        post("/update/sodu") {
            val request = call.receive<Map<String,String>>()
            val idtainguoidung = request["idnguoidung"]?.toIntOrNull()
            val sodu = request["sodu"] ?.toIntOrNull()

            if (idtainguoidung == null || sodu == null) {
                call.respond(mapOf("error" to "ID người dùng và số dư không được để trống"))
                return@post
            }
            val success = nguoiDungDAO.updateSoDuById(idtainguoidung,sodu)
            if (success) {
                call.respond(mapOf("message" to "Cập nhật số dư thành công"))
            } else {
                call.respond(mapOf("error" to "Không tìm thấy người dùng với ID đã cho"))
            }

        }
    }
}

fun Route.addTaiKhoanAndNguoiDung() {
    route("/post") {
        post("/add/taikhoan_nguoidung") {
            val request = call.receive<Map<String, String>>()
            val tentk = request["tentk"]
            val matkhau = request["matkhau"]
            val loaitkString = request["loaitk"]
            val hoten = request["hoten"]
            val sdt = request["sdt"]
            val ngaysinhStr = request["ngaysinh"]
            val cccd = request["cccd"]
            val quequan = request["quequan"]

            val gioitinh = request["gioitinh"]

            // Kiểm tra các tham số đầu vào
            if (tentk.isNullOrEmpty() || matkhau.isNullOrEmpty() || loaitkString.isNullOrEmpty() || hoten.isNullOrEmpty() || sdt.isNullOrEmpty()) {
                call.respond(ResponseMessage(error = "Các trường bắt buộc không được để trống"))
                return@post
            }

            // Kiểm tra xem tài khoản đã tồn tại chưa
            val existingAccount = taiKhoanDAO.getTaiKhoanByTenTK(tentk)
            if (existingAccount != null) {
                call.respond(ResponseMessage(error = "Tài khoản đã tồn tại"))
                return@post
            }

            // Chuyển đổi loaitk từ String sang LoaiTaiKhoan (enum)
            val loaitk = try {
                LoaiTaiKhoan.valueOf(loaitkString)
            } catch (e: IllegalArgumentException) {
                call.respond(ResponseMessage(error = "Loại tài khoản không hợp lệ"))
                return@post
            }

            // Tạo tài khoản mới
            val newAccount = taiKhoanDAO.createTaiKhoan(tentk, matkhau, loaitk)

            // Chuyển ngaysinh từ String thành LocalDate
//            val ngaysinh = ngaysinhStr?.let { LocalDate.parse(it) } ?: LocalDate.now()

            val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            // Chuyển đổi ngaysinh từ String sang LocalDate với định dạng dd/MM/yyyy
            val ngaysinh = try {
                LocalDate.parse(ngaysinhStr, dateFormatter)
            } catch (e: DateTimeParseException) {
                call.respond(mapOf("error" to "Ngày sinh không hợp lệ. Định dạng phải là dd/MM/yyyy"))
                return@post
            }

            // Tạo người dùng mới và thêm vào cơ sở dữ liệu
            val nguoiDung = NguoiDung(
                hoten = hoten,
                sdt = sdt,
                ngaysinh = ngaysinh,
                cccd = cccd ?: "",
                quequan = quequan,
                gioitinh = gioitinh,
                sodu = 0,
                idTaiKhoan = newAccount.idTaiKhoan // Liên kết người dùng với tài khoản mới
            )

            val addedNguoiDung = nguoiDungDAO.addNguoiDung(nguoiDung)

            // Phản hồi kết quả
            call.respond(ResponseMessage(message = "Tạo tài khoản và người dùng thành công"))
        }
    }
}
