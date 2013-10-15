/**
 * Created by IntelliJ IDEA.
 * User: Admin
 * Date: 10.04.11
 * Time: 16:44
 * To change this template use File | Settings | File Templates.
 */

class TravellerInformation {
  var lastName: String = null;

  def get_lastName() = {
    lastName

  }

  var firstName: String = null;

  def get_firstName() = {

    firstName
  }

  var ticketNumber: String = null;

  def get_ticketNumber() = {
    if (ticketNumber == null )
      ""
    else
      ticketNumber

  }

  override def toString = "lastName=" + lastName + ", " + "firstName=" + firstName + ", ticketNumber=" + ticketNumber
}