package com.kitapkiralama.controller;

import com.kitapkiralama.dto.response.BookResponse;
import com.kitapkiralama.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class WebController {

    private final BookService bookService;

    @Autowired
    public WebController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/books")
    public String books(Model model) {
        List<BookResponse> books = bookService.getAvailableBooks();
        model.addAttribute("books", books);
        return "books";
    }

    @GetMapping("/my-rentals")
    public String myRentals() {
        return "my-rentals";
    }
}
