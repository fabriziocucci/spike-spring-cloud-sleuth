package com.github.fabriziocucci.spike;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {
	
	private final Tracer tracer;
	
	@Autowired
	public Controller(Tracer tracer) {
		this.tracer = tracer;
	}
	
	@GetMapping(path = "/test")
	public String test() {
		return String.format(
				"Hi, I'm service-c and this is my baggage:" + System.lineSeparator()
				+ "%s" + System.lineSeparator(),
				this.tracer.getCurrentSpan().getBaggage());
	}
	
}
