package com.example

import io.ktor.util.*
import org.ktorm.database.Database
import org.ktorm.schema.*
import org.ktorm.dsl.*




// bảng bác sĩ
object BacSiTable : Table<Nothing>("bacSi") {
    val idBacSi = varchar("idBacSi").primaryKey()
    val hoTen = varchar("hoTen")
    val idTaiKhoan = int("idTaiKhoan")
    val khoa = varchar("khoa")
    val giaKham = int("giaKham")
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
    benhnhan,
    admin,
    chucnang
}
object TaiKhoanTable : Table<Nothing>("taiKhoan") {
    val tenTK = varchar("tenTK")
    val matKhau = varchar("matKhau")
    val loaiTK = varchar("loaiTK")
    val idTaiKhoan = int("idTaiKhoan").primaryKey()
}
// bảng người dùng
object BenhNhanTable : Table<Nothing>("benhNhan") {
    val idBenhNhan = varchar("idBenhNhan").primaryKey()
    val hoten = varchar("hoten")
    val sdt = varchar("sdt")
    val ngaysinh = date("ngaysinh")
    val cccd = varchar("cccd")
    val quequan = varchar("quequan")
    val gioitinh = varchar("gioitinh")
    val sodu = int("sodu")
    val idTaiKhoan = int("idTaiKhoan")
    val maPin = varchar("maPin")
}
// bảng lịch khám
 enum class TrangThai(val mota: String){
     da_len_lich("Đã lên lịch"),
     da_huy("Đã hủy"),
     da_hoan_tat("Đã hoàn tất"),
     da_thanh_toan("Đã thanh toán"),
     yc_thanh_toan("y/c thanh toán")
 }

object LichKhamTable :Table<Nothing>("lichKham"){
    val idLichKham = int("idLichKham").primaryKey()
    val idBenhNhan = varchar("idBenhNhan")
    val idBacSi = varchar("idBacSi")
    val ngayKham = date("ngayKham")
    val gioKham = time("gioKham")
    val trangThai = varchar("trangThai")
}

object ThongBaoTable :Table<Nothing>("thongBao"){
    val idThongBao = int("idThongBao").primaryKey()
    val idBenhNhan = varchar("idBenhNhan")
    val noiDung = varchar("noiDung")
    val daXem = boolean("daXem")
    val daNhan = boolean("daNhan")
    val thoiGian = datetime("thoiGian")
    val duongDan = varchar("duongDan")
}

object ChucNangTable :Table<Nothing>("chucNang"){
    val idChucNang = varchar("idChucNang").primaryKey()
    val tenChucNang = varchar("tenChucNang")
    val idTaiKhoan = int("idTaiKhoan")
    val giaKham = int("giaKham")
}
enum class TrangThaiCN(val mota: String){
    da_len_lich("Đã lên lịch"),
    da_hoan_tat("Đã hoàn tất")
}
object KhamChucNangTable :Table<Nothing>("khamChucNang"){
    val idKhamChucNang = int("idKhamChucNang").primaryKey()
    val idLichKham = int("idLichKham")
    val idChucNang = varchar("idChucNang")
    val gioKham = time("gioKham")
    val trangThai = varchar("trangThai")
}
object FileKetQuaTable :Table<Nothing>("fileKetQua"){
    val idFileKetQua = int("idFileKetQua").primaryKey()
    val idLichKham = int("idLichKham")
    val tenFile = varchar("tenFile")
    val ngayTraKetQua = date("ngayTraKetQua")
    val idChucNang = varchar("idChucNang")
    val filerUrl = varchar("fileUrl")
}
object KetQuaKhamTable :Table<Nothing>("ketQuaKham"){
    val idKetQua = int("idKetQua").primaryKey()
    val idLichKham = int("idLichKham")
    val nhanXet = varchar("nhanXet")
    val ngayTraKetQua = date("ngayTraKetQua")
    val daThanhToan = boolean("daThanhToan")
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