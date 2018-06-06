package com.github.fabriziocucci.spike;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class Controller {
	
	private final RestTemplate restTemplate;
	private final Tracer tracer;
	
	@Autowired
	public Controller(RestTemplate restTemplate, Tracer tracer) {
		this.restTemplate = restTemplate;
		this.tracer = tracer;
	}
	
	@GetMapping(path = "/test")
	public String test() {
		return String.format(
				"Hi, I'm service-b and this is my baggage:" + System.lineSeparator()
				+ "%s" + System.lineSeparator()
				+ System.lineSeparator()
				+ "%s",
				this.tracer.getCurrentSpan().getBaggage(),
				restTemplate.getForObject("http://localhost:8082/test", String.class));
	}
	
}
