import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import BoardingState.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch


fun main() {
    runBlocking {
        println("Getting the latest flight info...")
        val flights = fetchFlights()
        val flightDescriptions = flights.joinToString {
            "${it.passengerName} (${it.flightNumber})"
        }
        println("Found flights for $flightDescriptions")
        val flightAtGate = MutableStateFlow(flights.size)
        launch {
            flightAtGate.collect { flightCount ->
                println("There are $flightCount flights being tracked")
            }
            println("Finished tracking all flights")
        }

        launch {
            flights.forEach {
                watchFlight(it)
                flightAtGate.value = flightAtGate.value - 1
            }
        }
    }
}

suspend fun watchFlight(initialFlight: FlightStatus) {
    val passengerName = initialFlight.passengerName

    val currentFlight: Flow<FlightStatus> = flow {
        var flight = initialFlight

        while (flight.departureTimeInMinutes >= 0 && !flight.isFilghtCanced) {
            emit(flight)
            delay(100)
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