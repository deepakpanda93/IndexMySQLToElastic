import org.apache.spark.{SparkConf, SparkFiles}
import org.apache.spark.sql.SparkSession
import org.slf4j.LoggerFactory
import java.io.File
import com.typesafe.config.ConfigFactory

object MySQLToElastic {

  def main(args: Array[String]): Unit = {

    val logger = LoggerFactory.getLogger("MySQLToElasticMigration")
    val sparkConf = new SparkConf().setMaster("local[*]")
    val spark = SparkSession.builder().config(sparkConf).getOrCreate()

    val connConfigFile = "//Users//dpanda//IdeaProjects//MySQL_Elastic//src//main//resources//connection.conf"
    val tableConfigFile = "//Users//dpanda//IdeaProjects//MySQL_Elastic//src//main//resources//tables.conf"

    val connectionConfig = ConfigFactory.parseFile(new File(connConfigFile)).resolve()
    val tableConfig = ConfigFactory.parseFile(new File(tableConfigFile)).resolve()

    val table_List = tableConfig.getConfigList("tables.simple")
    val hostport = connectionConfig.getString("mysql.url")
    val driverName = connectionConfig.getString("mysql.driver")
    val userName = connectionConfig.getString("mysql.user")
    val password = connectionConfig.getString("mysql.password")


    for (i <- 0 to table_List.size() - 1) {
      val table_name = table_List.get(i).getString("tableName")

      try {
        logger.info(s"starting reading table ${table_name} ")
        val sourceDf = spark.read.format("jdbc")
          .option("driver", driverName)
          .option("url", hostport)
          .option("dbtable", table_name)
          .option("user", userName)
          .option("password", password)
          .load()

        logger.info(s"Count of rows in table ${table_name} is ${sourceDf.count()}")
        logger.info(s"Schema of ${table_name} is ${sourceDf.schema}")
        val resultDF = sourceDf.select("empno","ename","designation","deptno")
        // Read data from Dataframe
        resultDF.show()

        // Insert records into Elastic Index
        resultDF.write
          .format("org.elasticsearch.spark.sql")
          .option("es.port", "9200") // ElasticSearch port
          .option("es.nodes", "ElasticSearch host") // ElasticSearch host
          .mode("append")
          .save("employee/doc") // indexname/document type

      } catch {
        case e: Throwable => println(s"Connectivity Failed for Table ${table_name} ", e)
      }
    }
  }
}
