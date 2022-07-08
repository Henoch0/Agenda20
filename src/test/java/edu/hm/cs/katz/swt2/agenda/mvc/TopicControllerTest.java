package edu.hm.cs.katz.swt2.agenda.mvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import edu.hm.cs.katz.swt2.agenda.service.TaskService;
import edu.hm.cs.katz.swt2.agenda.service.TopicService;
import edu.hm.cs.katz.swt2.agenda.service.UserService;
import edu.hm.cs.katz.swt2.agenda.service.dto.UserDisplayDto;

@WebMvcTest
public class TopicControllerTest {

    @Autowired
    MockMvc mvc;
    
    @MockBean
    TopicService topicService;
    
    @MockBean
    TaskService taskService;

    @MockBean
    UserService userService;
    
    @Test
    public void testGetIndex() throws Exception {
        MvcResult indexPage = mvc.perform(MockMvcRequestBuilders.get("/")).
                andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
        
        assertTrue(indexPage.getResponse().getContentAsString().contains("Agenda"));
    }
    
    @Test
    @WithMockUser(username = "finn", password = "user", roles = "USER")
    public void testGetTopics() throws Exception {
        
        UserDisplayDto finn = new UserDisplayDto();
        finn.setLogin("finn");
        
        Mockito.when(userService.getUserInfo("finn")).thenReturn(finn);
        
        MvcResult result = mvc.perform(MockMvcRequestBuilders.get("/topics")).
                andExpect(MockMvcResultMatchers.status().isOk()).andReturn();   
        
        UserDisplayDto user = (UserDisplayDto) result.getModelAndView().getModel().get("user");
        
        assertEquals(finn, user);
    }
}
