package scripts
import bleep.internal.FileUtils
import bleep.{Commands, Started}

object GeneratedRowParsers extends bleep.BleepCodegenScript("GeneratedRowParsers") {
  val N = 100

  override def run(started: Started, commands: Commands, targets: List[GeneratedRowParsers.Target], args: List[String]): Unit = {
    val functions = 1
      .until(N)
      .map { n =>
        s"""|    @FunctionalInterface
            |    interface Function$n<${0.until(n).map(nn => s"T$nn").mkString(", ")}, R> {
            |        R apply(${0.until(n).map(nn => s"T$nn t$nn").mkString(", ")});
            |    }""".stripMargin
      }

    val constructorMethods = 1
      .until(N)
      .map { n =>
        val range = 0.until(n)
        val tparams = range.map(nn => s"T$nn").mkString(", ")
        val params = range.map(nn => s"PgType<T$nn> t$nn").mkString(", ")
        val decodeFunction = s"Function$n<$tparams, Row>"
        val decodeParams = range.map(nn => s"(T$nn) a[$nn]").mkString(", ")
        val tuple = s"Tuple$n<$tparams>"
        val encodeFunction = s"Function1<Row, $tuple>"
        s"""|    @SuppressWarnings("unchecked")
            |    static <$tparams, Row> RowParser<Row> of($params, $decodeFunction decode, $encodeFunction encode) {
            |        return new RowParser<>(unmodifiableList(asList(${range.map(nn => s"t$nn").mkString(", ")})), a -> decode.apply($decodeParams), row -> encode.apply(row).toArray());
            |    }""".stripMargin
      }

    val tuples = 1
      .until(N)
      .map { n =>
        val range = 0.until(n)
        s"""    record Tuple$n<${range.map(nn => s"T$nn").mkString(", ")}>(${range.map(nn => s"T$nn t$nn").mkString(", ")}) implements Tuple {
           |        public Object[] toArray() {
           |            return new Object[]{${range.map(nn => s"t$nn").mkString(", ")}};
           |        }
           |    }""".stripMargin
      }
    val contents =
      s"""|package typo.runtime;
          |
          |import static java.util.Arrays.asList;
          |import static java.util.Collections.unmodifiableList;
          |
          |public interface RowParsers {
          |${constructorMethods.mkString("\n\n")}
          |${functions.mkString("\n\n")}
          |    interface Tuple {
          |        Object[] toArray();
          |    }
          |${tuples.mkString("\n\n")}
          |}""".stripMargin

    targets.foreach { target =>
      FileUtils.writeString(started.logger, Some("writing"), target.sources.resolve("typo/runtime/RowParsers.java"), contents)
    }
  }
}
