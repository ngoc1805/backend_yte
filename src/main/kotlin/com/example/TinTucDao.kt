package com.example

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.ktorm.database.Database
import org.ktorm.dsl.*

@Serializable
data class TinTuc(
    val tinTucId: Int = 1,
    val tieuDe: String,
    val linkAnh: String?,
    val noiDung: String

)
class TinTucDao(private val database: Database) {

    //thêm một tin tức mới
    fun addTinTuc(tieuDe: String, linkAnh: String?,noiDung: String): TinTuc{
        val insertResult = database.insertAndGenerateKey(TinTucTable){
            set(TinTucTable.tieuDe,tieuDe)
            set(TinTucTable.linkAnh,linkAnh)
            set(TinTucTable.noiDung,noiDung)
        }
        return TinTuc(
            tinTucId = insertResult as Int,
            tieuDe = tieuDe,
            linkAnh = linkAnh,
            noiDung = noiDung
        )
    }
    //lay tin tuc theo so
    fun getTinTucById(id: Int): TinTuc? {
        return database.from(TinTucTable)
            .select()
            .where { TinTucTable.tinTucId eq id }  // Lọc theo id
            .map { row ->
                TinTuc(
                    tinTucId = row[TinTucTable.tinTucId] ?: 0,
                    tieuDe = row[TinTucTable.tieuDe] ?: "",
                    linkAnh = row[TinTucTable.linkAnh] ?: "",
                    noiDung = row[TinTucTable.noiDung] ?: ""
                )
            }
            .singleOrNull()  // Chỉ lấy một bản ghi hoặc null nếu không có bản ghi nào
    }
    // lấy tất cả tin tức
    fun getAllTinTuc(): List<TinTuc>{
        return database.from(TinTucTable)
            .select()
            .orderBy(TinTucTable.tinTucId.desc())
            .map { row ->
                TinTuc(
                    tinTucId = row[TinTucTable.tinTucId] ?: 0,
                    tieuDe = row[TinTucTable.tieuDe] ?: "",
                    linkAnh = row[TinTucTable.linkAnh] ?: "",
                    noiDung = row[TinTucTable.noiDung] ?: ""

                )
            }
    }

}
fun Route.getAllTinTuc(){
    route("/get"){
        get("/tintuc/all"){
            val allTinTuc = tinTucDao.getAllTinTuc()
            call.respond(allTinTuc)
        }
    }
}
fun Route.addTinTuc(){
    route("/post"){
        post("/tintuc"){
            val request = call.receive<Map<String,String>>()
            val tieuDe = request["tieu_de"]
            val linkAnh = request["link_anh"]
            val noiDung = request["noi_dung"]

            if (tieuDe.isNullOrEmpty() || noiDung.isNullOrEmpty()) {
                call.respond(mapOf("error" to "Tiêu đề và nội dung không được để trống "))
                return@post
            }
            // thêm tin tức vào cơ sở dữ liệu
            val newTinTuc = tinTucDao.addTinTuc(tieuDe, linkAnh, noiDung)
            call.respond(newTinTuc)
        }
    }
}
fun Route.GetTinTucById(){
    route("/get"){
        get("/tintuc/id") {
            val id = call.request.queryParameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(mapOf("error" to "ID không hợp lệ"))
                return@get
            }
            val tinTuc = tinTucDao.getTinTucById(id)
            if (tinTuc == null) {
                call.respond(mapOf("error" to "Tin tức không tìm thấy"))
            } else {
                call.respond(tinTuc)
            }
        }
    }
}
