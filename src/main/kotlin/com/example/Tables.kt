package com.example

import org.ktorm.database.Database
import org.ktorm.schema.*
import org.ktorm.dsl.*

// bảng bác sĩ
object BacSiTable : Table<Nothing>("bacSi") {
    val bacSiId = int("bac_si_id").primaryKey()
    val hoten = varchar("hoten")
    val idTaiKhoan = int("idtaikhoan")
    val khoa = varchar("khoa")
    val giakham = int("giakham")
}
// bảng tin tức
object TinTucTable : Table<Nothing>("tinTuc"){
    val tinTucId = int("tin_tuc_id").primaryKey()
    val tieuDe = varchar("tieu_de")
    val linkAnh = varchar("link_anh")
    val noiDung = varchar("noi_dung")
}

// bảng tài khoản
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
// bảng người dùng
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
// bảng lịch khám
 enum class TrangThai(val mota: String){
     da_len_lich("Đã lên lịch"),
     da_huy("Đã hủy"),
     da_hoan_tat("Đã hoàn tất")

 }

object LichKhamTable :Table<Nothing>("lichKham"){
    val lichKhamId = int("lich_kham_id").primaryKey()
    val benhNhanId = int("benh_nhan_id")
    val bacSiId = int("bac_si_id")
    val ngayKham = date("ngay_kham")
    val gioKham = time("gio_kham")
    val trangThai = varchar("trang_thai")
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