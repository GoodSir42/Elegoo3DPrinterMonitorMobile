package eu.kutscheid.elegoomonitor.data

interface DataRepository {
    suspend fun fetchDataFromUdp(): Result<ByteArray>
    // You might want to parse the ByteArray into a more meaningful data model here
}

class DataRepositoryImpl(private val udpDataSource: UdpDataSource) : DataRepository {
    override suspend fun fetchDataFromUdp(): Result<ByteArray> {
        return try {
            val data = udpDataSource.receiveData()
            Result.success(data)
        } catch (e: Exception) {
            // Log the exception or handle it more gracefully
            Result.failure(e)
        }
    }
}