package org.delcom.helpers

import kotlinx.coroutines.Dispatchers
import org.delcom.dao.PlantDAO
import org.delcom.dao.DestinationDAO
import org.delcom.entities.Plant
import org.delcom.entities.Destination
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

suspend fun <T> suspendTransaction(block: Transaction.() -> T): T =
    newSuspendedTransaction(Dispatchers.IO, statement = block)

fun daoToModel(dao: PlantDAO) = Plant(
    dao.id.value.toString(),
    dao.nama,
    dao.pathGambar,
    dao.deskripsi,
    dao.manfaat,
    dao.efekSamping,
    dao.createdAt,
    dao.updatedAt
)

fun daoToModel(dao: DestinationDAO) = Destination(
    dao.id.value.toString(),
    dao.nama,
    dao.pathGambar,
    dao.deskripsi,
    dao.lokasi,
    dao.feedback,
    dao.createdAt,
    dao.updatedAt
)