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
import com.example.gradle.Either
import com.example.gradle.invoke
import com.example.gradle.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import java.time.LocalDateTime
import java.time.ZoneId

class DynamoDbInsertTest {

  lateinit var dynamoDbInsert: DynamoDbInsert
  lateinit var tableName: String

  @BeforeEach
  fun connect() {
    val client = client()
    val now = LocalDateTime.now()
    val prefix = now.format(DynamoDbCreateTableTest.prefixFormatter)
    val creator = DynamoDbCreateTable(client, prefix)
    this.tableName = creator.createTable(StoryPoint)
    this.dynamoDbInsert = DynamoDbInsert(client, prefix)
  }

  private val storyPoint: StoryPoint = StoryPoint(30, 15, "2020-02-20", ZoneId.of("Asia/Tokyo"))

  @TestFactory
  fun insert(): Iterable<DynamicTest> = listOf(
      "作成できる" { dynamoDbInsert.insert(storyPoint, StoryPoint) shouldBe Either.right<String, StoryPoint>(storyPoint) }
  )

  companion object {
    private val dynamoDbHost = System.getenv("DYNAMO_DB").let { if (it.isNullOrBlank()) "localhost" else it }

    fun client(): AmazonDynamoDB =
        AmazonDynamoDBClientBuilder.standard()
            .withEndpointConfiguration(AwsClientBuilder.EndpointConfiguration("http://$dynamoDbHost:8000", "ap-northeast-1"))
            .withCredentials(DefaultAWSCredentialsProviderChain())
            .build()
  }
}

