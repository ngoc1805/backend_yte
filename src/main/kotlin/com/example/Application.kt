package com.example



import io.ktor.http.*
import io.ktor.serialization.gson.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.ktorm.database.Database
import org.ktorm.dsl.*

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException


fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

val database = connectToDatabase()
val bacSiDAO = BacSiDAO(database)
val tinTucDao = TinTucDao(database)
val taiKhoanDAO = TaiKhoanDAO(database)
val nguoiDungDAO = NguoiDungDAO(database)
fun Application.modules() {
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
//        post("/post/add/taikhoan") {
//            val request = call.receive<Map<String,String>>()
//            val tentk = request["tentk"]
//            val matkhau = request["matkhau"]
//            val loaitkString = request["loaitk"]
//            // kiểm tra các tham số đầu vào
//            if(tentk.isNullOrEmpty() || matkhau.isNullOrEmpty() || loaitkString.isNullOrEmpty()){
//                call.respond(mapOf("error" to "Tên tài khoản, mật khẩu và loại tài khoản không được để trống"))
//                return@post
//            }
//            //kiểm tra xem tài khoản đã tồn tại chưa
//            val existingAccount = taiKhoanDAO.getTaiKhoanByTenTK(tentk)
//            if(existingAccount != null){
//                call.respond(mapOf("error" to "Tên tài khoản đã tồn tại"))
//                return@post
//            }
//            // chuyển đổi loaitk từ String sang LoaiTaiKhoan (enum)
//            val loaitk = try {
//                LoaiTaiKhoan.valueOf(loaitkString)
//            } catch (e: IllegalArgumentException) {
//                call.respond(mapOf("error" to "Loại tài khoản không hợp lệ"))
//                return@post
//            }
//            // tạo tài khoản mới
//            val newAccount = taiKhoanDAO.createTaiKhoan(tentk,matkhau,loaitk)
//            call.respond(mapOf("success" to "Tạo tài khoản thành công"))
//        }
        //them nguoi dung moi--------------------------------------------------------------
        post("/post/add/user") {
            // Lấy dữ liệu từ request body
            val request = call.receive<Map<String,String>>()

            // Lấy thông tin từ request
            val hoten = request["hoten"]
            val sdt = request["sdt"]
            val ngaysinhStr = request["ngaysinh"]
            val cccd = request["cccd"]
            val quequan = request["quequan"]
            val gioitinh = request["gioitinh"]
            val sodu = request["sodu"]?.toIntOrNull()
            val idTaiKhoan = request["idTaiKhoan"]?.toIntOrNull()

            // Kiểm tra các tham số bắt buộc
            if (hoten.isNullOrEmpty() || sdt.isNullOrEmpty() || ngaysinhStr.isNullOrEmpty() || cccd.isNullOrEmpty() || idTaiKhoan == null) {
                call.respond(mapOf("error" to "Các trường bắt buộc không được để trống"))
                return@post
            }

            // Định nghĩa dateFormatter cho định dạng dd/MM/yyyy
            val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

            // Chuyển đổi ngaysinh từ String sang LocalDate với định dạng dd/MM/yyyy
            val ngaysinh = try {
                LocalDate.parse(ngaysinhStr, dateFormatter)
            } catch (e: DateTimeParseException) {
                call.respond(mapOf("error" to "Ngày sinh không hợp lệ. Định dạng phải là dd/MM/yyyy"))
                return@post
            }

            // Tạo đối tượng NguoiDung
            val nguoiDung = NguoiDung(
                hoten = hoten,
                sdt = sdt,
                ngaysinh = ngaysinh,
                cccd = cccd,
                quequan = quequan,
                gioitinh = gioitinh,
                sodu = sodu,
                idTaiKhoan = idTaiKhoan
            )

            // Thêm người dùng vào cơ sở dữ liệu
            val addedNguoiDung = nguoiDungDAO.addNguoiDung(nguoiDung)

            // Trả về thông tin người dùng đã thêm vào cơ sở dữ liệu
            call.respond(mapOf("message" to "Thêm người dùng thành công", "nguoiDung" to addedNguoiDung))
        }
        //----------------------------------------------------------------------------------------------------------
        post("/post/add/nguoidung") {
            val request = call.receive<Map<String,String>>()

            val tentk = request["tentk"]
            val hoten = request["hoten"]
            val sdt = request["sdt"]
            val ngaysinhStr = request["ngaysinh"]
            val cccd = request["cccd"]
            val quequan = request["quequan"]
            val gioitinh = request["gioitinh"]
            val sodu = request["sodu"]?.toIntOrNull()

            //kiểm tra các tham số bắt buộc
            if (tentk.isNullOrEmpty() || hoten.isNullOrEmpty() || sdt.isNullOrEmpty() || ngaysinhStr.isNullOrEmpty() || cccd.isNullOrEmpty()) {
                call.respond(mapOf("error" to "Các trường bắt buộc không được để trống"))
                return@post
            }
            // Định nghĩa dateFormatter cho định dạng dd/MM/yyyy
            val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

            // Chuyển đổi ngaysinh từ String sang LocalDate với định dạng dd/MM/yyyy
            val ngaysinh = try {
                LocalDate.parse(ngaysinhStr, dateFormatter)
            } catch (e: DateTimeParseException) {
                call.respond(mapOf("error" to "Ngày sinh không hợp lệ. Định dạng phải là dd/MM/yyyy"))
                return@post
            }
            //lấy id từ tên tk
            val idTaiKhoan = taiKhoanDAO.getIdByTenTK(tentk)
            if (idTaiKhoan == null) {
                call.respond(mapOf("error" to "Không tìm thấy tài khoản với tên đã cho"))
                return@post
            }
            // tạo người dùng
            val nguoiDung = NguoiDung(
                hoten = hoten,
                sdt = sdt,
                ngaysinh = ngaysinh,
                cccd = cccd,
                quequan = quequan,
                gioitinh = gioitinh,
                sodu = sodu,
                idTaiKhoan = idTaiKhoan
            )
            //thêm mới người dùng
            val newNguoiDung = nguoiDungDAO.addNguoiDung(nguoiDung)
            call.respond(mapOf("success" to "Thêm người dùng thành công", "nguoiDungId" to newNguoiDung.nguoiDungId))
        }
        //kiem tra tai khoan mat khau-----------------------------------------------------------------------------
        checkLogin()
//        post("/post/login") {
//            val request = call.receive<Map<String,String>>()
//
//            val tentk = request["tentk"]
//            val matkhau = request["matkhau"]
//
//            if(tentk.isNullOrEmpty() || matkhau.isNullOrEmpty()){
//                call.respond(mapOf("error" to "Các trường bắt buộc không được để trống"))
//                return@post
//            }
//            val kttaikhoa = taiKhoanDAO.getTaiKhoanByTenTK(tentk)
//            val login = taiKhoanDAO.checkPassword(tentk,matkhau)
//            if(kttaikhoa != null && login == true){
//                call.respond(mapOf("exists" to true, "message" to "Đăng nhập thành công"))
//            }
//            else{
//                call.respond(mapOf("exists" to false, "message" to "Tài khoản hoặc mật khẩu không chính xác"))
//            }
//        }
        // kiểm tra tài khoản đã tồn tại chưa-----------------------------------------------------------------------
        checkTk()
//        get("/check/taikhoan") {
//            val tenTK = call.request.queryParameters["tenTK"]
//
//            // Kiểm tra nếu tenTK bị null hoặc trống
//            if (tenTK.isNullOrEmpty()) {
//                call.respond(mapOf("error" to "Tên tài khoản không được để trống"))
//                return@get
//            }
//            // Gọi phương thức getTaiKhoanByTenTK để kiểm tra sự tồn tại của tenTK
//            val taiKhoan = taiKhoanDAO.getTaiKhoanByTenTK(tenTK)
//            // Trả về kết quả
//            if (taiKhoan != null) {
//                call.respond(mapOf("exists" to true, "message" to "Tên tài khoản đã tồn tại"))
//            } else {
//                call.respond(mapOf("exists" to false, "message" to "Tên tài khoản chưa tồn tại"))
//            }
//
//        }
        // kiem tra ma khau---------------------------------------------------------------------------------------
        //xoa tai khoan-------------------------------------------------------------------------------------------
        deleteTk()
//        post("/post/delete_account") {
//            try {
//                // Nhận dữ liệu từ request body (JSON)
//                val requestBody = call.receive<Map<String, String>>()
//                val tentk = requestBody["tentk"]
//
//                // Kiểm tra `tentk` có tồn tại không
//                if (tentk.isNullOrEmpty()) {
//                    call.respond(mapOf("error" to "Tên tài khoản không được để trống"))
//                    return@post
//                }
//
//                // Gọi hàm xóa tài khoản từ DAO
//                val rowsDeleted = taiKhoanDAO.deleteTaiKhoanByTenTK(tentk)
//
//                // Trả về kết quả
//                if (rowsDeleted > 0) {
//                    call.respond(mapOf("message" to "Tài khoản đã được xóa", "rowsDeleted" to rowsDeleted))
//                } else {
//                    call.respond(mapOf("error" to "Không tìm thấy tài khoản để xóa"))
//                }
//            } catch (e: Exception) {
//                // Xử lý lỗi nếu có
//                call.respond(mapOf("error" to "Có lỗi xảy ra: ${e.message}"))
//            }
//        }
        // lấy id theo tai khoan
        getIdbyTenTk()
        getNguoiDungByIdTk()

 }


}
