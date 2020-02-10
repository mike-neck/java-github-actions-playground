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
package com.example.gradle

import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.auth.profile.internal.BasicProfile
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import org.gradle.api.provider.Property

interface AwsDynamoDbClientConfig {
  fun configure(builder: AmazonDynamoDBClientBuilder): AmazonDynamoDBClientBuilder
}

class CredentialConfig(
    private val awsKey: Property<String>,
    private val awsSecret: Property<String>,
    private val awsProfile: Property<String>
) : AwsDynamoDbClientConfig {

  override fun configure(builder: AmazonDynamoDBClientBuilder): AmazonDynamoDBClientBuilder =
      builder.withCredentials(awsCredentialsProvider())

  private fun awsCredentialsProvider(): AWSCredentialsProvider =
      takeFirst(
          { staticCredentialsProviderFrom(awsKey, awsSecret) },
          { awsProfile.map<AWSCredentialsProvider>(::ProfileCredentialsProvider).orNull }
      ).withDefault(DefaultAWSCredentialsProviderChain())

  companion object {
    fun staticCredentialsProviderFrom(awsKey: Property<String>, awsSecret: Property<String>): AWSCredentialsProvider? =
        if (awsKey.isPresent && awsSecret.isPresent) AWSStaticCredentialsProvider(BasicAWSCredentials(awsKey.get(), awsSecret.get()))
        else null

    fun <T : Any> takeFirst(vararg items: () -> T?): WithDefault<T> = firstOf(0, items.size, listOf(*items))

    private tailrec fun <T : Any> firstOf(index: Int, size: Int, list: List<() -> T?>): WithDefault<T> =
        if (index == size) object : WithDefault<T> {
          override fun withDefault(item: T): T = item
        }
        else when (val fromList = list[index]()) {
          fromList != null -> object : WithDefault<T> {
            override fun withDefault(item: T): T = fromList
          }
          else -> firstOf(index + 1, size, list)
        }
  }

  interface WithDefault<T : Any> {
    fun withDefault(item: T): T
  }
}

class EndpointConfig(private val endpoint: Property<String>) : AwsDynamoDbClientConfig {

  override fun configure(builder: AmazonDynamoDBClientBuilder): AmazonDynamoDBClientBuilder {
    val url = endpoint.orNull
    if (url != null) {
      return builder.withEndpointConfiguration(AwsClientBuilder.EndpointConfiguration(url, null))
    }
    return builder
  }
}
