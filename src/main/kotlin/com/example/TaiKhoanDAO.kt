package com.example

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.ktorm.database.Database
import org.ktorm.dsl.*
import at.favre.lib.crypto.bcrypt.BCrypt
import io.ktor.http.*


@Serializable
data class TaiKhoan(
    val tentk: String,
    val matkhau: String,
    val loaitk: LoaiTaiKhoan?,
    val idTaiKhoan: Int
)
@Serializable
data class ResponseMessage(
    val message: String? = null,
    val error: String? = null,
    val exists: Boolean? = null,
    val rowsDeleted: Int? = null

)
class TaiKhoanDAO(private val database: Database) {
    // lấy tài khoản theo tên tài khoản
    fun getTaiKhoanByTenTK(tentk: String): TaiKhoan?{
        return database.from(TaiKhoanTable)
            .select()
            .where( TaiKhoanTable.tenTK eq tentk)
            .map { row ->
                TaiKhoan(
                    tentk = row[TaiKhoanTable.tenTK] ?: "",
                    matkhau = row[TaiKhoanTable.matKhau] ?: "",
                    loaitk = row[TaiKhoanTable.loaiTK] ?.let { LoaiTaiKhoan.valueOf(it) },
                    idTaiKhoan = row[TaiKhoanTable.idTaiKhoan] ?: 0
                )
            }
            .singleOrNull() // trả về null nếu không tìm thấy tài khoản
    }
    // Phương thức kiểm tra mật khẩu của tài khoản theo tên tài khoản
//    fun checkPassword(tentk: String, inputPassword: String): Boolean {
//        val taiKhoan = getTaiKhoanByTenTK(tentk)
//        return taiKhoan?.matkhau == inputPassword
//    }
    fun checkPassword(tentk: String, inputPassword: String): Boolean {
        val taiKhoan = getTaiKhoanByTenTK(tentk)
        return taiKhoan?.let {
            it.loaitk == LoaiTaiKhoan.benhnhan &&
            BCrypt.verifyer().verify(inputPassword.toCharArray(), it.matkhau).verified
        } ?: false
    }

    // kiem tra mat khau bac si
//    fun checkPasswordBacSi(tentk: String, inputPassword: String): Boolean {
//        // Lấy tài khoản theo tên tài khoản
//        val taiKhoan = getTaiKhoanByTenTK(tentk)
//        return taiKhoan?.loaitk == LoaiTaiKhoan.bacsi && taiKhoan.matkhau == inputPassword
//    }
    fun checkPasswordBacSi(tentk: String, inputPassword: String): Boolean {
        // Lấy tài khoản theo tên tài khoản
        val taiKhoan = getTaiKhoanByTenTK(tentk)

        // Kiểm tra nếu tài khoản không null, loại tài khoản là "bacsi", và mật khẩu khớp
        return taiKhoan?.let {
            it.loaitk == LoaiTaiKhoan.bacsi &&
                    BCrypt.verifyer().verify(inputPassword.toCharArray(), it.matkhau).verified
        } ?: false
    }

//    fun checkPasswordChucNang(tentk: String, inputPassword: String): Boolean {
//        // Lấy tài khoản theo tên tài khoản
//        val taiKhoan = getTaiKhoanByTenTK(tentk)
//
//        // Kiểm tra nếu tài khoản không null, loại tài khoản là "chucnang", và mật khẩu khớp
//        return taiKhoan?.loaitk == LoaiTaiKhoan.chucnang && taiKhoan.matkhau == inputPassword
//    }
fun checkPasswordChucNang(tentk: String, inputPassword: String): Boolean {
    // Lấy tài khoản theo tên tài khoản
    val taiKhoan = getTaiKhoanByTenTK(tentk)

    // Kiểm tra nếu tài khoản không null, loại tài khoản là "chucnang", và mật khẩu khớp
    return taiKhoan?.let {
        it.loaitk == LoaiTaiKhoan.chucnang &&
                BCrypt.verifyer().verify(inputPassword.toCharArray(), it.matkhau).verified
    } ?: false
}


