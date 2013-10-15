import java.lang.String
import org.apache.commons.lang.math.NumberUtils
import org.joda.time.DateTime

/**
 * Created by IntelliJ IDEA.
 * User: Admin
 * Date: 10.04.11
 * Time: 16:54
 * To change this template use File | Settings | File Templates.
 */

class FlightInformation {
  var eTicketNr: String = ""

  def get_eTicketNr() = {
    eTicketNr

  }

  var seatNr: String = ""

  def get_seatNr() = {
    seatNr

  }

  var pnr: String = null;

  def get_pnr() = {
    pnr

  }

  var flightNumber: String = null;

  def get_flightNumber() = {
    flightNumber.trim

  }

  var airCraft: String = null;

  def get_airCraft() = {
    airCraft

  }

  var extraInformation: String = null;

  def get_extraInformation() = {
    val info1: String = extraInformation.split(" ")(0).trim
    if (NumberUtils.isNumber(info1))
      info1 + " " + extraInformation.split(" ")(1)
    else
      ""
  }

  var duration: String = null;

  def get_duration() = {
    duration.trim

  }

  var airlineName: String = null;

  def get_airlineName() = {
    airlineName.split(" ")(0)

  }

  var airline2LetterCode: String = null;

  def get_airline2LetterCode() = {
    airline2LetterCode

  }

  var lastCheckIn: String = null;

  def get_lastCheckIn() = {
    lastCheckIn

  }

  var fareType: String = null;

  def get_fareType() = {
    fareType

  }

  var departureAirportCity: String = null;

  def get_departureAirportCity = {
    departureAirportCity
  }

  var departureAirport3LetterCode: String = null;

  def get_departureAirport3LetterCode() = {
    departureAirport3LetterCode

  }

  var departureAirportName: String = null;

  def get_departureAirportName() = {
    departureAirportName

  }

  var departureDateString: String = null;

  def get_departureDateString() = {
    departureDateString

  }

  var departureDate: DateTime = null;

  def get_departureDate() = {
    departureDate

  }

  def get_departureDateFormatted() = {
    departureDate.toString("dd.MM.yyyy")

  }

  var departureTime: String = null;

  def get_departureTime() = {
    departureTime.trim

  }

  var arrivalAirportCity: String = null;

  def get_arrivalAirportCity() = {
    arrivalAirportCity

  }

  var arrivalAirport3LetterCode: String = null;

  def get_arrivalAirport3LetterCode() = {
    arrivalAirport3LetterCode

  }

  var arrivalAirportName: String = null;

  def get_arrivalAirportName() = {
    arrivalAirportName

  }

  var arrivalDateString: String = null;

  def get_arrivalDateString() = {
    arrivalDateString

  }

  var arrivalTime: String = null;

  def get_arrivalTime() = {
    if (arrivalTimeExtraInformation != null) {
      val substring: String = arrivalTimeExtraInformation.substring(0, 2)
      if (arrivalTime.indexOf(substring) < 0)
        arrivalTime += substring;
    }
    arrivalTime.trim
  }

  var arrivalTimeExtraInformation: String = null;

  def get_arrivalTimeExtraInformation() = {
    arrivalTimeExtraInformation

  }

  var arrivalDate: DateTime = null;

  def get_arrivalDate() = {
    arrivalDate

  }

  override def equals(obj: Any) = {
    val f1 = obj.asInstanceOf[FlightInformation];
    if (f1.departureAirportCity.equals(departureAirportCity) && f1.arrivalAirportCity.equals(arrivalAirportCity))
      true
    else
      false
  }

  override def toString = {

    "flightNumber: " + flightNumber + "\n" +
      "airCraft: " + airCraft + "\n" +
      "extraInformation: " + extraInformation + "\n" +
      "duration: " + duration + "\n" +
      "airlineName: " + airlineName + "\n" +
      "airline2LetterCode: " + airline2LetterCode + "\n" +
      "departureAirportCity: " + departureAirportCity + "\n" +
      "departureAirport3LetterCode: " + departureAirport3LetterCode + "\n" +
      "departureAirportName: " + departureAirportName + "\n" +
      "departureDateString: " + departureDateString + "\n" +
      "departureDate: " + departureDate + "\n" +
      "departureTime: " + departureTime + "\n" +
      "arrivalAirportCity: " + arrivalAirportCity + "\n" +
      "arrivalAirport3LetterCode: " + arrivalAirport3LetterCode + "\n" +
      "arrivalAirportName: " + arrivalAirportName + "\n" +
      "arrivalDateString: " + arrivalDateString + "\n" +
      "arrivalTime: " + arrivalTime + "\n" +
      "arrivalTimeExtraInformation: " + arrivalTimeExtraInformation + "\n" +
      "arrivalDate: " + arrivalDate + "\n" + "\n" + "\n"
  }
}