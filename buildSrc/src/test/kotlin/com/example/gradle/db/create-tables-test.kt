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

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.example.gradle.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField

class DynamoDbCreateTableTest {

  lateinit var client: AmazonDynamoDB

  @BeforeEach
  fun connect() {
    this.client = client()
  }

  @TestFactory
  fun scrumTable(): Iterable<DynamicTest> {
    val now = LocalDateTime.now()
    val creator = DynamoDbCreateTable(client, now.format(prefixFormatter))
    val tableName = creator.createTable(Scrum)
    return listOf(
        "作成された scrum テーブルの prefix(${now.format(prefixFormatter)}) が一致する" { tableName shouldStartWith now.format(prefixFormatter) },
        "作成されたテーブルの名前に scrum が含まれる" { tableName shouldContain Scrum.tableName("") }
    )
  }

  @TestFactory
  fun storyPointsTable(): Iterable<DynamicTest> {
    val now = LocalDateTime.now()
    val creator = DynamoDbCreateTable(client, now.format(prefixFormatter))
    val tableName = creator.createTable(StoryPoint)
    return listOf(
        "作成された storyPoints テーブルの prefix(${now.format(prefixFormatter)}) が一致する" { tableName shouldStartWith now.format(prefixFormatter) },
        "作成されたテーブルの名前に story_points が含まれる" { tableName shouldContain StoryPoint.tableName("") }
    )
  }

  companion object {
    val prefixFormatter: DateTimeFormatter =
        DateTimeFormatterBuilder()
            .appendLiteral("test.table")
            .appendValue(ChronoField.YEAR, 4)
            .appendLiteral(".")
            .appendValue(ChronoField.MONTH_OF_YEAR, 2)
            .appendLiteral(".")
            .appendValue(ChronoField.DAY_OF_MONTH, 2)
            .appendLiteral(".")
            .appendValue(ChronoField.HOUR_OF_DAY, 2)
            .appendLiteral(".")
            .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
            .appendLiteral(".")
            .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
            .optionalStart()
            .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
            .toFormatter()

    fun client(): AmazonDynamoDB =
        AmazonDynamoDBClientBuilder.standard()
            .withEndpointConfiguration(AwsClientBuilder.EndpointConfiguration("http://localhost:8000", "ap-northeast-1"))
            .withCredentials(DefaultAWSCredentialsProviderChain())
            .build()


  }
}
