package com.example

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.serialization.Serializable
import org.ktorm.database.Database
import org.ktorm.dsl.*

@Serializable
data class FileKetQua(
    val idFileKetQua: Int? = null,
    val idLichKham: Int,
    val tenFile: String,
    val ngayTraKetQua: String,
    val idChucNang: String,
    val fileUrl: String
)

fun saveFileAndGenerateUrl(fileName: String, fileContent: ByteArray, serverIp: String, serverPort: Int): String {
    val uploadDir = "C:/localhost_files" // Thư mục lưu file

    // Tạo thư mục nếu chưa tồn tại
    val directory = File(uploadDir)
    if (!directory.exists()) {
        directory.mkdirs()
    }

    // Lưu file vào thư mục
    val filePath = "$uploadDir/$fileName"
    val file = File(filePath)
    file.writeBytes(fileContent)

    // Trả về URL
    return "http://$serverIp:$serverPort/files/$fileName"
}

class FileKetQuaDAO(private val database: Database) {
    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    fun themFileKetQua(fileKetQua: FileKetQua): Int {
        val parsedNgayTraKetQua = LocalDate.parse(fileKetQua.ngayTraKetQua, dateFormatter)
        return database.insertAndGenerateKey(FileKetQuaTable) { row ->
            set(row.idLichKham, fileKetQua.idLichKham)
            set(row.tenFile, fileKetQua.tenFile)
            set(row.ngayTraKetQua, parsedNgayTraKetQua)
            set(row.idChucNang, fileKetQua.idChucNang)
            set(row.filerUrl, fileKetQua.fileUrl)
        } as Int
    }
    fun getFileById(idLichKham: Int, idChucNang: String): FileKetQua? {
        return database.from(FileKetQuaTable)
            .select()
            .where { (FileKetQuaTable.idLichKham eq idLichKham) and (FileKetQuaTable.idChucNang eq idChucNang) }
            .map { row ->
                FileKetQua(
                    idFileKetQua = row[FileKetQuaTable.idFileKetQua],
                    idLichKham = row[FileKetQuaTable.idLichKham]!!,
                    tenFile = row[FileKetQuaTable.tenFile]!!,
                    ngayTraKetQua = row[FileKetQuaTable.ngayTraKetQua]?.toString() ?: "",
                    idChucNang = row[FileKetQuaTable.idChucNang]!!,
                    fileUrl = row[FileKetQuaTable.filerUrl]!!
                )
            }
            .firstOrNull()
    }
    fun getFileByIdLichKham(idLichKham: Int): List<FileKetQua>{
        return database.from(FileKetQuaTable)
            .select()
            .where { FileKetQuaTable.idLichKham eq idLichKham }
            .map { row ->
                FileKetQua(
                    idFileKetQua = row[FileKetQuaTable.idFileKetQua],
                    idLichKham = row[FileKetQuaTable.idLichKham]!!,
                    tenFile = row[FileKetQuaTable.tenFile]!!,
                    ngayTraKetQua = row[FileKetQuaTable.ngayTraKetQua]?.toString() ?: "",
                    idChucNang = row[FileKetQuaTable.idChucNang]!!,
                    fileUrl = row[FileKetQuaTable.filerUrl]!!
                )
            }
    }
}

