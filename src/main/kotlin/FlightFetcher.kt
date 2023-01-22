import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.net.URL


private const val BASE_URL = "http://kotlin-book.bignerdranch.com/2e"
private const val FLIGHT_ENDPOINT = "$BASE_URL/flight"


fun main() {
    runBlocking {
        println("Started")

        launch {
            val flight = fetchFlight()
            println(flight)
        }
        println("Finished")
    }
}

fun fetchFlight(): String = URL(FLIGHT_ENDPOINT).readText()
