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
    val idBenhNhan: String = "",
    val hoten: String,
    val sdt: String,
    @Serializable(with = LocalDateSerializer::class) val ngaysinh: LocalDate,
    val cccd: String,
    val quequan: String?,
    val gioitinh: String?,
    val sodu: Int?,
    val idTaiKhoan: Int
)
@Serializable
data class NguoiDungLichKham(
    val idBenhNhan: String = "",
    val hoten: String,
    val sdt: String,
    @Serializable(with = LocalDateSerializer::class) val ngaysinh: LocalDate,
    val cccd: String,
    val quequan: String?,
    val gioitinh: String?,
    val sodu: Int?,
    val idTaiKhoan: Int,
    val idLichKham: Int,
    val idChucNang: String
)
@Serializable
data class MaPin(
    val maPin: String
)
class NguoiDungDAO(private val database: Database) {
    // thêm người dùng mới
    fun addNguoiDung(nguoiDung: NguoiDung): NguoiDung{
        val newsId = database.insertAndGenerateKey(BenhNhanTable){
            set(BenhNhanTable.hoten, nguoiDung.hoten)
            set(BenhNhanTable.sdt, nguoiDung.sdt)
            set(BenhNhanTable.ngaysinh, nguoiDung.ngaysinh)
            set(BenhNhanTable.cccd, nguoiDung.cccd)
            set(BenhNhanTable.quequan, nguoiDung.quequan)
            set(BenhNhanTable.gioitinh, nguoiDung.gioitinh)
            set(BenhNhanTable.sodu, nguoiDung.sodu)
            set(BenhNhanTable.idTaiKhoan, nguoiDung.idTaiKhoan)
        } as String
        return nguoiDung.copy(idBenhNhan = newsId)
    }
    fun getNguoiDungByIdTk(idTaiKhoan: Int): NguoiDung?{
        return database.from(BenhNhanTable)
            .select()
            .where(BenhNhanTable.idTaiKhoan eq idTaiKhoan )
            .map { row ->
                NguoiDung(
                    idBenhNhan = row[BenhNhanTable.idBenhNhan] ?: "",
                    hoten = row[BenhNhanTable.hoten] ?: "",
                    sdt = row[BenhNhanTable.sdt] ?: "",
                    ngaysinh = row[BenhNhanTable.ngaysinh] ?.let {
                        // Chuyển đổi giá trị từ cơ sở dữ liệu thành LocalDate
                        LocalDate.parse(it.toString())
                    } ?: LocalDate.now(), // Giá trị mặc định nếu null
                    cccd = row[BenhNhanTable.cccd] ?: "",
                    quequan = row[BenhNhanTable.quequan] ?: "",
                    gioitinh = row[BenhNhanTable.gioitinh] ?: "",
                    sodu = row[BenhNhanTable.sodu] ?: 0,
                    idTaiKhoan = row[BenhNhanTable.idTaiKhoan] ?:0
                )
            }
            .singleOrNull()
    }
    fun updateSoDuById(idBenhNhan: String, sodu: Int) : Boolean{
        val updatedRows = database.update(BenhNhanTable) {
            set(BenhNhanTable.sodu, sodu)
            where { BenhNhanTable.idBenhNhan eq idBenhNhan  }
        }
        return updatedRows > 0
    }

    fun getNguoiDungByIdBenhNhan(idBenhNhan: String): NguoiDung? {
        return database.from(BenhNhanTable)
            .select()
            .where(BenhNhanTable.idBenhNhan eq idBenhNhan)  // Truy vấn bằng idBenhNhan
            .map { row ->
                NguoiDung(
                    idBenhNhan = row[BenhNhanTable.idBenhNhan] ?: "",  // Lấy idBenhNhan
                    hoten = row[BenhNhanTable.hoten] ?: "",  // Lấy họ tên
                    sdt = row[BenhNhanTable.sdt] ?: "",  // Lấy số điện thoại
                    ngaysinh = row[BenhNhanTable.ngaysinh]?.let {
                        // Chuyển đổi từ cơ sở dữ liệu thành LocalDate
                        LocalDate.parse(it.toString())
                    } ?: LocalDate.now(),  // Nếu ngaysinh là null thì trả về ngày hiện tại
                    cccd = row[BenhNhanTable.cccd] ?: "",  // Lấy số CCCD
                    quequan = row[BenhNhanTable.quequan] ?: "",  // Lấy quê quán
                    gioitinh = row[BenhNhanTable.gioitinh] ?: "",  // Lấy giới tính
                    sodu = row[BenhNhanTable.sodu] ?: 0,  // Lấy số dư
                    idTaiKhoan = row[BenhNhanTable.idTaiKhoan] ?: 0  // Lấy id tài khoản
                )
            }
            .singleOrNull()  // Lấy một kết quả duy nhất, hoặc null nếu không tìm thấy
    }

