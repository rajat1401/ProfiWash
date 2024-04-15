package utils

import com.typesafe.config.{Config, ConfigFactory}
import play.api.Logging

class KafkaLoggerUtil(config: Config) extends Logging  {
//  private val producer =

}
object KafkaLoggerUtil {
  def apply(): KafkaLoggerUtil = {
    val config = ConfigFactory.load().getConfig("kafka")
    new KafkaLoggerUtil(config)
  }

}
