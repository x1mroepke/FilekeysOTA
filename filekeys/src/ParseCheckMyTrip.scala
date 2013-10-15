import java.io._
import java.lang.String
import java.util.{Properties, Locale}
import org.apache.log4j.Logger
import org.apache.velocity.app.VelocityEngine
import org.apache.velocity.tools.generic.DateTool
import org.joda.time.format.DateTimeFormat
import org.joda.time.DateTime
import org.springframework.context.support.ClassPathXmlApplicationContext
import org.springframework.ui.velocity.VelocityEngineUtils
import org.watij.webspec.dsl.{Tag, WebSpec}

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 19.03.11
 * Time: 15:23
 * To change this template use File | Settings | File Templates.
 */

object ParseCheckMyTrip {
  val checkMyTripURL = "https://www.checkmytrip.com/plnext/XCMTXITN/CleanUpSessionPui.action?SITE=XCMTXITN&LANGUAGE=GB"
  val log = Logger.getLogger("CheckMyTrip")
  var spec: WebSpec = null
  val templateFilekeys = "FilekeysItineraryWord2010.vm.xml"
  var personName: String = null
  var pnr: String = null


  def main(args: Array[String]): Unit = {
    try {
      spec = getWebSpec(args)
      extractTravelers
      var ind = 0
      var tablesTags: Tag = null
      do {
        tablesTags = spec.find("table").`with`.id("tabFgtReview_" + ind)
        if (tablesTags.exists) {
          val findtds: Tag = tablesTags.find("td")
          if (findtds.exists())
            extractFlights(findtds)
        }
        ind += 1
      } while (tablesTags.exists)
      val sh_fltItinerary_Etkt: Tag = spec.find("div").`with`.id("sh_fltItinerary_Etkt")
      val itineraryLen: Int = sh_fltItinerary_Etkt.all.length
      if (itineraryLen > 1) {
        val fltIninerary: Tag = sh_fltItinerary_Etkt.at(1)
        // <TH colSpan=3>Porto Alegre to Iguassu Falls </TH></TR>
        val thTags: Tag = fltIninerary.find("TH")
        if (thTags.exists) {
          val flightsTags: Tag = thTags.`with`("colSpan=='3'")
          flightsTags.at(0).get.innerHTML
          // <TD class=nowrap Departures and Arrivals
          val allCells: Tag = fltIninerary.find("TD")
          extractFlights(allCells)
        }
        spec.close
      }
      TravelItinerary.flights = TravelItinerary.flights.sortWith((f1: FlightInformation, f2: FlightInformation) => (f1.departureDate.isBefore(f2.departureDate)))
      log.info(TravelItinerary);
      createETicketBeleg
    }
    finally {
      if (spec != null)
        spec.close
    }
  }

  def getWebSpec(args: Array[String]): WebSpec = {
    spec = new WebSpec().ie
    if (args != null && args.length > 1) {
      personName = args(0)
      pnr = args(1)
      printf("Accessing CheckMyTripSite %s\nName: %s\nPNR: %s\n", checkMyTripURL, personName, pnr)

      spec = spec.open(checkMyTripURL)
      spec.find("input").`with`.id("REC_LOC").set(pnr)
      spec.find("input").`with`.id("DIRECT_RETRIEVE_LASTNAME").set(personName)
      spec.find("a").`with`("view itinerary").click()
      val source: String = spec.source()
      log.info(source)
      //val data = io.Source.fromURL(checkMyTripURL).mkString
      val out = new FileWriter(format("%s_%s.html", personName, pnr))
      out.write(source)
      out.close
      //spec.close
    }
    else if (args.length == 1 && args(0).endsWith(".html")) {
      val f = new File(args(0));
      val path: String = f.getCanonicalPath()
      spec = spec.open(path)
    } else {
      val s: String = String.format("Usage: %s name pnr|checkmytrip.html", ParseCheckMyTrip.getClass().getName())
      log.warn(s)
      throw new Exception(s)
    }
    spec
  }

  def getEncoding(f: String) = {
    val in = new File(f);
    val r = new InputStreamReader(new FileInputStream(in));
    r.getEncoding()
  }

  def extractETicketNumber: Unit = {
    var ind = 1
    var eticketsTags: Tag = null
    do {
      eticketsTags = spec.find("th").`with`.id("documentNumber_" + ind)
      if (eticketsTags.exists) {
        val parentTag: Tag = eticketsTags.parent("TR")
        if (parentTag.exists) {
          val nameTag: Tag = parentTag.find("TD")
          if (nameTag.exists) {
            val name: String = nameTag.get.innerText
            val fname = name.split(" ")(0)
            val lname = name.split(" ")(1)
            val foundTrav: Option[TravellerInformation] = TravelItinerary.travelers.find((p) => p.get_firstName == fname && p.get_lastName == lname)
            foundTrav.get.ticketNumber = eticketsTags.get.innerText().replaceAll(":", "").replaceAll("Document ", "")
          }
        }
      }
      ind += 1
    } while (eticketsTags.exists)
  }