    fun getThongTinBenhNhanByIdChucNang(idChucNang: String): List<NguoiDungLichKham> {
        // Bước 1: Lấy danh sách idLichKham từ idChucNang
        val idLichKhamList = khamChucNangDAO.getIdLichKhamByIdChucNang(idChucNang)

        // Bước 2: Lấy danh sách idBenhNhan từ idLichKham
        val idBenhNhanList = lichKhamDAO.getIdBenhNhanByIdLichKham(idLichKhamList)

        // Bước 3: Lấy thông tin bệnh nhân từ danh sách idBenhNhan và ghép với idLichKham
        val nguoiDungDAO = NguoiDungDAO(database)

        // Sử dụng zip để ghép từng idBenhNhan với idLichKham
        val danhSachBenhNhan = idBenhNhanList.zip(idLichKhamList).mapNotNull { (idBenhNhan, idLichKham) ->
            // Lấy thông tin bệnh nhân từ database
            val nguoiDung = nguoiDungDAO.getNguoiDungByIdBenhNhan(idBenhNhan)

            // Nếu bệnh nhân có thông tin, kết hợp với idLichKham và idChucNang
            nguoiDung?.let {
                // Trả về đối tượng chứa thông tin bệnh nhân, idLichKham và idChucNang
                NguoiDungLichKham(
                    idBenhNhan = nguoiDung.idBenhNhan,
                    hoten = nguoiDung.hoten,
                    sdt = nguoiDung.sdt,
                    ngaysinh = nguoiDung.ngaysinh,   // Ngày sinh bệnh nhân
                    cccd = nguoiDung.cccd,           // Căn cước công dân
                    quequan = nguoiDung.quequan,     // Quê quán
                    gioitinh = nguoiDung.gioitinh,   // Giới tính
                    sodu = nguoiDung.sodu,           // Số dư (nếu có)
                    idTaiKhoan = nguoiDung.idTaiKhoan, // ID tài khoản
                    idLichKham = idLichKham,    // ID lịch khám từ zip
                    idChucNang = idChucNang     // ID chức năng (thông qua tham số)
                )
            }
        }

        // Trả về danh sách thông tin bệnh nhân kèm theo idLichKham và idChucNang
        return danhSachBenhNhan
    }
    fun capNhapMaPin(idBenhNhan: String, maPin: String): Boolean {
        // Thực hiện câu lệnh update để cập nhật mã pin cho bệnh nhân
        val updatedRows = database.update(BenhNhanTable) {
            set(BenhNhanTable.maPin, maPin)  // Cập nhật trường maPin
            where { BenhNhanTable.idBenhNhan eq idBenhNhan }  // Điều kiện cập nhật theo idBenhNhan
        }
        return updatedRows > 0  // Trả về true nếu có ít nhất một dòng được cập nhật
    }

}

// lấy thông tin theo id tài khoản--------------------------------------------------------------------------------------
fun Route.getNguoiDungByIdTk() {
    route("/get") {
        get("/nguoidung") {
            try {
                // Nhận tham số từ query
                val idTaiKhoanStr = call.request.queryParameters["idTaiKhoan"]

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
            val idbenhnhan = request["idbenhnhan"]
            val sodu = request["sodu"] ?.toIntOrNull()

            if (idbenhnhan== null || sodu == null) {
                call.respond(mapOf("error" to "ID người dùng và số dư không được để trống"))
                return@post
            }
            val success = nguoiDungDAO.updateSoDuById(idbenhnhan,sodu)
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
//----------------------------------------------------------------------------------------------------------------------
fun Route.getNguoiDungByIdBenhNhan() {
    route("/get") {
        get("/benhnhan") {
            try {
                // Receive idBenhNhan from query parameters
                val idBenhNhan = call.request.queryParameters["idBenhNhan"]

                // Check if idBenhNhan is empty or null
                if (idBenhNhan.isNullOrEmpty()) {
                    call.respond(mapOf("error" to "Id bệnh nhân không được để trống"))
                    return@get
                }

                // Get the patient data using the DAO
                val result = nguoiDungDAO.getNguoiDungByIdBenhNhan(idBenhNhan)

                // If the patient is not found, respond with an error message
                if (result == null) {
                    call.respond(mapOf("error" to "Không tìm thấy bệnh nhân"))
                } else {
                    // Return the patient details
                    call.respond(result)
                }
            } catch (e: Exception) {
                // Log the exception and respond with an error
                e.printStackTrace()
                call.respond(mapOf("error" to "Đã xảy ra lỗi: ${e.message}"))
            }
        }
    }
}
//----------------------------------------------------------------------------------------------------------------------
fun Route.getThongTinBenhNhanByIdChucNangRoute() {
    route("/get") {
        get("/benhnhan/idchucnang") {
            try {
                // Nhận tham số idChucNang từ query
                val idChucNang = call.request.queryParameters["idchucnang"]

                // Kiểm tra nếu idChucNang trống
                if (idChucNang.isNullOrEmpty()) {
                    call.respond(mapOf("error" to "Id chức năng không được để trống"))
                    return@get
                }

                // Lấy dữ liệu bệnh nhân từ DAO
                val nguoiDungDAO = NguoiDungDAO(database)
                val danhSachBenhNhan = nguoiDungDAO.getThongTinBenhNhanByIdChucNang(idChucNang)

                // Kiểm tra xem có bệnh nhân nào không
                if (danhSachBenhNhan.isEmpty()) {
                    call.respond(mapOf("error" to "Không tìm thấy bệnh nhân cho chức năng này"))
                } else {
                    // Trả về danh sách bệnh nhân
                    call.respond(danhSachBenhNhan)
                }
            } catch (e: Exception) {
                // Log lỗi và phản hồi
                e.printStackTrace()
                call.respond(mapOf("error" to "Đã xảy ra lỗi: ${e.message}"))
            }
        }
    }
}

