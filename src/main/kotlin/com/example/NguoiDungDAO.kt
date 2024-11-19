package com.example

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.ktorm.database.Database
import org.ktorm.dsl.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter


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
