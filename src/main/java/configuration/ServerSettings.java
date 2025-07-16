package configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public class ServerSettings {

    private static ServerSettings instance;
    private final Map<String,String> settings = new HashMap<>();


    public String getSetting(String key){
        if (settings.containsKey(key)){
            return settings.get(key);
        }else {
            throw new NoSuchElementException();
        }
    }

   private ServerSettings(){}

    public void addSetting(String key,String value){
        settings.put(key,value);
    }

    public static ServerSettings getInstance() {
        if (instance == null){
            instance = new ServerSettings();
        }
        return instance;
    }
}
