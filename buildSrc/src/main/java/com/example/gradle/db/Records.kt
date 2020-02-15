/*
 * Copyright 2020 Shinya Mochida
 * 
 * Licensed under the Apache License,Version2.0(the"License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,software
 * Distributed under the License is distributed on an"AS IS"BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.gradle.db

import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

val localDateFormat: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

interface Records {
  fun data(dataFactory: DataFactory): Iterable<Data>
}

data class Scrum(val id: Long, val start: LocalDate, val end: LocalDate, val timeZone: ZoneId): Records {

  constructor(start: LocalDate, end: LocalDate, timeZone: ZoneId): this(start.atStartOfDay(timeZone).toEpochSecond(), start, end, timeZone)

  override fun data(dataFactory: DataFactory): Iterable<Data> = listOf(
      dataFactory.identifierLong(scrumId, id),
      dataFactory.string(startDay, start.format(localDateFormat)),
      dataFactory.string(endDay, end.format(localDateFormat)),
      dataFactory.string(tz, timeZone.id)
  )

  companion object: DataDefinitionProvider<Scrum> {
    const val scrumId = "scrum_id"
    const val startDay = "start_day"
    const val endDay = "end_day"
    const val tz = "time_zone"

    override fun tableName(prefix: String?): String =
        if (prefix == null) "scrum"
        else "${prefix}.scrum"

    override fun definition(): List<DataDefinition> {
      return listOf(
          DataDefinition.identifierHashableLong(scrumId),
          DataDefinition.string(startDay),
          DataDefinition.string(endDay),
          DataDefinition.string(tz)
      )
    }
  }
} 

data class StoryPoint(val sum: Int, val finished: Int, val date: LocalDate, val timeZone: ZoneId): Records {

  constructor(sum: Int, finished: Int, date: String, timeZone: ZoneId): this(sum, finished, LocalDate.parse(date, localDateFormat), timeZone)

  override fun data(dataFactory: DataFactory): Iterable<Data> =
      listOf(
          dataFactory.identifierString(d, date.format(localDateFormat)),
          dataFactory.int(s, sum),
          dataFactory.int(fin, finished),
          dataFactory.string(tz, timeZone.id)
      )

  companion object: DataDefinitionProvider<StoryPoint> {
    const val d = "date"
    const val s = "sum"
    const val fin = "finished"
    const val tz = "time_zone"

    override fun tableName(prefix: String?): String =
        if (prefix == null) "story_points"
        else "${prefix}.story_points"

    override fun definition(): List<DataDefinition> =
        listOf(
            DataDefinition.identifierHashableString(d),
            DataDefinition.int(s),
            DataDefinition.int(fin),
            DataDefinition.string(tz)
        )
  }
}
