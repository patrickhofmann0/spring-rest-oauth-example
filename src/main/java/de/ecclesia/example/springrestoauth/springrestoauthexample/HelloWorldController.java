package de.ecclesia.example.springrestoauth.springrestoauthexample;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloWorldController {

	@GetMapping("/hello")
	public String returnHelloWorld() {
		return "Hello World!";
	}

}