    // Phương thức lấy id_taikhoan theo tên tài khoản (tentk)
    fun getIdByTenTK(tentk: String): Int? {
        return database.from(TaiKhoanTable)
            .select(TaiKhoanTable.idTaiKhoan)
            .where { TaiKhoanTable.tenTK eq tentk }
            .map { row -> row[TaiKhoanTable.idTaiKhoan] }
            .singleOrNull()
    }
    // tạo tài khoản mới
//    fun createTaiKhoan(tentk: String, matkhau: String, loaitk: LoaiTaiKhoan): TaiKhoan{
//
//
//        val newId = database.insertAndGenerateKey(TaiKhoanTable){
//            set(TaiKhoanTable.tenTK, tentk)
//            set(TaiKhoanTable.matKhau, matkhau)
//            set(TaiKhoanTable.loaiTK, loaitk.name)
//        } as Int
//        return TaiKhoan(tentk,matkhau,loaitk,newId)
//    }
    fun createTaiKhoan(tentk: String, matkhau: String, loaitk: LoaiTaiKhoan): TaiKhoan {
        val hashedPassword = BCrypt.withDefaults().hashToString(12, matkhau.toCharArray())

        val newId = try {
            database.insertAndGenerateKey(TaiKhoanTable) {
                set(TaiKhoanTable.tenTK, tentk)
                set(TaiKhoanTable.matKhau, hashedPassword)
                set(TaiKhoanTable.loaiTK, loaitk.name)
            }
        } catch (e: Exception) {
            println("Error while inserting account: ${e.message}")
            throw Exception("Failed to insert and generate key")
        }

        println("Generated Key: $newId")

        if (newId !is Int) {
            throw Exception("Generated key is not an Int: $newId")
        }

        return TaiKhoan(tentk, hashedPassword, loaitk, newId)
    }


    // Phương thức xóa tài khoản theo tên tài khoản

    fun deleteTaiKhoanByTenTK(tentk: String): Int {
        return database.delete(TaiKhoanTable) {
            it.tenTK eq tentk
        }
    }
//    fun checkPassWordById(idTaiKhoan: Int, passWord: String): Boolean{
//        val passWordDb = database.from(TaiKhoanTable)
//            .select(TaiKhoanTable.matKhau)
//            .where{TaiKhoanTable.idTaiKhoan eq idTaiKhoan}
//            .map { row -> row[TaiKhoanTable.matKhau] }
//            .singleOrNull()
//
//        return passWordDb == passWord
//    }
fun checkPassWordById(idTaiKhoan: Int, passWord: String): Boolean {
    // Lấy mật khẩu mã hóa từ cơ sở dữ liệu
    val hashedPasswordDb = database.from(TaiKhoanTable)
        .select(TaiKhoanTable.matKhau)
        .where { TaiKhoanTable.idTaiKhoan eq idTaiKhoan }
        .map { row -> row[TaiKhoanTable.matKhau] }
        .singleOrNull()

    // Kiểm tra mật khẩu người dùng nhập với mật khẩu mã hóa
    return hashedPasswordDb?.let {
        BCrypt.verifyer().verify(passWord.toCharArray(), it).verified
    } ?: false
}

//    fun updatePassWord(idTaiKhoan: Int, newPassWord: String):Boolean{
//        val updateRows = database.update(TaiKhoanTable){
//            set(TaiKhoanTable.matKhau, newPassWord)
//            where { TaiKhoanTable.idTaiKhoan eq idTaiKhoan }
//        }
//        return updateRows > 0
//    }
    //
fun updatePassWord(idTaiKhoan: Int, newPassWord: String): Boolean {
    return try {
        // Mã hóa mật khẩu mới bằng BCrypt
        val hashedPassword = BCrypt.withDefaults().hashToString(12, newPassWord.toCharArray())

        // Cập nhật mật khẩu đã mã hóa vào cơ sở dữ liệu
        val updateRows = database.update(TaiKhoanTable) {
            set(TaiKhoanTable.matKhau, hashedPassword)
            where { TaiKhoanTable.idTaiKhoan eq idTaiKhoan }
        }

        updateRows > 0 // Trả về true nếu cập nhật thành công
    } catch (e: Exception) {
        println("Error updating password: ${e.message}")
        false // Trả về false nếu có lỗi
    }
}

    fun encryptPassword(idTaiKhoan: Int, plainPassword: String): Boolean {
        return try {
            val hashedPassword = BCrypt.withDefaults().hashToString(12, plainPassword.toCharArray())
            val updateRows = com.example.database.update(TaiKhoanTable) {
                set(TaiKhoanTable.matKhau, hashedPassword)
                where { TaiKhoanTable.idTaiKhoan eq idTaiKhoan }
            }
            updateRows > 0
        } catch (e: Exception) {
            println("Error encrypting password: ${e.message}")
            false
        }
    }

}



