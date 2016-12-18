package warehouse.model.loggers;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Loggers {

    public static Logger getConsoleLogger(Class c) {
        Properties props = new Properties();
        InputStream inStream = c.getResourceAsStream("/console-log4j.properties");
        try {
            props.load(inStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        PropertyConfigurator.configure(props);
        return LoggerFactory.getLogger(c);
    }

    public static Logger getULogger(Class c, String fileName) {
        Properties props = new Properties();
        InputStream inStream = c.getResourceAsStream("/log4j-u.properties");
        try {
            props.load(inStream);
            props.put("log4j.appender.file.File", "logs/" + fileName + "-logs.log");
        } catch (IOException e) {
            e.printStackTrace();
        }
        PropertyConfigurator.configure(props);
        return LoggerFactory.getLogger(c);
    }
}
