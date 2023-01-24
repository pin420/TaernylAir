import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import BoardingState.*


fun main() {
    runBlocking {
        println("Getting the latest flight info...")
        val flights = fetchFlights()
        val flightDescriptions = flights.joinToString {
            "${it.passengerName} (${it.flightNumber})"
        }
        println("Found flights for $flightDescriptions")
        flights.forEach {
            watchFlight(it)
        }
    }
}

suspend fun watchFlight(initialFlight: FlightStatus) {
    val passengerName = initialFlight.passengerName

    val currentFlight: Flow<FlightStatus> = flow {
        var flight = initialFlight

        while (flight.departureTimeInMinutes >= 0 && !flight.isFilghtCanced) {
            emit(flight)
            delay(1000)
            flight = flight.copy(
                departureTimeInMinutes = flight.departureTimeInMinutes - 1
            )
        }
    }

    currentFlight.collect {
        val status = when (it.boardingStatus) {
            FilghtCanceled -> "Your flight was canceld"
            BoardingNotStarted -> "Boarding will start soon"
            WaitingToBoard -> "Other passengers are boarding"
            Boarding -> "You can now board the plane"
            BoardingEnded -> "The boarding doors have closed"
        } + " (FLight departs in ${it.departureTimeInMinutes} minutes)"

        println("$passengerName: $status")
    }

    println("Finished tracking $passengerName's flight")
}

suspend fun fetchFlights(passengerNames: List<String> = listOf("Madrigal", "Polarcubis")) =
    passengerNames.map { fetchFlight(it) }