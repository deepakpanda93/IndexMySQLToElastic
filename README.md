
# Migrate multiple tables from MySQL to Elastic

Access records from MySQL tables and index to Elastic


## Tech Stack

* Spark
* ElasticSearch
* MySQL
## Installation

Install MySQL on CentOS 7

Step 1: MySQL is no longer included with RHEL. You must download the repository from the MySQL site and install it directly. 
You can use the following commands to install MySQL. 

```bash
wget http://repo.mysql.com/mysql-community-release-el7-5.noarch.rpm

sudo rpm -ivh mysql-community-release-el7-5.noarch.rpm

sudo yum update

sudo yum install mysql-server

```

Step 2: Start and enable mysql service on boot

```bash
sudo systemctl start mysqld
sudo systemctl enable mysqld
```

Confirm that the service is running.

```bash
sudo systemctl status mysqld
```
## Usage/Examples

### Steps to perform:

1. Create a database on MySQL.

```bash
mysql> create database employeeDB;
Query OK, 1 row affected (0.00 sec)
```
2. Create 3 tables inside ***employeeDB*** database having some common fields in each of them.

```bash
mysql> CREATE TABLE employee1 (empno INT,ename VARCHAR(100),designation VARCHAR(100),manager INT,sal INT,deptno INT,age INT);
Query OK, 0 rows affected (0.01 sec)

mysql> CREATE TABLE employee2 (empno INT,ename VARCHAR(100),designation VARCHAR(100),manager INT,hire_date VARCHAR(50),sal INT,deptno INT);
Query OK, 0 rows affected (0.00 sec)

mysql> CREATE TABLE employee3 (empno INT,ename VARCHAR(100),designation VARCHAR(100),manager INT,sal INT,deptno INT);
Query OK, 0 rows affected (0.01 sec)
```

3. Insert records into the tables.

```bash
mysql> INSERT INTO employee1 (empno, ename, designation, manager, sal, deptno, age)
    -> VALUES (7369,'SMITH','CLERK',7902,800.00,20,29),
    -> (7499,'ALLEN','SALESMAN',7698,1600.00,30,33),
    -> (7521,'WARD','SALESMAN',7698,1250.00,30,37),
    -> (7566,'JONES','MANAGER',7839,2975.00,20,41),
    -> (7654,'MARTIN','SALESMAN',7698,1250.00,30,46);
Query OK, 5 rows affected (0.00 sec)
Records: 5  Duplicates: 0  Warnings: 0
```

```bash
mysql> INSERT INTO employee2 (empno, ename, designation, manager, hire_date, sal, deptno)
    -> VALUES (6369,'ANDREW','CLERK',7902,'1980-12-17',800.00,20),
    -> (6498,'CLARK','SALESMAN',7698,'1981-02-20',1600.00,30),
    -> (6522,'MATHEW','SALESMAN',7698,'1981-02-22',1250.00,30),
    -> (6565,'ADAM','MANAGER',7839,'1981-04-02',2975.00,20),
    -> (6653,'JOHN','SALESMAN',7698,'1981-09-28',1250.00,30);
Query OK, 5 rows affected (0.00 sec)
Records: 5  Duplicates: 0  Warnings: 0
```

```bash
mysql> INSERT INTO employee3 (empno, ename, designation, manager, sal, deptno)
    -> VALUES (5569,'BOB','CLERK',7902,800.00,20),
    -> (5499,'LIANG','SALESMAN',7698,1600.00,30),
    -> (5621,'KEN','SALESMAN',7698,1250.00,30),
    -> (5565,'ZHOU','MANAGER',7839,2975.00,20),
    -> (5554,'ZACH','SALESMAN',7698,1250.00,30);
Query OK, 5 rows affected (0.00 sec)
Records: 5  Duplicates: 0  Warnings: 0
```

4. Display the records from each of the tables.

```bash
mysql> select * from employee1;
+-------+--------+-------------+---------+------+--------+------+
| empno | ename  | designation | manager | sal  | deptno | age  |
+-------+--------+-------------+---------+------+--------+------+
|  7369 | SMITH  | CLERK       |    7902 |  800 |     20 |   29 |
|  7499 | ALLEN  | SALESMAN    |    7698 | 1600 |     30 |   33 |
|  7521 | WARD   | SALESMAN    |    7698 | 1250 |     30 |   37 |
|  7566 | JONES  | MANAGER     |    7839 | 2975 |     20 |   41 |
|  7654 | MARTIN | SALESMAN    |    7698 | 1250 |     30 |   46 |
+-------+--------+-------------+---------+------+--------+------+
5 rows in set (0.00 sec)
```

