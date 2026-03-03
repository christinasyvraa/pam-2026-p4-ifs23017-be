package org.delcom.repositories

import org.delcom.entities.Destination

interface  IDestinationRepository {
    suspend fun getDestinations(search: String): List<Destination>
    suspend fun getDestinationById(id: String): Destination?
    suspend fun getDestinationByName(name: String): Destination?
    suspend fun addDestination(destination: Destination) : String
    suspend fun updateDestination(id: String, newDestination: Destination): Boolean
    suspend fun removeDestination(id: String): Boolean
}