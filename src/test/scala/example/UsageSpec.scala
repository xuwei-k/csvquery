package example

import org.scalatest._
import csvquery._
import scalikejdbc._
import org.joda.time._
import skinny.logging.Logging
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class UsageSpec extends AnyFunSpec with Matchers with Logging {

  // http://support.spatialkey.com/spatialkey-sample-csv-data/
  val filepath = "src/test/resources/SacramentocrimeJanuary2006.csv"
  val headers = Seq("cdatetime", "address", "district", "beat", "grid", "crimedescr", "ucr_ncic_code", "latitude", "longitude")

  describe("CSVQuery") {

    it("counts csv records") {
      val count = withSession { implicit session =>
        withCSV(CSV(filepath, headers)) { table =>
          sql"select count(1) from $table".map(_.long(1)).single.apply()
        }
      }
      count should equal(Some(7584))
    }

    it("filters csv records") {
      implicit val session = autoCSVSession
      val records: Seq[Map[String, Any]] = withCSV(CSV(filepath, headers)) { table =>
        val (from, to) = (38.45, 38.50)
        sql"select cdatetime, address, latitude from $table where latitude >= $from and latitude <= $to"
          .toMap.list.apply()
      }
      logger.info("records: " + records.take(5))
      records.size should equal(1258)
    }

    case class Account(name: String, companyName: String, company: Option[Company])
    case class Company(name: String, url: String)

    it("runs join queries") {
      implicit val session = autoCSVSession
      val (accountsCsv, companiesCsv) = (
        CSV("src/test/resources/accounts.csv", Seq("name", "company_name")),
        CSV("src/test/resources/companies.csv", Seq("name", "url")))
      val accounts = withCSV(accountsCsv, companiesCsv) { (a, c) =>
        sql"select a.name, a.company_name, c.url from $a a left join $c c on a.company_name = c.name".map { rs =>
          new Account(
            name = rs.get("name"),
            companyName = rs.get("company_name"),
            company = rs.stringOpt("url").map(url => Company(rs.get("company_name"), url)))
        }.list.apply()
      }
      logger.info("records: " + accounts)
      accounts.size should equal(10)
    }

    it("fails to update csv records") {
      implicit val session = autoCSVSession
      intercept[org.h2.jdbc.JdbcSQLSyntaxErrorException] {
        withCSV(CSV(filepath, headers)) { table => sql"delete from $table".update.apply() }
      }
    }

  }

  case class CrimeRecord(
    cdatetime: String,
    address: String,
    district: Int,
    beat: String,
    grid: Int,
    crimedescr: String,
    ucrNcicCode: Int,
    latitude: Double,
    longitude: Double)

  object CrimeRecordDAO extends SkinnyCSVMapper[CrimeRecord] {
    def csv = CSV(filepath, headers)
    override def extract(rs: WrappedResultSet, n: ResultName[CrimeRecord]) = autoConstruct(rs, n)
  }

  describe("SkinnyCSVMapper") {
    it("runs queries") {
      val records: Seq[CrimeRecord] = withSession { implicit s =>
        CrimeRecordDAO.where(Symbol("crimedescr") -> "459 PC  BURGLARY BUSINESS").apply()
      }
      logger.info("resuls: " + records.take(5))
      records.size should equal(135)
    }
  }

}
