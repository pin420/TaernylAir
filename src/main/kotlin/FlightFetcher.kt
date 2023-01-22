import java.net.URL


private const val BASE_URL = "http://kotlin-book.bignerdranch.com/2e"
private const val FLIGHT_ENDPOINT = "$BASE_URL/flight"


fun main() {
    val flight = fetchFlight()
    println(flight)
}

fun fetchFlight(): String = URL(FLIGHT_ENDPOINT).readText()
