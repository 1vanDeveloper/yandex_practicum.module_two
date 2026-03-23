package ru.yandex.practicum.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.TestConfig;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(classes = TestConfig.class)
class WebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getItems_request_without_params() throws Exception {
        var getResult = mockMvc.perform(get("/"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(getResult))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                // Проверяем наличие обязательных атрибутов модели
                .andExpect(model().attributeExists("items"))
                .andExpect(model().attributeExists("paging"))
                .andExpect(model().attribute("sort", "ALPHA"))
                // Проверяем, что в HTML отрендерились ключевые данные
                .andExpect(content().string(containsString("Витрина магазина")))
                .andExpect(xpath("//div[@class='card']").nodeCount(4))
                .andExpect(xpath("(//h5[@class='card-title'])[1]").string("Title 1 first"))
                .andExpect(xpath("(//span[contains(@class, 'text-bg-success')])[2]").string(containsString("10.0 руб.")))
                .andExpect(xpath("(//h5[@class='card-title'])[2]").string("Title 2 second"))
                .andExpect(xpath("(//span[contains(@class, 'text-bg-success')])[3]").string(containsString("100.0 руб.")))
                .andExpect(xpath("(//h5[@class='card-title'])[3]").string("Title 3 third"))
                .andExpect(xpath("(//span[contains(@class, 'text-bg-success')])[4]").string(containsString("399.0 руб.")))
                .andExpect(xpath("(//h5[@class='card-title'])[4]").string("Title 4 forth"))
                .andExpect(xpath("(//span[contains(@class, 'text-bg-success')])[5]").string(containsString("2000.0 руб.")));
    }

    @Test
    void getItems_request_with_params() throws Exception {
        var getResult = mockMvc.perform(get("/")
                        .param("search", "phone")
                        .param("sort", "PRICE")
                        .param("pageSize", "2"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(getResult))
                .andExpect(status().isOk());
    }

    @Test
    void editCartItemsFromItems() throws Exception {
        var getResult = mockMvc.perform(post("/items")
                        .param("id", "1")
                        .param("action", "PLUS")
                        .param("pageNumber", "1"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(getResult))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/items?search&sort=ALPHA&pageNumber=1&pageSize=10"));
    }

    @Test
    void getItem() {
    }

    @Test
    void editCartItemsFromItem() {
    }

    @Test
    void getCart() {
    }

    @Test
    void editCartItemsFromCart() {
    }

    @Test
    void getOrders() {
    }

    @Test
    void testGetOrders() {
    }

    @Test
    void buy() {
    }
}