import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import BoardingState.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

val bannedPassengers = "Nogartse"

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
            flightAtGate
                .takeWhile { it > 0 }
                .onCompletion {
                    println("Finished tracking all flights")
                }
                .collect { flightCount ->
                println("There are $flightCount flights being tracked")
            }
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

        require(passengerName != bannedPassengers ) {
            "Cannot track $passengerName's flight. They are banned from the airport"
        }

        var flight = initialFlight

        while (flight.departureTimeInMinutes >= 0 && !flight.isFilghtCanced) {
            emit(flight)
            delay(100)
            flight = flight.copy(
                departureTimeInMinutes = flight.departureTimeInMinutes - 1
            )
        }
    }

//        currentFlight
//        .catch { throwable ->
//            throwable.printStackTrace()
//            emit(/*Резервное значение*/)
//        }
//        .collect { println("Got flight data: $it") }

    currentFlight
        .map { flight ->
            when (flight.boardingStatus) {
                FilghtCanceled -> "Your flight was canceld"
                BoardingNotStarted -> "Boarding will start soon"
                WaitingToBoard -> "Other passengers are boarding"
                Boarding -> "You can now board the plane"
                BoardingEnded -> "The boarding doors have closed"
            } + " (FLight departs in ${flight.departureTimeInMinutes} minutes)"
        }
        .onCompletion {
            println("Finished tracking $passengerName's flight")
        }
        .collect { status ->
            println("$passengerName: $status")
    }
}

suspend fun fetchFlights(passengerNames: List<String> = listOf("Madrigal", "Polarcubis")): List<FlightStatus> =
    coroutineScope {
        val passengerNamesChannel = Channel<String>()
        launch {
            passengerNames.forEach {
                passengerNamesChannel.send(it)
            }
        }

        launch {
            fetchFlightStatuses(passengerNamesChannel)
        }

        emptyList()
    }


suspend fun fetchFlightStatuses(
    fetchChannel: Channel<String>
) {
    val passengerName = fetchChannel.receive()
    val flight = fetchFlight(passengerName)
    println("Fetched flight: $flight")
}