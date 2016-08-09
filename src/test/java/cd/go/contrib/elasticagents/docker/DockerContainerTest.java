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

package cd.go.contrib.elasticagents.docker;

import cd.go.contrib.elasticagents.docker.requests.CreateAgentRequest;
import com.spotify.docker.client.exceptions.ImageNotFoundException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Collections;
import java.util.HashMap;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DockerContainerTest extends BaseTest {

    private CreateAgentRequest request;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        HashMap<String, String> properties = new HashMap<>();
        properties.put("Image", "gocdcontrib/ubuntu-docker-elastic-agent");
        request = new CreateAgentRequest("key", properties, "production");
    }

    @Test
    public void shouldCreateContainer() throws Exception {
        DockerContainer container = DockerContainer.create(request, createSettings(), docker);
        containers.add(container.name());
        assertContainerExist(container.name());
    }

    @Test
    public void shouldPullAnImageWhenOneDoesNotExist() throws Exception {
        String imageName = "busybox:latest";

        try {
            docker.removeImage(imageName);
        } catch (ImageNotFoundException ignore) {
        }
        DockerContainer container = DockerContainer.create(new CreateAgentRequest("key", Collections.singletonMap("Image", imageName), "prod"), createSettings(), docker);
        containers.add(container.name());

        assertNotNull(docker.inspectImage(imageName));
        assertContainerExist(container.name());
    }

    @Test
    public void shouldRaiseExceptionWhenImageIsNotFoundInDockerRegistry() throws Exception {
        String imageName = "ubuntu:does-not-exist";
        thrown.expect(ImageNotFoundException.class);
        thrown.expectMessage(containsString("Image not found: " + imageName));
        DockerContainer.create(new CreateAgentRequest("key", Collections.singletonMap("Image", imageName), "prod"), createSettings(), docker);
    }

    @Test
    public void shouldNotCreateContainerIfTheImageIsNotProvided() throws Exception {
        CreateAgentRequest request = new CreateAgentRequest("key", new HashMap<String, String>(), "production");

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Must provide `Image` attribute.");

        DockerContainer.create(request, createSettings(), docker);
    }

    @Test
    public void shouldTerminateAnExistingContainer() throws Exception {
        DockerContainer container = DockerContainer.create(request, createSettings(), docker);
        containers.add(container.name());

        container.terminate(docker);

        assertContainerDoesNotExist(container.name());
    }

    @Test
    public void shouldFindAnExistingContainer() throws Exception {
        DockerContainer container = DockerContainer.create(request, createSettings(), docker);
        containers.add(container.name());

        DockerContainer dockerContainer = DockerContainer.find(docker, container.name());

        assertEquals(container, dockerContainer);
    }
}
