package org.delcom.repositories

import org.delcom.dao.DestinationDAO
import org.delcom.entities.Destination
import org.delcom.helpers.daoToModel
import org.delcom.helpers.suspendTransaction
import org.delcom.tables.DestinationTable
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.lowerCase
import java.util.UUID

class DestinationRepository : IDestinationRepository {
    override suspend fun getDestinations(search: String): List<Destination> = suspendTransaction {
        if (search.isBlank()) {
            DestinationDAO.all()
                .orderBy(DestinationTable.createdAt to SortOrder.DESC)
                .limit(20)
                .map(::daoToModel)
        } else {
            val keyword = "%${search.lowercase()}%"

            DestinationDAO
                .find {
                    DestinationTable.nama.lowerCase() like keyword
                }
                .orderBy(DestinationTable.nama to SortOrder.ASC)
                .limit(20)
                .map(::daoToModel)
        }
    }

    override suspend fun getDestinationById(id: String): Destination? = suspendTransaction {
        DestinationDAO
            .find { (DestinationTable.id eq UUID.fromString(id)) }
            .limit(1)
            .map(::daoToModel)
            .firstOrNull()
    }

    override suspend fun getDestinationByName(name: String): Destination? = suspendTransaction {
        DestinationDAO
            .find { (DestinationTable.nama eq name) }
            .limit(1)
            .map(::daoToModel)
            .firstOrNull()
    }

    override suspend fun addDestination(destination: Destination): String = suspendTransaction {
        val destinationDAO = DestinationDAO.new {
            nama = destination.nama
            pathGambar = destination.pathGambar
            deskripsi = destination.deskripsi
            lokasi = destination.lokasi
            feedback = destination.feedback
            createdAt = destination.createdAt
            updatedAt = destination.updatedAt
        }

        destinationDAO.id.value.toString()
    }

    override suspend fun updateDestination(id: String, newDestination: Destination): Boolean = suspendTransaction {
        val destinationDAO = DestinationDAO
            .find { DestinationTable.id eq UUID.fromString(id) }
            .limit(1)
            .firstOrNull()

        if (destinationDAO != null) {
            destinationDAO.nama = newDestination.nama
            destinationDAO.pathGambar = newDestination.pathGambar
            destinationDAO.deskripsi = newDestination.deskripsi
            destinationDAO.lokasi = newDestination.lokasi
            destinationDAO.feedback = newDestination.feedback
            destinationDAO.updatedAt = newDestination.updatedAt
            true
        } else {
            false
        }
    }

    override suspend fun removeDestination(id: String): Boolean = suspendTransaction {
        val rowsDeleted = DestinationTable.deleteWhere {
            DestinationTable.id eq UUID.fromString(id)
        }
        rowsDeleted == 1
    }

}