fun Route.uploadFile() {
    route("/post") {
        post("/upload") {
            val multipart = call.receiveMultipart()
            var fileName: String? = null
            var fileBytes: ByteArray? = null
            var idLichKham: Int? = null
            var ngayTraKetQua: String? = null
            var idChucNang: String? = null

            multipart.forEachPart { part ->
                when (part) {
                    is PartData.FormItem -> {
                        when (part.name) {
                            "idLichKham" -> idLichKham = part.value.toInt()
                            "ngayTraKetQua" -> ngayTraKetQua = part.value
                            "idChucNang" -> idChucNang = part.value
                        }
                    }
                    is PartData.FileItem -> {
                        fileName = part.originalFileName
                        fileBytes = part.streamProvider().readBytes()
                    }
                    else -> Unit
                }
                part.dispose()
            }

            if (idLichKham == null || ngayTraKetQua == null || idChucNang == null || fileName == null || fileBytes == null) {
                call.respond(HttpStatusCode.BadRequest, "Thiếu thông tin cần thiết")
                return@post
            }

            val serverIp = "192.168.0.102" // Địa chỉ IP của máy chủ
            val serverPort = 8080 // Cổng của server
            val fileUrl = saveFileAndGenerateUrl(fileName!!, fileBytes!!, serverIp, serverPort)

            val fileKetQua = FileKetQua(
                idLichKham = idLichKham!!,
                tenFile = fileName!!,
                ngayTraKetQua = ngayTraKetQua!!,
                idChucNang = idChucNang!!,
                fileUrl = fileUrl
            )

            // Lưu thông tin file vào cơ sở dữ liệu
            val generatedId = fileKetQuaDAO.themFileKetQua(fileKetQua)

            call.respondText("File đã được tải lên với ID $generatedId và URL: $fileUrl", status = HttpStatusCode.OK)
        }
    }
}
//----------------------------------------------------------------------------------------------------------------------
fun Route.serveFiles() {
    get("/files/{fileName}") {
        val fileName = call.parameters["fileName"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing file name")
        val file = File("C:/localhost_files/$fileName")

        if (!file.exists()) {
            call.respond(HttpStatusCode.NotFound, "File not found")
            return@get
        }

        // Thêm header `Content-Disposition` để mở file trên trình duyệt
        call.response.header(
            HttpHeaders.ContentDisposition,
            ContentDisposition.Inline.withParameter(ContentDisposition.Parameters.FileName, fileName).toString()
        )

        // Đoán MIME type dựa trên phần mở rộng file (nếu không có, dùng OctetStream)
        val mimeType = when (file.extension.lowercase()) {
            "pdf" -> ContentType.Application.Pdf
            "docx" -> ContentType("application", "vnd.openxmlformats-officedocument.wordprocessingml.document")
            "pptx" -> ContentType("application", "vnd.openxmlformats-officedocument.presentationml.presentation")
            "txt" -> ContentType.Text.Plain
            else -> ContentType.Application.OctetStream
        }

        // Trả file về trình duyệt
        call.respondFile(file)
    }
}
//----------------------------------------------------------------------------------------------------------------------
fun Route.getFileById() {
    route("/get"){
        get("/files/idlichkham/idchucnang") {
            val idLichKham = call.parameters["idlichkham"]?.toIntOrNull()
            val idChucNang = call.parameters["idchucnang"]

            if (idLichKham == null || idChucNang.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest, "Thiếu thông tin idLichKham hoặc idChucNang")
                return@get
            }

            // Retrieve the file metadata from the database.
            val fileKetQua = fileKetQuaDAO.getFileById(idLichKham, idChucNang)

            if (fileKetQua == null) {
                call.respond(HttpStatusCode.NotFound, "Không tìm thấy file")
            } else {
                // Return file metadata
                call.respond(fileKetQua)
            }
        }
    }
}
//----------------------------------------------------------------------------------------------------------------------
fun Route.getFileByIdLichKham(){
    route("/get"){
        get("/files/idlichkham"){
            val idLichKham = call.parameters["idlichkham"]?.toIntOrNull()
            if(idLichKham == null){
                call.respond(HttpStatusCode.BadRequest,"Thiếu thông tin idLichKham")
                return@get
            }
            val fileKetQua = fileKetQuaDAO.getFileByIdLichKham(idLichKham)
            if(fileKetQua == null){
                call.respond(HttpStatusCode.NotFound, "Không tìm thấy file")
            }else{
                call.respond(fileKetQua)
            }
        }
    }
}


