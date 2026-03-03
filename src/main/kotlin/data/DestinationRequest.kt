package org.delcom.data

import kotlinx.serialization.Serializable
import org.delcom.entities.Destination

@Serializable
data class DestinationRequest(
    var nama: String = "",
    var deskripsi: String = "",
    var lokasi: String = "",
    var feedback: String = "",
    var pathGambar: String = "",
){
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "nama" to nama,
            "deskripsi" to deskripsi,
            "lokasi" to lokasi,
            "feedback" to feedback,
            "pathGambar" to pathGambar
        )
    }

    fun toEntity(): Destination {
        return Destination(
            nama = nama,
            deskripsi = deskripsi,
            lokasi = lokasi,
            feedback = feedback,
            pathGambar =  pathGambar,
        )
    }

}