  def extractTravelers {
    val pnrTag: Tag = spec.find("INPUT").`with`.id("pnrNbr")
    if (pnrTag.exists) {
      pnr = pnrTag.get.value
    }

    log.info("Getting paxe")
    // TABLE id=pax2 class=tablePassenger
    val passTABLE: Tag = spec.find("table")
    for (i <- 0 until passTABLE.all.length) {
      log.info("table id =" + passTABLE.at(i).get.id)
    }
    val paxe: Tag = spec.find("table").`with`.id("pax2")
    if (paxe.exists()) {
      log.info("Found Table Passenger")
      var names: Tag = paxe.find("span")
      if (names.exists()) {
        for (i <- 0 until names.all.length) {
          val name: String = names.at(i).get.innerText()
          log.info("Found name: " + name)
          val toks = name.split(" ")
          val ti = new TravellerInformation()
          if (toks.length > 2) {
            ti.firstName = toks(1)
            ti.lastName = toks(2)
          }
          else {
            ti.firstName = toks(0)
            ti.lastName = toks(1)
          }
          personName = ti.lastName + " " + ti.firstName
          TravelItinerary.travelers = TravelItinerary.travelers.::(ti)
        }
      }
    }
    // Get eTicktes Numbers

    extractETicketNumber

  }


  def getItineraryFilename(traveler: TravellerInformation) = {
    val firstFlight = TravelItinerary.flights(0)
    val eticketsDir: String = format("./Etickets/%d/%d/%d", firstFlight.get_departureDate().getYear(), firstFlight.get_departureDate().getMonthOfYear(), firstFlight.get_departureDate().getDayOfMonth())
    new File(eticketsDir).mkdirs
    val eticketsFile: String = format("%s/%s_%s_%s", eticketsDir, traveler.get_lastName + "_" + traveler.get_firstName, pnr, "eticket.xml")
    eticketsFile
  }

