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

import org.gradle.api.Project
import org.gradle.api.provider.Property

interface AwsConfig {
  var awsAccessKey: String?
  var awsSecretKey: String?
  var profile: String?
  var endpoint: String?
  var tableNamePrefix: String?
}

data class Aws(
    internal val _awsAccessKey: Property<String>,
    internal val _awsSecretKey: Property<String>,
    internal val _profile: Property<String>,
    internal val _endpoint: Property<String>,
    internal val _tableNamePrefix: Property<String> 
): AwsConfig {

  constructor(project: Project):
      this(
          project.prop(),
          project.prop(),
          project.prop(),
          project.prop(),
          project.prop()
      )

  override var awsAccessKey: String?
    get() = _awsAccessKey.orNull
    set(value) { _awsAccessKey.set(value) }

  override var awsSecretKey: String?
    get() = _awsSecretKey.orNull
    set(value) { _awsSecretKey.set(value) }

  override var profile: String?
    get() = _profile.orNull
    set(value) { _profile.set(value) }

  override var endpoint: String?
    get() = _endpoint.orNull
    set(value) { _endpoint.set(value) }

  override var tableNamePrefix: String?
    get() = _tableNamePrefix.orNull
    set(value) { _tableNamePrefix.set(value) }
}
