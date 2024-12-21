package com.example



import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import io.ktor.http.*
import io.ktor.serialization.gson.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

val ipNha = "172.20.10.7"
val database = connectToDatabase()
val bacSiDAO = BacSiDAO(database)
val tinTucDao = TinTucDao(database)
val taiKhoanDAO = TaiKhoanDAO(database)
val nguoiDungDAO = NguoiDungDAO(database)
val lichKhamDAO = LichKhamDAO(database)
val thongBaoDAO = ThongBaoDAO(database)
val chucNangDAO = ChucNangDAO(database)
val khamChucNangDAO = KhamChucNangDAO(database)
val fileKetQuaDAO = FileKetQuaDAO(database)
val ketQuaKhamDAO = KetQuaKhamDAO(database)
fun Application.modules() {

    //----------------------------
    val serviceAccountStream = this::class.java.classLoader.getResourceAsStream("ytenhom14-firebase-adminsdk-pk35k-73f56c612f.json")
    val options = FirebaseOptions.builder()
        .setCredentials(GoogleCredentials.fromStream(serviceAccountStream))
        .build()

    FirebaseApp.initializeApp(options)
    //----------------------------

    install(ContentNegotiation) {
//        gson {  }
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
    routing {
        // lấy bác sĩ theo khoa------------------------------------------
        get("/bacsi") {
            val khoa = call.request.queryParameters["khoa"]
            if (khoa.isNullOrEmpty()) {
                call.respond(mapOf("error" to "Khoa không được để trống"))
                return@get
            }
            val result = bacSiDAO.getBacSiByKhoa(khoa)
            call.respond(result)
    }
        // lấy tất cả bác sĩ------------------------------------------------
        getAllBacSi()
        //thêm tin tức mới-------------------------------------------------
        addTinTuc()
        //lay tin tuc theo id-----------------------------------------------
        GetTinTucById()
        // lấy danh sách tin tức----------------------------------------------
        getAllTinTuc()
        //đăng ký tài khoản mới-----------------------------------------------
        addTaiKhoan()
        //them nguoi dung moi--------------------------------------------------------------
//        post("/post/add/user") {
//            // Lấy dữ liệu từ request body
//            val request = call.receive<Map<String,String>>()
//
//            // Lấy thông tin từ request
//            val hoten = request["hoten"]
//            val sdt = request["sdt"]
//            val ngaysinhStr = request["ngaysinh"]
//            val cccd = request["cccd"]
//            val quequan = request["quequan"]
//            val gioitinh = request["gioitinh"]
//            val sodu = request["sodu"]?.toIntOrNull()
//            val idTaiKhoan = request["idTaiKhoan"]?.toIntOrNull()
//
//            // Kiểm tra các tham số bắt buộc
//            if (hoten.isNullOrEmpty() || sdt.isNullOrEmpty() || ngaysinhStr.isNullOrEmpty() || cccd.isNullOrEmpty() || idTaiKhoan == null) {
//                call.respond(mapOf("error" to "Các trường bắt buộc không được để trống"))
//                return@post
//            }
//
//            // Định nghĩa dateFormatter cho định dạng dd/MM/yyyy
//            val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
//
//            // Chuyển đổi ngaysinh từ String sang LocalDate với định dạng dd/MM/yyyy
//            val ngaysinh = try {
//                LocalDate.parse(ngaysinhStr, dateFormatter)
//            } catch (e: DateTimeParseException) {
//                call.respond(mapOf("error" to "Ngày sinh không hợp lệ. Định dạng phải là dd/MM/yyyy"))
//                return@post
//            }
//
//            // Tạo đối tượng NguoiDung
//            val nguoiDung = NguoiDung(
//                hoten = hoten,
//                sdt = sdt,
//                ngaysinh = ngaysinh,
//                cccd = cccd,
//                quequan = quequan,
//                gioitinh = gioitinh,
//                sodu = sodu,
//                idTaiKhoan = idTaiKhoan
//            )
//
//            // Thêm người dùng vào cơ sở dữ liệu
//            val addedNguoiDung = nguoiDungDAO.addNguoiDung(nguoiDung)
//
//            // Trả về thông tin người dùng đã thêm vào cơ sở dữ liệu
//            call.respond(mapOf("message" to "Thêm người dùng thành công", "nguoiDung" to addedNguoiDung))
//        }
//        //----------------------------------------------------------------------------------------------------------
//        post("/post/add/nguoidung") {
//            val request = call.receive<Map<String,String>>()
//
//            val tentk = request["tentk"]
//            val hoten = request["hoten"]
//            val sdt = request["sdt"]
//            val ngaysinhStr = request["ngaysinh"]
//            val cccd = request["cccd"]
//            val quequan = request["quequan"]
//            val gioitinh = request["gioitinh"]
//            val sodu = request["sodu"]?.toIntOrNull()
//
//            //kiểm tra các tham số bắt buộc
//            if (tentk.isNullOrEmpty() || hoten.isNullOrEmpty() || sdt.isNullOrEmpty() || ngaysinhStr.isNullOrEmpty() || cccd.isNullOrEmpty()) {
//                call.respond(mapOf("error" to "Các trường bắt buộc không được để trống"))
//                return@post
//            }
//            // Định nghĩa dateFormatter cho định dạng dd/MM/yyyy
//            val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
//
//            // Chuyển đổi ngaysinh từ String sang LocalDate với định dạng dd/MM/yyyy
//            val ngaysinh = try {
//                LocalDate.parse(ngaysinhStr, dateFormatter)
//            } catch (e: DateTimeParseException) {
//                call.respond(mapOf("error" to "Ngày sinh không hợp lệ. Định dạng phải là dd/MM/yyyy"))
//                return@post
//            }
//            //lấy id từ tên tk
//            val idTaiKhoan = taiKhoanDAO.getIdByTenTK(tentk)
//            if (idTaiKhoan == null) {
//                call.respond(mapOf("error" to "Không tìm thấy tài khoản với tên đã cho"))
//                return@post
//            }
//            // tạo người dùng
//            val nguoiDung = NguoiDung(
//                hoten = hoten,
//                sdt = sdt,
//                ngaysinh = ngaysinh,
//                cccd = cccd,
//                quequan = quequan,
//                gioitinh = gioitinh,
//                sodu = sodu,
//                idTaiKhoan = idTaiKhoan
//            )
//            //thêm mới người dùng
//            val newNguoiDung = nguoiDungDAO.addNguoiDung(nguoiDung)
//            call.respond(mapOf("success" to "Thêm người dùng thành công", "nguoiDungId" to newNguoiDung.idBenhNhan))
//        }
        //kiem tra tai khoan mat khau-----------------------------------------------------------------------------
        checkLogin()
        //kiem tra ton tai--------------------------------------------------------------------------------------------
        checkTk()
        //xoa tai khoan-------------------------------------------------------------------------------------------
        deleteTk()
        // lấy id theo tai khoan
        getIdbyTenTk()
        // laaysys nnguoiwfi dung theo id tai khoan
        getNguoiDungByIdTk()
        // cap nhat so du
        updateSoDu()
        // dang ky
        addTaiKhoanAndNguoiDung()
        themLichKham()
        getBacSiById()
        layLichKhamTheoNguoiDung()
        capNhatTrangThaiLichKham()
        layallLichKhamTheoNguoiDung()
        kiemTraLichKham()
        addThongBao()
        getThongBao()
        updateDaXem()
        updateAllDaNhanByIdBenhNhan()
        kiemTraThongBaoChuaNhan()
        checkLoginBacSi()
        getBacSiByIdTK()
        lichKhamHomNay()
        lichKhamToanBoCuaBacSi()
        getNguoiDungByIdBenhNhan()
        getAllChucNang()
        addKhamChucNang()
        getKhamChucNangByIdLichKham()
        getChucNangbyIdChucNang()
        checkLoginChucNang()
        getChucNangByIdTaiKhoan()
        getThongTinBenhNhanByIdChucNangRoute()
        getKhamChucNangByIdChucNang()
        getKhamChucNangByIdChucNangAndLichKham()
        uploadFile()
        serveFiles()
        updateTrangThaiKhamById()
        getFileById()
        getKetQuaKhamByIdLichKham()
        getKetQuaKhamByIdBenhNhan()
        addKetQuaKham()
        getLichHenByIdLichKham()
        getFileByIdLichKham()
        capNhatTrangThaiThanhToan()
        updateMaPin()
        hasMaPin()
        checkMaPin()
        changePassWord()
        lichKhamDaKhamCuaBacSi()
        encryptPasswordRoute()
        addTaiKhoanAndBacSi()
        sendNotification()
        updateFCMToken()

 }


}
