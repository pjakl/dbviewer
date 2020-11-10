package com.pjakl.dbviewer;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableEncryptableProperties
@SpringBootApplication
public class DbViewerApplication {

	public static void main(String[] args) {
		SpringApplication.run(DbViewerApplication.class, args);
	}

}