//Thêm tài khoản
fun Route.addTaiKhoan(){
    route("/post"){
        post("/add/taikhoan") {
            val request = call.receive<Map<String,String>>()
            val tentk = request["tenTK"]
            val matkhau = request["matKhau"]
            val loaitkString = request["loaiTK"]
            // kiểm tra các tham số đầu vào
            if(tentk.isNullOrEmpty() || matkhau.isNullOrEmpty() || loaitkString.isNullOrEmpty()){
                call.respond(ResponseMessage(error = "Tên tài khoản, mật khẩu và loại tài khoản không được để trống"))
                return@post
            }
            //kiểm tra xem tài khoản đã tồn tại chưa
            val existingAccount = taiKhoanDAO.getTaiKhoanByTenTK(tentk)
            if(existingAccount != null){
                call.respond(ResponseMessage(error = "Tài khoản đã tồn tại"))
                return@post
            }
            // chuyển đổi loaitk từ String sang LoaiTaiKhoan (enum)
            val loaitk = try {
                LoaiTaiKhoan.valueOf(loaitkString)
            } catch (e: IllegalArgumentException) {
                call.respond(ResponseMessage(error = "Loại tài khoản không hợp lệ"))
                return@post
            }
            // tạo tài khoản mới
            val newAccount = taiKhoanDAO.createTaiKhoan(tentk,matkhau,loaitk)
            call.respond(ResponseMessage(message = "Tạo tài khoản thành công"))
        }
    }
    //



}


//----------------------------------------------------------------------------------------------------------------------
// kiểm tra tai khoan mat khau
fun Route.checkLogin() {
    route("/post") {
        post("/login") {
            val request = call.receive<Map<String, String>>()
            val tentk = request["tentk"]
            val matkhau = request["matkhau"]
            if (tentk.isNullOrEmpty() || matkhau.isNullOrEmpty()) {
                call.respond(ResponseMessage(error = "Các trường bắt buộc không được để trống"))
                return@post
            }
            val kttaikhoa = taiKhoanDAO.getTaiKhoanByTenTK(tentk)
            val login = taiKhoanDAO.checkPassword(tentk, matkhau)
            if (kttaikhoa != null && login) {
                call.respond(ResponseMessage(exists = true, message = "Đăng nhập thành công"))
            } else {
                call.respond(ResponseMessage(exists = false, message = "Tài khoản hoặc mật khẩu không chính xác"))
            }
        }
    }
}