![Employee1](https://github.com/deepakpanda93/IndexMySQLToElastic/blob/master/images/employee1.png?raw=true)

```bash
mysql> select * from employee2;
+-------+--------+-------------+---------+------------+------+--------+
| empno | ename  | designation | manager | hire_date  | sal  | deptno |
+-------+--------+-------------+---------+------------+------+--------+
|  6369 | ANDREW | CLERK       |    7902 | 1980-12-17 |  800 |     20 |
|  6498 | CLARK  | SALESMAN    |    7698 | 1981-02-20 | 1600 |     30 |
|  6522 | MATHEW | SALESMAN    |    7698 | 1981-02-22 | 1250 |     30 |
|  6565 | ADAM   | MANAGER     |    7839 | 1981-04-02 | 2975 |     20 |
|  6653 | JOHN   | SALESMAN    |    7698 | 1981-09-28 | 1250 |     30 |
+-------+--------+-------------+---------+------------+------+--------+
5 rows in set (0.00 sec)
```

![Employee2](https://github.com/deepakpanda93/IndexMySQLToElastic/blob/master/images/employee2.png?raw=true)

```bash
mysql> select * from employee3;
+-------+-------+-------------+---------+------+--------+
| empno | ename | designation | manager | sal  | deptno |
+-------+-------+-------------+---------+------+--------+
|  5569 | BOB   | CLERK       |    7902 |  800 |     20 |
|  5499 | LIANG | SALESMAN    |    7698 | 1600 |     30 |
|  5621 | KEN   | SALESMAN    |    7698 | 1250 |     30 |
|  5565 | ZHOU  | MANAGER     |    7839 | 2975 |     20 |
|  5554 | ZACH  | SALESMAN    |    7698 | 1250 |     30 |
+-------+-------+-------------+---------+------+--------+
5 rows in set (0.00 sec)
```

![Employee3](https://github.com/deepakpanda93/IndexMySQLToElastic/blob/master/images/employee3.png?raw=true)

5. Create a Spark Code file to read data from MySQL tables and Index into ElasticSearch.

```bash
$ cat MySQLToElastic.scala

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
```

6. Create a connection.conf file to connect to MySQL:

```bash
$ cat connection.conf

mysql =
  {
    url = "jdbc:mysql://<MYSQL_HOST>:3306/employeeDB"
    driver= "com.mysql.jdbc.Driver"
    user= "root"
    password= ""
  }
```

7. Create a tables.conf file to read the required tables from MySQL DB:

```bash
$ cat tables.conf

tables = {
  simple =
    [
      {
        tableName = "employee1"
      },
      {
        tableName = "employee2"
      },
      {
        tableName = "employee3"
      }
  ]
}
```

8. Prepare a pom.xml file for your project

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>org.example</groupId>
    <artifactId>MySQL_Elastic</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.build.targetEncoding>UTF-8</project.build.targetEncoding>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>

        <maven.compiler.plugin.version>3.8.1</maven.compiler.plugin.version>
        <maven-shade-plugin.version>3.2.3</maven-shade-plugin.version>
        <scala-maven-plugin.version>3.2.2</scala-maven-plugin.version>
        <scalatest-maven-plugin.version>2.0.0</scalatest-maven-plugin.version>

        <scala.version>2.11.8</scala.version>
        <scala.binary.version>2.11</scala.binary.version>
        <spark.version>2.4.7</spark.version>
        <jackson.version>2.11.0</jackson.version>
        <spark.scope>provided</spark.scope>
    </properties>

    <!-- Developers -->
    <developers>
        <developer>
            <id>deepakpanda93</id>
            <name>Deepak Panda</name>
            <email>deepakpanda93@gmail.com</email>
            <url>https://github.com/deepakpanda93</url>
        </developer>
    </developers>

    <!-- Repositories -->
    <repositories>
        <repository>
            <id>central</id>
            <name>Maven Central</name>
            <url>https://repo1.maven.org/maven2</url>
        </repository>

        <repository>
            <id>cldr-repo</id>
            <name>Cloudera Public Repo</name>
            <url>https://repository.cloudera.com/artifactory/cloudera-repos/</url>
        </repository>

        <repository>
            <id>hdp-repo</id>
            <name>Hortonworks Public Repo</name>
            <url>https://repo.hortonworks.com/content/repositories/releases/</url>
        </repository>
    </repositories>

    <dependencies>
        <dependency>
            <groupId>org.scala-lang</groupId>
            <artifactId>scala-library</artifactId>
            <version>${scala.version}</version>
        </dependency>
        <dependency>
            <groupId>org.apache.spark</groupId>
            <artifactId>spark-core_2.11</artifactId>
            <version>${spark.version}</version>
        </dependency>
        <dependency>
            <groupId>org.apache.spark</groupId>
            <artifactId>spark-sql_2.11</artifactId>
            <version>${spark.version}</version>
        </dependency>
        <!-- https://mvnrepository.com/artifact/mysql/mysql-connector-java -->
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <version>5.1.30</version>
        </dependency>
        <dependency>
            <groupId>com.typesafe</groupId>
            <artifactId>config</artifactId>
            <version>1.4.0</version>
        </dependency>
        <dependency>
            <groupId>org.elasticsearch</groupId>
            <artifactId>elasticsearch-spark-20_2.11</artifactId>
            <version>7.10.0</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>

            <!-- Maven Compiler Plugin -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>${maven.compiler.plugin.version}</version>
                <configuration>
                    <source>${java.version}</source>
                    <target>${java.version}</target>
                    <compilerArgs>
                        <arg>-Xlint:all,-serial,-path</arg>
                    </compilerArgs>
                </configuration>
            </plugin>

            <!-- Scala Maven Plugin -->
            <plugin>
                <groupId>net.alchim31.maven</groupId>
                <artifactId>scala-maven-plugin</artifactId>
                <version>${scala-maven-plugin.version}</version>
                <executions>
                    <execution>
                        <goals>
                            <goal>compile</goal>
                            <goal>testCompile</goal>
                        </goals>
                    </execution>
                </executions>
                <configuration>
                    <scalaVersion>${scala.version}</scalaVersion>
                    <scalaCompatVersion>${scala.binary.version}</scalaCompatVersion>
                    <checkMultipleScalaVersions>true</checkMultipleScalaVersions>
                    <failOnMultipleScalaVersions>true</failOnMultipleScalaVersions>
                    <recompileMode>incremental</recompileMode>
                    <charset>${project.build.sourceEncoding}</charset>
                    <args>
                        <arg>-unchecked</arg>
                        <arg>-deprecation</arg>
                        <arg>-feature</arg>
                    </args>
                    <jvmArgs>
                        <jvmArg>-Xss64m</jvmArg>
                        <jvmArg>-Xms1024m</jvmArg>
                        <jvmArg>-Xmx1024m</jvmArg>
                        <jvmArg>-XX:ReservedCodeCacheSize=1g</jvmArg>
                    </jvmArgs>
                    <javacArgs>
                        <javacArg>-source</javacArg>
                        <javacArg>${java.version}</javacArg>
                        <javacArg>-target</javacArg>
                        <javacArg>${java.version}</javacArg>
                        <javacArg>-Xlint:all,-serial,-path</javacArg>
                    </javacArgs>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```



## Run Locally

Run the Spark job from IDE

```bash
I used InteliJ to run the project. But one can build the project, deploy the JAR on the cluster and execute using spark-submit
```




## Screenshots

### Visualize records on Kibana

![employee_index](https://github.com/deepakpanda93/IndexMySQLToElastic/blob/master/images/Kibana_Employee_Index.png?raw=true)

### Expand a single record to check the fields

![employee_index](https://github.com/deepakpanda93/IndexMySQLToElastic/blob/master/images/Kibana_Employee_Expand_Doc.png?raw=true)

### Records in each table while processing in Spark
***Employee1***
![employee1_spark](https://github.com/deepakpanda93/IndexMySQLToElastic/blob/master/images/employee1_Spark.png?raw=true)

***Employee2***
![employee2_spark](https://github.com/deepakpanda93/IndexMySQLToElastic/blob/master/images/employee2_Spark.png?raw=true)

***Employee3***
![employee3_spark](https://github.com/deepakpanda93/IndexMySQLToElastic/blob/master/images/employee3_Spark.png?raw=true)

## 🚀 About Me

## Hi, I'm Deepak! 👋

I'm a Big Data Engineer...




## 🔗 Links
[![portfolio](https://img.shields.io/badge/my_portfolio-000?style=for-the-badge&logo=ko-fi&logoColor=white)](https://deepakpanda93.gitbook.io/integrated-spark)
[![linkedin](https://img.shields.io/badge/linkedin-0A66C2?style=for-the-badge&logo=linkedin&logoColor=white)](https://www.linkedin.com/in/deepakpanda93)
[!["Buy Me A Coffee"](https://www.buymeacoffee.com/assets/img/custom_images/orange_img.png)](https://www.buymeacoffee.com/deepakpanda93)
