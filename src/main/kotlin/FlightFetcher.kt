import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.URL


private const val BASE_URL = "http://kotlin-book.bignerdranch.com/2e"
private const val FLIGHT_ENDPOINT = "$BASE_URL/flight"


@OptIn(DelicateCoroutinesApi::class)
fun main() {
    println("Started")

    GlobalScope.launch {
        val flight = fetchFlight()
        println(flight)
    }

    println("Finished")
}

fun fetchFlight(): String = URL(FLIGHT_ENDPOINT).readText()
