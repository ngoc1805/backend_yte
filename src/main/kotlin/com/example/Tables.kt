package com.example

import org.ktorm.database.Database
import org.ktorm.schema.*
import org.ktorm.dsl.*

object BacSiTable : Table<Nothing>("bacSi") {
    val bacSiId = int("bac_si_id").primaryKey()
    val hoten = varchar("hoten")
    val khoa = varchar("khoa")
    val giakham = int("giakham")

}
object TinTucTable : Table<Nothing>("tinTuc"){
    val tinTucId = int("tin_tuc_id").primaryKey()
    val tieuDe = varchar("tieu_de")
    val linkAnh = varchar("link_anh")
    val noiDung = varchar("noi_dung")
}

enum class LoaiTaiKhoan {
    bacsi,
    benhnhan
}
object TaiKhoanTable : Table<Nothing>("taiKhoan") {
    val tentk = varchar("tentk")
    val matkhau = varchar("matkhau")
    val loaitk = varchar("loaitk")
    val id_taikhoan = int("id_taikhoan").primaryKey()
}
object NguoiDungTable : Table<Nothing>("nguoiDung") {
    val nguoiDungId = int("nguoi_dung_id").primaryKey()
    val hoten = varchar("hoten")
    val sdt = varchar("sdt")
    val ngaysinh = date("ngaysinh")
    val cccd = varchar("cccd")
    val quequan = varchar("quequan")
    val gioitinh = varchar("gioitinh")
    val sodu = int("sodu")
    val idTaiKhoan = int("id_taikhoan")
}

fun connectToDatabase(): Database {
    val jdbcUrl = "jdbc:mysql://localhost:3306/yte"
    return Database.connect(
        url = jdbcUrl,
        driver = "com.mysql.cj.jdbc.Driver",
        user = "root",
        password = "180503"
    )
}