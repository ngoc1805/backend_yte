package com.example

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.ktorm.database.Database
import org.ktorm.dsl.*

@Serializable
data class BacSi(val bacSiId: Int, val hoten: String, val khoa: String, val giakham: Int)
class BacSiDAO(private val database: Database) {

    //lay danh sach bac si theo khoa
    fun getBacSiByKhoa(khoa: String): List<BacSi> {
        return database.from(BacSiTable)
            .select()
            .where { BacSiTable.khoa eq khoa }
            .map { row ->
                BacSi(
                    bacSiId = row[BacSiTable.bacSiId] ?: 0,
                    hoten = row[BacSiTable.hoten] ?: "",
                    khoa = row[BacSiTable.khoa] ?: "",
                    giakham = row[BacSiTable.giakham] ?: 0
                )
            }
    }
    // lấy danh sách toàn bộ các sĩ
    fun getAllBacSi(): List<BacSi>{
        return database.from(BacSiTable)
            .select()
            .map { row ->
                BacSi(
                    bacSiId = row[BacSiTable.bacSiId] ?: 0,
                    hoten = row[BacSiTable.hoten] ?: "",
                    khoa = row[BacSiTable.khoa] ?: "",
                    giakham = row[BacSiTable.giakham] ?: 0
                )
            }
    }
}

fun Route.getAllBacSi(){

    route("get"){
        get("/bacsi/all"){
            val allBacSi = bacSiDAO.getAllBacSi()
            call.respond(allBacSi)
        }
    }
}