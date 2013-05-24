package com.testprioritization.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = "classpath:config/test-config.xml")
@ActiveProfiles("dev")
public class TPControllerFunctionalTest {
	private static final String testDataDir = "src/test/resources/test-data/";
	@Autowired
	private WebApplicationContext context;

	private MockMvc mockMvc;

	@Before
	public void setup() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
	}

	@Test
	public void shouldSaveChanges() throws Exception {
		String project = "some_project";
		MockMultipartHttpServletRequestBuilder mockRequestBuilder = fileUpload("/");
		mockRequestBuilder.file("diff", FileUtils.readFileToByteArray(new File(
				testDataDir + "diff.txt")));
		mockRequestBuilder.param("project", project);
		mockRequestBuilder.contentType(MediaType.MULTIPART_FORM_DATA);
		mockMvc.perform(mockRequestBuilder).andExpect(status().isCreated())
				.andExpect(header().string("Location", "/" + project));
	}

	@Test
	public void shouldPrioritizeTests() throws Exception {
		mockMvc.perform(get("/project2").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	public void shouldProcessTestResults() throws Exception {
		MockMultipartHttpServletRequestBuilder mockRequestBuilder = fileUpload("/project1");
		mockRequestBuilder.file("test-report", FileUtils
				.readFileToByteArray(new File(testDataDir + "TAP.txt")));
		mockRequestBuilder.contentType(MediaType.MULTIPART_FORM_DATA);
		mockMvc.perform(mockRequestBuilder).andExpect(status().isCreated());

	}

	@Test
	public void shouldReturnBadRequestWhenProcessTestTesultsTwice()
			throws Exception {
		MockMultipartHttpServletRequestBuilder mockRequestBuilder = fileUpload("/project2");
		mockRequestBuilder.file("test-report", FileUtils
				.readFileToByteArray(new File(testDataDir + "TAP.txt")));
		mockRequestBuilder.contentType(MediaType.MULTIPART_FORM_DATA);
		mockMvc.perform(mockRequestBuilder).andExpect(status().isBadRequest());
	}

	@Test
	public void shouldReturnNotFoundWhenPrioritizationTestsInNonExistingProject()
			throws Exception {
		mockMvc.perform(
				get("/nonexisting_project").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound());
	}

	@Test
	public void shouldNotAllowPostingToEmptyProject() throws Exception {
		mockMvc.perform(get("/").accept(MediaType.APPLICATION_JSON)).andExpect(
				status().isMethodNotAllowed());
	}
}