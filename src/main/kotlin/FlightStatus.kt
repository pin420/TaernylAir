


data class FlightStatus(
    val flightNumber: String,
    val passengerName: String,
    val passengerLoyaltyTier: LoyaltyTier,
    val originAirport: String,
    val distinationAirport: String,
    val status: String,
    val departureTimeInMinutes: Int
) {
    val isFilghtCanced: Boolean
        get() = status.equals("Canceled", ignoreCase = true)

    val hasBoardingStarted: Boolean
        get() = departureTimeInMinutes in 15..60

    val isBoardingOver: Boolean
        get() = departureTimeInMinutes < 15

    val isEligibleToBoard: Boolean
        get() = departureTimeInMinutes in 15..passengerLoyaltyTier.boardingWindowStart

    val boardingStatus: BoardingState
        get() = when {
            isFilghtCanced -> BoardingState.FilghtCanceled
            isBoardingOver -> BoardingState.BoardingEnded
            isEligibleToBoard -> BoardingState.Boarding
            hasBoardingStarted -> BoardingState.WaitingToBoard
            else -> BoardingState.BoardingNotStarted
        }


    companion object {
        fun parse(
            flightResponse: String,
            loyaltyResponse: String,
            passengerName: String
        ): FlightStatus {
            val (flightNumber, originAirport, distinationAirport, status,
                departureTimeInMinutes) = flightResponse.split(",")

            val (loyaltyTierName, milesFlown, milesToNextTier) =
                loyaltyResponse.split(",")

            return FlightStatus(
                flightNumber = flightNumber,
                passengerName = passengerName,
                passengerLoyaltyTier = LoyaltyTier.values()
                    .first { it.tierName == loyaltyTierName },
                originAirport = originAirport,
                distinationAirport = distinationAirport,
                status = status,
                departureTimeInMinutes = departureTimeInMinutes.toInt()
            )
        }
    }
}


enum class LoyaltyTier(
    val tierName: String,
    val boardingWindowStart: Int
) {
    Bronze("Bronze", 25),
    Silver("Silver", 25),
    Gold("Gold", 30),
    Platinum("Platinum", 35),
    Titanium("Titanium", 40),
    Diamond("Diamond", 45),
    DiamondPlus("DiamondPlus",50),
    DiamondPlusPlus("DiamondPlusPlus", 60)
}

enum class BoardingState {
    FilghtCanceled,
    BoardingNotStarted,
    WaitingToBoard,
    Boarding,
    BoardingEnded
}
