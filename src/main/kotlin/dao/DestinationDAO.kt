package org.delcom.dao

import org.delcom.tables.DestinationTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import java.util.UUID


class DestinationDAO(id: EntityID<UUID>) : Entity<UUID>(id) {
    companion object : EntityClass<UUID, DestinationDAO>(DestinationTable)

    var nama by DestinationTable.nama
    var pathGambar by DestinationTable.pathGambar
    var deskripsi by DestinationTable.deskripsi
    var lokasi by DestinationTable.lokasi
    var feedback by DestinationTable.feedback
    var createdAt by DestinationTable.createdAt
    var updatedAt by DestinationTable.updatedAt
}