  def createETicketBeleg {
    val model: java.util.HashMap[String, Object] = new java.util.HashMap[String, Object]()
    val flightlistJ: java.util.List[FlightInformation] = new java.util.ArrayList[FlightInformation]()
    val travellistJ: java.util.List[TravellerInformation] = new java.util.ArrayList[TravellerInformation]()
    for (fs <- TravelItinerary.flights) {
      flightlistJ.add(fs)
    }
    for (fs <- TravelItinerary.travelers) {
      travellistJ.add(fs)
    }
    model.put("flights", flightlistJ)
    model.put("DateTool", new DateTool())
    //model = model.+(("travelers", TravelItinerary.travelers))
    val appCtx = new ClassPathXmlApplicationContext("application.xml");
    val velocityEngine = appCtx.getBean("velocityEngine").asInstanceOf[VelocityEngine]
    // notificationTemplate.vm must be in your classpath
    val encoding: String = getEncoding(templateFilekeys)
    val mapJ: java.util.Map[String, Object] = new java.util.HashMap[String, Object]()

    for (traveler <- TravelItinerary.travelers) {
      model.put("traveler", traveler)
      val itineraryFilename: String = getItineraryFilename(traveler)
      val fileWriter: Writer = new OutputStreamWriter(new FileOutputStream(itineraryFilename), "UTF-8")
      var result = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, templateFilekeys, "UTF-8", model);
      //result = result.replaceAll("0xC2"," ")
      log.info("Result:\n" + result)
      fileWriter.write(result);
      fileWriter.close()
      val props = new Properties()
      props.load(new FileInputStream("app.properties"))
      val winword: String = props.getProperty("winword")
      Runtime.getRuntime.exec(Array(winword, itineraryFilename))
    }
    // msg.setText(result);
  }

  def getDateTimeFromFlight(flight: FlightInformation, departure: Boolean): DateTime = {
    val defLocale = Locale.getDefault
    Locale.setDefault(Locale.ENGLISH)
    // val pattern = DateTimeFormat.patternForStyle("FS", Locale.ENGLISH)
    var s: String = ""
    if (departure)
      s = flight.departureDateString.trim() + " " + flight.departureTime.trim()
    else
      s = flight.arrivalDateString.trim() + " " + flight.arrivalTime.trim()
    val d = DateTimeFormat.forPattern("EEEE, MMMM dd, yyyy HH:mm").parseDateTime(s);
    Locale.setDefault(defLocale)
    d
  }

  def dump(tag: Tag): Unit = {
    if (!tag.exists() || tag.all.length() == 0)
      return
    for (i <- 0 until tag.all.length) {
      log.info("tag(" + i + ")=" + tag.at(i).get.innerHTML())
    }
  }

  def extractFlights(tag: Tag): Unit = {
    var flight: FlightInformation = null
    if (!tag.exists() || tag.all.length() == 0)
      return
    for (i <- 0 until tag.all.length) {
      val tagi = tag.at(i)
      if (tagi.get.innerHTML != null) {
        val mL: String = tagi.get.innerHTML
        //if (mL.startsWith("<") && mL.indexOf(" ") < 8)
        // matchTag(mL, extractFlights _, tagi)
        if (!mL.startsWith("<TABLE")) {
          log.info("tag(" + i + ").innerHTML=" + mL)
          val innerText = tagi.get.innerText
          if (innerText != null) {
            log.info("innerText=" + innerText)
            innerText match {
              case "Departure:" | "Departure: " | "Departure" => {
                flight = new FlightInformation
                // Departure
                flight.departureDateString = tag.at(i - 4).get.innerText
                flight.departureTime = tag.at(i + 1).get.innerText
                flight.departureDate = getDateTimeFromFlight(flight, departure = true)
                flight.departureAirportCity = tag.at(i + 2).get.innerText.split(",")(0)
                var formIdLetterCode: String = tag.at(i + 2).find("form").get.id
                flight.departureAirport3LetterCode = formIdLetterCode.substring(formIdLetterCode.length - 3, formIdLetterCode.length)
                flight.departureAirportName = tag.at(i + 2).find("a").get.innerText()

              }
              case "Arrival: " | "Arrival:" | "Arrival" => {
                // Arrival
                flight.arrivalTime = tag.at(i + 1).get.innerText
                flight.arrivalDate = flight.departureDate
                // Check if arrival is next day or previuos day
                val arrtimeHtml: String = tag.at(i + 1).get.innerHTML
                log.info("tag(" + (i + 1) + ").innerHTML=" + arrtimeHtml)
                if (arrtimeHtml.toLowerCase.contains("span")) {
                  val arrInfo: Tag = tag.at(i + 1).find.span()
                  if (arrInfo.exists()) {
                    flight.arrivalTime = flight.arrivalTime.trim().split(" ")(0)
                    val addArr: String = arrInfo.get.innerText
                    flight.arrivalTimeExtraInformation = addArr
                    addArr.substring(0, 3) match {
                      case "+1 " => {
                        var depDate = flight.departureDate
                        flight.arrivalDate = new DateTime(depDate).plusDays(1)
                      }

                      case "-1 " =>
                        var depDate = flight.departureDate
                        flight.arrivalDate = new DateTime(depDate).minusDays(1)
                      case x => flight.arrivalDate = flight.departureDate

                    }
                  }
                }

                flight.arrivalAirportCity = tag.at(i + 2).get.innerText.split(",")(0)
                val formIdLetterCode: String = tag.at(i + 2).find("form").get.id
                flight.arrivalAirport3LetterCode = formIdLetterCode.substring(formIdLetterCode.length - 3, formIdLetterCode.length)
                flight.arrivalAirportName = tag.at(i + 2).find("a").get.innerText()

                flight.airlineName = tag.at(i + 5).get.innerHTML().split("\\&nbsp\\;")(0)
                flight.flightNumber = tag.at(i + 5).get.innerHTML().split("\\&nbsp\\;")(1)
                flight.airline2LetterCode = flight.flightNumber.substring(0, 2)
                flight.duration = tag.at(i + 8).get.innerText()
                flight.extraInformation = tag.at(i + 18).get.innerText()
                flight.lastCheckIn = tag.at(i + 12).get.innerText()
                flight.fareType = tag.at(i + 16).get.innerText()
                val defLocale = Locale.getDefault
                Locale.setDefault(Locale.ENGLISH)
                flight.arrivalDateString = flight.arrivalDate.toString("EEEE, MMMM dd, yyyy")
                Locale.setDefault(defLocale)
                flight.arrivalDate = getDateTimeFromFlight(flight, departure = false)

                val tag1: Tag = tag.at(i + 10)
                log.info("tag(" + (i + 10) + ").innerHTML=" + tag1.get.innerHTML())
                if (tag1.get.innerHTML().toLowerCase().contains("<a ")) {
                  val aircraftTag: Tag = tag1.find("a")
                  if (aircraftTag.exists())
                    flight.airCraft = aircraftTag.get.innerText()
                }
              }
              case x => //log.info("tag(" + i + ").innerHTML=" + mL) // log.warn("Token not matched=" + x)
            }

            if (flight != null && flight.arrivalAirportCity != null && !TravelItinerary.flights.contains(flight)) {
              log.info(flight)
              flight.pnr = pnr
              TravelItinerary.flights = TravelItinerary.flights.::(flight)
            }
          }
        }
      }
    }
  }
}