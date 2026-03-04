package org.delcom.services

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import org.delcom.data.AppException
import org.delcom.data.DataResponse
import org.delcom.data.DestinationRequest
import org.delcom.helpers.ValidatorHelper
import org.delcom.repositories.IDestinationRepository
import java.io.File
import java.util.*

class DestinationService(private val destinationRepository: IDestinationRepository) {
    // Mengambil semua data Wisata
    suspend fun getAllDestinations(call: ApplicationCall) {
        val search = call.request.queryParameters["search"] ?: ""

        val destinations = destinationRepository.getDestinations(search)

        val response = DataResponse(
            "success",
            "Berhasil mengambil daftar Wisata",
            mapOf(Pair("destinations", destinations))
        )
        call.respond(response)
    }

    // Mengambil data Wisata berdasarkan id
    suspend fun getDestinationById(call: ApplicationCall) {
        val id = call.parameters["id"]
            ?: throw AppException(400, "ID Wisata tidak boleh kosong!")

        val destination = destinationRepository.getDestinationById(id) ?: throw AppException(404, "Data Wisata tidak tersedia!")

        val response = DataResponse(
            "success",
            "Berhasil mengambil data Wisata",
            mapOf(Pair("destination", destination))
        )
        call.respond(response)
    }

    // Ambil data request
    private suspend fun getDestinationRequest(call: ApplicationCall): DestinationRequest {
        // Buat object penampung
        val destinationReq = DestinationRequest()

        val multipartData = call.receiveMultipart(formFieldLimit = 1024 * 1024 * 5)
        multipartData.forEachPart { part ->
            when (part) {
                // Ambil request berupa teks
                is PartData.FormItem -> {
                    when (part.name) {
                        "nama" -> destinationReq.nama = part.value.trim()
                        "deskripsi" -> destinationReq.deskripsi = part.value
                        "lokasi" -> destinationReq.lokasi = part.value
                        "feedback" -> destinationReq.feedback = part.value
                    }
                }

                // Upload file
                is PartData.FileItem -> {
                    val ext = part.originalFileName
                        ?.substringAfterLast('.', "")
                        ?.let { if (it.isNotEmpty()) ".$it" else "" }
                        ?: ""

                    val fileName = UUID.randomUUID().toString() + ext
                    val filePath = "uploads/destinations/$fileName"

                    val file = File(filePath)
                    file.parentFile.mkdirs() // pastikan folder ada

                    part.provider().copyAndClose(file.writeChannel())
                    destinationReq.pathGambar = filePath
                }

                else -> {}
            }

            part.dispose()
        }

        return destinationReq
    }

    // Validasi request data dari pengguna
    private fun validateDestinationRequest(destinationReq: DestinationRequest){
        val validatorHelper = ValidatorHelper(destinationReq.toMap())
        validatorHelper.required("nama", "Nama tidak boleh kosong")
        validatorHelper.required("deskripsi", "Deskripsi tidak boleh kosong")
        validatorHelper.required("lokasi", "Lokasi tidak boleh kosong")
        validatorHelper.required("feedback", "Feedback tidak boleh kosong")
        validatorHelper.required("pathGambar", "Gambar tidak boleh kosong")
        validatorHelper.validate()

        val file = File(destinationReq.pathGambar)
        if (!file.exists()) {
            throw AppException(400, "Gambar Wisata gagal diupload!")
        }

    }

    // Menambahkan data Wisata
    suspend fun createDestination(call: ApplicationCall) {
        // Ambil data request
        val destinationReq = getDestinationRequest(call)

        // Validasi request
        validateDestinationRequest(destinationReq)

        // periksa destination dengan nama yang sama
        val existDestination = destinationRepository.getDestinationByName(destinationReq.nama)
        if(existDestination != null){
            val tmpFile = File(destinationReq.pathGambar)
            if(tmpFile.exists()){
                tmpFile.delete()
            }
            throw AppException(409, "Wisata dengan nama ini sudah terdaftar!")
        }

        val destinationId = destinationRepository.addDestination(
            destinationReq.toEntity()
        )

        val response = DataResponse(
            "success",
            "Berhasil menambahkan data Wisata",
            mapOf(Pair("destinationId", destinationId))
        )
        call.respond(response)
    }

    // Mengubah data Wisata
    suspend fun updateDestination(call: ApplicationCall) {
        val id = call.parameters["id"]
            ?: throw AppException(400, "ID Wisata tidak boleh kosong!")

        val oldDestination = destinationRepository.getDestinationById(id) ?: throw AppException(404, "Data Wisata tidak tersedia!")

        // Ambil data request
        val destinationReq = getDestinationRequest(call)

        if(destinationReq.pathGambar.isEmpty()){
            destinationReq.pathGambar = oldDestination.pathGambar
        }

        // Validasi request
        validateDestinationRequest(destinationReq)

        // periksa destination dengan nama yang sama jika nama diubah
        if(destinationReq.nama != oldDestination.nama){
            val existDestination = destinationRepository.getDestinationByName(destinationReq.nama)
            if(existDestination != null){
                val tmpFile = File(destinationReq.pathGambar)
                if(tmpFile.exists()){
                    tmpFile.delete()
                }
                throw AppException(409, "Wisata dengan nama ini sudah terdaftar!")
            }
        }

        // Hapus gambar lama jika mengupload file baru
        if(destinationReq.pathGambar != oldDestination.pathGambar){
            val oldFile = File(oldDestination.pathGambar)
            if(oldFile.exists()){
                oldFile.delete()
            }
        }

        val isUpdated = destinationRepository.updateDestination(
            id, destinationReq.toEntity()
        )
        if (!isUpdated) {
            throw AppException(400, "Gagal memperbarui data Wisata!")
        }

        val response = DataResponse(
            "success",
            "Berhasil mengubah data Wisata",
            null
        )
        call.respond(response)
    }

    // Menghapus data Wisata
    suspend fun deleteDestination(call: ApplicationCall) {
        val id = call.parameters["id"]
            ?: throw AppException(400, "ID Wisata tidak boleh kosong!")

        val oldDestination = destinationRepository.getDestinationById(id) ?: throw AppException(404, "Data Wisata tidak tersedia!")

        val oldFile = File(oldDestination.pathGambar)

        val isDeleted = destinationRepository.removeDestination(id)
        if (!isDeleted) {
            throw AppException(400, "Gagal menghapus data Wisata!")
        }

        // Hapus data gambar jika data Wisata sudah dihapus
        if (oldFile.exists()) {
            oldFile.delete()
        }

        val response = DataResponse(
            "success",
            "Berhasil menghapus data Wisata",
            null
        )
        call.respond(response)
    }

    // Mengambil gambar Wisata
    suspend fun getDestinationImage(call: ApplicationCall) {
        val id = call.parameters["id"]
            ?: return call.respond(HttpStatusCode.BadRequest)

        val destination = destinationRepository.getDestinationById(id)
            ?: return call.respond(HttpStatusCode.NotFound)

        val file = File(destination.pathGambar)

        if (!file.exists()) {
            return call.respond(HttpStatusCode.NotFound)
        }

        call.respondFile(file)
    }
}