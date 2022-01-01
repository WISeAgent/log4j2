package au.com.wiseagent.springboot.log4j2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
//log4j2 logger
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SpringBootApplication
@RestController
public class VulLog4j2Application {
	private static Logger logger = LogManager.getLogger(VulLog4j2Application.class);
	public static void main(String[] args) {
		SpringApplication.run(VulLog4j2Application.class, args);
	}
	/**
	 * http://localhost:8080/VulLog4j2/${env:username}
	 * NB. passing ${jndi:ldap://localhost} as payload results in 404
	 * @param payload
	 * @return
	 */
	@GetMapping("/VulLog4j2/{payload}")
	@ResponseBody
    public String VulLog4j2(@PathVariable String payload) {
	  logger.info(payload);
 	  payload="${jndi:ldap://localhost}";
      logger.info(payload);
      return "payload received=" + payload;
	} 
	/**
	 *  http://localhost:8080/VulLog4j2PayLoad?payload=${env:username}
	 *  http://localhost:8080/VulLog4j2PayLoad?payload=${jndi:ldap://localhost}
	 * @param payload
	 * @return
	 */
	@GetMapping("/PayLoad")
	@ResponseBody
    public String PayLoad(@RequestParam  String payload) {
		payload="${java:os}";
		logger.info(payload);
		return "payload received" + payload;
	} 
}