//----------------------------------------------------------------------------------------------------------------------
// kiểm tra tài khoản đã tồn tại chưa
fun Route.checkTk(){
    route("/check"){
        get("/taikhoan") {
            val tenTK = call.request.queryParameters["tenTK"]
            // Kiểm tra nếu tenTK bị null hoặc trống
            if (tenTK.isNullOrEmpty()) {
                call.respond(ResponseMessage(error = "Tên tài khoản không được để trống"))
                return@get
            }
            // Gọi phương thức getTaiKhoanByTenTK để kiểm tra sự tồn tại của tenTK
            val taiKhoan = taiKhoanDAO.getTaiKhoanByTenTK(tenTK)
            // Trả về kết quả
            if (taiKhoan != null) {
                call.respond(ResponseMessage(exists = true, message = "Tên tài khoản đã tồn tại"))
            } else {
                call.respond(ResponseMessage(exists = false, message = "Tên tài khoản chưa tồn tại"))
            }
        }
    }
}
//----------------------------------------------------------------------------------------------------------------------
fun Route.deleteTk(){
    route("/post"){
        post("/delete_account") {
            try {
                // Nhận dữ liệu từ request body (JSON)
                val requestBody = call.receive<Map<String, String>>()
                val tentk = requestBody["tentk"]
                // Kiểm tra `tentk` có tồn tại không
                if (tentk.isNullOrEmpty()) {
                    call.respond(ResponseMessage(error = "Tên tài khoản không được để trống"))
                    return@post
                }
                // Gọi hàm xóa tài khoản từ DAO
                val rowsDeleted = taiKhoanDAO.deleteTaiKhoanByTenTK(tentk)
                // Trả về kết quả
                if (rowsDeleted > 0) {
                    call.respond(ResponseMessage(message = "Tài khoản đã được xóa", rowsDeleted = rowsDeleted))
                } else {
                    call.respond(ResponseMessage(error = "Không tìm thấy tài khoản để xóa"))
                }
            } catch (e: Exception) {
                // Xử lý lỗi nếu có
                call.respond(ResponseMessage(error = "Có lỗi xảy ra: ${e.message}"))
            }
        }
    }
}
//----------------------------------------------------------------------------------------------------------------------
fun Route.getIdbyTenTk(){
    route("/post"){
            post("/id/bytentk") {
            val requestBody = call.receive<Map<String, String>>()
            val tentk = requestBody["tentk"]
            if(tentk.isNullOrEmpty()){
                call.respond(ResponseMessage(error = "Tên tài khoản không được để trống"))
                return@post
            }
            val idTk = taiKhoanDAO.getIdByTenTK(tentk)
            call.respond(ResponseMessage(rowsDeleted = idTk))
        }
    }
}
//----------------------------------------------------------------------------------------------------------------------
fun Route.checkLoginBacSi() {
    route("/post") {
        post("/login/bacsi") {
            // Nhận dữ liệu từ request body (JSON)
            val request = call.receive<Map<String, String>>()
            val tentk = request["tentk"]
            val matkhau = request["matkhau"]

            // Kiểm tra các trường bắt buộc không được để trống
            if (tentk.isNullOrEmpty() || matkhau.isNullOrEmpty()) {
                call.respond(ResponseMessage(error = "Tên tài khoản và mật khẩu không được để trống"))
                return@post
            }

            // Kiểm tra tài khoản bác sĩ và mật khẩu
            val isLoginSuccessful = taiKhoanDAO.checkPasswordBacSi(tentk, matkhau)

            // Kiểm tra kết quả
            if (isLoginSuccessful) {
                call.respond(ResponseMessage(exists = true, message = "Đăng nhập bác sĩ thành công"))
            } else {
                call.respond(ResponseMessage(exists = false, message = "Tài khoản hoặc mật khẩu không chính xác hoặc không phải tài khoản bác sĩ"))
            }
        }
    }
}
//----------------------------------------------------------------------------------------------------------------------
fun Route.checkLoginChucNang(){
    route("/post"){
        post("/login/chucnang"){
            // Nhận dữ liệu từ request body (JSON)
            val request = call.receive<Map<String, String>>()
            val tentk = request["tentk"]
            val matkhau = request["matkhau"]

            // Kiểm tra các trường bắt buộc không được để trống
            if (tentk.isNullOrEmpty() || matkhau.isNullOrEmpty()) {
                call.respond(ResponseMessage(error = "Tên tài khoản và mật khẩu không được để trống"))
                return@post
            }

            // Kiểm tra tài khoản bác sĩ và mật khẩu
            val isLoginSuccessful = taiKhoanDAO.checkPasswordChucNang(tentk, matkhau)

            // Kiểm tra kết quả
            if (isLoginSuccessful) {
                call.respond(ResponseMessage(exists = true, message = "Đăng nhập bác sĩ thành công"))
            } else {
                call.respond(ResponseMessage(exists = false, message = "Tài khoản hoặc mật khẩu không chính xác hoặc không phải tài khoản bác sĩ"))
            }
        }
    }
}
//----------------------------------------------------------------------------------------------------------------------
fun Route.changePassWord(){
    route("/post"){
        post("/changepassword") {
            val request = call.receive<Map<String,String>>()
            val idTaiKhoan = request["idTaiKhoan"]?.toIntOrNull()
            val currentPassWord = request["currentPassWord"]
            val newPassWord = request["newPassWord"]
            if(idTaiKhoan == null || currentPassWord.isNullOrEmpty() || newPassWord.isNullOrEmpty()){
                call.respond(HttpStatusCode.BadRequest,"id tài khoản, mật khẩu hiện tại, mật khẩu mới không được để trống")
                return@post
            }
            val checkPassWord = taiKhoanDAO.checkPassWordById(idTaiKhoan,currentPassWord)
            if (checkPassWord){
                val success = taiKhoanDAO.updatePassWord(idTaiKhoan,newPassWord)
                if(success){
                    call.respond(HttpStatusCode.OK,"Đổi mật khẩu thành công")
                }
                else{
                    call.respond(HttpStatusCode.BadRequest,"Đổi mật khẩu thất bại")
                }
            }
            else{
                call.respond(HttpStatusCode.NotFound,"Bạn đã nhập sai mật khẩu hiện tại")
                return@post
            }
        }
    }
}
//----------------------------------------------------------------------------------------------------------------------
fun Route.encryptPasswordRoute() {
    route("/post") {
        post("/encrypt-password") {
            val request = call.receive<Map<String, String>>()
            val idTaiKhoan = request["idTaiKhoan"]?.toIntOrNull()
            val plainPassword = request["matKhau"]

            if (idTaiKhoan == null || plainPassword.isNullOrEmpty()) {
                call.respond(HttpStatusCode.BadRequest, ResponseMessage(error = "ID tài khoản hoặc mật khẩu không hợp lệ"))
                return@post
            }

            val success = taiKhoanDAO.encryptPassword(idTaiKhoan, plainPassword)
            if (success) {
                call.respond(ResponseMessage(message = "Mật khẩu đã được mã hóa và cập nhật thành công"))
            } else {
                call.respond(HttpStatusCode.InternalServerError, ResponseMessage(error = "Lỗi trong quá trình mã hóa mật khẩu"))
            }
        }
    }
}




