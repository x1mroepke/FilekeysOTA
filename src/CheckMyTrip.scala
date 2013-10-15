import java.io.FileWriter
import java.lang.String
import org.apache.log4j.Logger
import org.watij.webspec.dsl.WebSpec

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 19.03.11
 * Time: 15:23
 * To change this template use File | Settings | File Templates.
 */

object CheckMyTrip {

  val checkMyTripURL = "https://www.checkmytrip.com/plnext/XCMTXITN/CleanUpSessionPui.action?SITE=XCMTXITN&LANGUAGE=GB"

  val log = Logger.getLogger("CheckMyTrip")

  def main(args: Array[String]): Unit = {

    if (args.length < 2) {
      printf("Usage: %s name pnr", CheckMyTrip.getClass.getName)
    }
    else {
      val personName: String = args(0)
      val pnr: String = args(1)
      printf("Accessing CheckMyTripSite %s\nName: %s\nPNR: %s\n", checkMyTripURL, personName, pnr)
      val data = io.Source.fromURL(checkMyTripURL).mkString
      val out = new FileWriter(format("%s_%s.html", personName, pnr))
      out.write(data)
      out.close
      var spec = new WebSpec().ie

      spec = spec.open(checkMyTripURL)
      spec.find("input").`with`.id("REC_LOC").set(pnr)
      spec.find("input").`with`.id("DIRECT_RETRIEVE_LASTNAME").set(personName)
      spec.find("a").`with`("view itinerary").click
      val source: String = spec.source()
      log.info(source)
      //spec.close
    }

  }

}