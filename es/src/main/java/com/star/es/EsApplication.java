package com.star.es;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
/**
 * @author gaoxing
 * @date 2018-09-19
 */
@SpringBootApplication
public class EsApplication {
	private static Logger logger = LoggerFactory.getLogger(EsApplication.class);
	public static void main(String[] args) {
		SpringApplication.run(EsApplication.class, args);
		logger.info("-------------------------------------");
		logger.info("	 GTGJ-ES 微服务启动成功		  ");
		logger.info("-------------------------------------");
	}

}
