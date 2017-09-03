/*
 * Copyright 2016 ThoughtWorks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cd.go.contrib.elasticagents.docker.executors;

import cd.go.contrib.elasticagents.docker.requests.ValidatePluginSettings;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ValidateConfigurationExecutorTest {
    @Test
    public void shouldValidateABadConfiguration() throws Exception {
        ValidatePluginSettings settings = new ValidatePluginSettings();
        GoPluginApiResponse response = new ValidateConfigurationExecutor(settings).execute();

        assertThat(response.responseCode(), is(200));
        JSONAssert.assertEquals("[\n" +
                "  {\n" +
                "    \"message\": \"Go Server URL must not be blank.\",\n" +
                "    \"key\": \"go_server_url\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"message\": \"Maximum containers to allow must be a positive integer.\",\n" +
                "    \"key\": \"max_docker_containers\"\n" +
                "  }\n," +
                "  {\n" +
                "    \"message\": \"Docker URI must not be blank.\",\n" +
                "    \"key\": \"docker_uri\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"message\": \"Agent auto-register Timeout (in minutes) must be a positive integer.\",\n" +
                "    \"key\": \"auto_register_timeout\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"message\": \"Use Private Registry must not be blank.\",\n" +
                "    \"key\": \"enable_private_registry_authentication\"\n" +
                "  }\n" +
                "]\n", response.responseBody(), true);
    }

    @Test
    public void shouldValidateAGoodConfiguration() throws Exception {
        ValidatePluginSettings settings = new ValidatePluginSettings();
        settings.put("max_docker_containers", "1");
        settings.put("docker_uri", "https://api.example.com");
        settings.put("docker_ca_cert", "some ca cert");
        settings.put("docker_client_key", "some client key");
        settings.put("docker_client_cert", "sone client cert");
        settings.put("go_server_url", "https://ci.example.com");
        settings.put("enable_private_registry_authentication", "false");
        settings.put("auto_register_timeout", "10");

        GoPluginApiResponse response = new ValidateConfigurationExecutor(settings).execute();

        assertThat(response.responseCode(), is(200));
        JSONAssert.assertEquals("[]", response.responseBody(), true);
    }


    @Test
    public void shouldValidateAConfigurationWithAllPrivateRegistryInfos() throws Exception {
        ValidatePluginSettings settings = new ValidatePluginSettings();
        settings.put("max_docker_containers", "1");
        settings.put("docker_uri", "https://api.example.com");
        settings.put("docker_ca_cert", "some ca cert");
        settings.put("docker_client_key", "some client key");
        settings.put("docker_client_cert", "sone client cert");
        settings.put("go_server_url", "https://ci.example.com");
        settings.put("enable_private_registry_authentication", "true");
        settings.put("private_registry_server", "server");
        settings.put("private_registry_username", "username");
        settings.put("private_registry_password", "password");
        settings.put("auto_register_timeout", "10");
        GoPluginApiResponse response = new ValidateConfigurationExecutor(settings).execute();

        assertThat(response.responseCode(), is(200));
        JSONAssert.assertEquals("[]", response.responseBody(), true);
    }

    @Test
    public void shouldNotValidateAConfigurationWithInvalidPrivateRegistrySettings() throws Exception {
        ValidatePluginSettings settings = new ValidatePluginSettings();
        settings.put("max_docker_containers", "1");
        settings.put("docker_uri", "https://api.example.com");
        settings.put("docker_ca_cert", "some ca cert");
        settings.put("docker_client_key", "some client key");
        settings.put("docker_client_cert", "sone client cert");
        settings.put("go_server_url", "https://ci.example.com");
        settings.put("enable_private_registry_authentication", "true");
        settings.put("private_registry_server", "");
        settings.put("private_registry_username", "");
        settings.put("private_registry_password", "");
        settings.put("auto_register_timeout", "10");
        GoPluginApiResponse response = new ValidateConfigurationExecutor(settings).execute();

        String expectedString = "[{\"message\":\"Private Registry Server must not be blank.\",\"key\":\"private_registry_server\"}," +
                "{\"message\":\"Private Registry Username must not be blank.\",\"key\":\"private_registry_username\"}," +
                "{\"message\":\"Private Registry Password must not be blank.\",\"key\":\"private_registry_password\"}]";
        assertThat(response.responseCode(), is(200));
        JSONAssert.assertEquals(expectedString, response.responseBody(), true);
    }
}
