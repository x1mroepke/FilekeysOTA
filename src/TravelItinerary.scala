/**
 * Created by IntelliJ IDEA.
 * User: Admin
 * Date: 10.04.11
 * Time: 16:53
 * To change this template use File | Settings | File Templates.
 */

object TravelItinerary {
  var travelers: List[TravellerInformation] = List[TravellerInformation]()
  var flights: List[FlightInformation] = List[FlightInformation]()

  override def toString = {
    var s = "\nTravelers:\n";
    for (t <- travelers.toArray[TravellerInformation]) {
      s += t.toString()
    }
    s += "\nFlights:\n";
    var i: Int = 1
    for (f <- flights) {
      s += "Flight " + i + "\n"
      s += f.toString()
      i += 1
    }
    s
  }

}