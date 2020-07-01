import java.io.InputStream;
import java.net.*;

public class Wunderground {
    
    final String WEATHER_URL =
        "http://api.wunderground.com/api/%s/conditions/q/%s/%s.json";
    
    String key = null;
    
    @SuppressWarnings("UseOfObsoleteCollectionType")
    public java.util.Hashtable<String, WeatherSet> H
            = new java.util.Hashtable<String, WeatherSet>();
    public java.util.Hashtable<String, javax.swing.ImageIcon> II
            = new java.util.Hashtable<String, javax.swing.ImageIcon>();
    
    Wunderground(String key) throws Exception
    {
        this.key = key;
        if (key == null || key.length() == 0) throw new Exception("Wundergound key must be provided.");
    }
    
    public class WeatherSet 
    {
        String Contents = "";
        String City = "";
        long Time = 0;
        int Celsius = 0;
        int Humidity = 0;
        String IconURL = "";
        String forecast = "";
        int getCelsius() {return Celsius;}
        String getForecast() {return forecast;}
        int getAge() {return (int)(System.currentTimeMillis() - Time) / 1000;}
        boolean isValid() {return getAge() < 15 * 60;}
        public String ToString() {return getForecast();}
        
        void loadIcon() throws Exception {
            if (!IconURL.toLowerCase().startsWith("http:")) return;
            if (II.contains(IconURL)) return;
            javax.swing.ImageIcon i = new javax.swing.ImageIcon(new URL(IconURL));
            while (i.getImageLoadStatus() == java.awt.MediaTracker.LOADING) Thread.sleep(100);
            if (i.getImageLoadStatus() == java.awt.MediaTracker.COMPLETE) II.put(IconURL, i);
        }
        javax.swing.ImageIcon getIcon() {
            return II.get(IconURL);
        }

    }

    public WeatherSet get(String city)
    {
        //System.out.println("\n\nWeatherForecast " + city + "...");
        WeatherSet w = null;
        java.util.Enumeration<WeatherSet> i = H.elements();
        while(i.hasMoreElements()) {
            WeatherSet wi = i.nextElement();
            if (city.equalsIgnoreCase(wi.City)) w = wi;
            //System.out.println("--- Cache " + wi.getAge() + "' --- " + wi.City + ": " + wi.getForecast());
        } 
        if (w == null) w = parse(city);
        else if (!w.isValid())
        {
            System.out.println("--- TooAged " + w.getAge() + "' --- " + w.City + ": " + w.getForecast());
            WeatherSet w2 = parse(city);
            if (w2 != null) w = w2;
        }
        
        if (w != null) H.put(city, w);
        System.out.println("WeatherForecast " + city + ": " + (w == null ? "N/A" : w.getForecast()));
        //try
        //{
        //    Thread.sleep(6000);
        //} catch(Exception e) {}
        return w;
    }
    
    WeatherSet parse(String city) {
        String country = null;
        if (city.equalsIgnoreCase("PARIS")) country = "FR";
        else if (city.equalsIgnoreCase("ORLEANS")) country = "FR";
        else if (city.equalsIgnoreCase("MILAN")) country = "IT";
        else if (city.equalsIgnoreCase("MADRID")) country = "SP";
        if (country == null) country = "";
        return parse(country, city);
    }
    
    WeatherSet parse(String country, String city) {
        city = city.replace(" ", "%20");
        String weatherURL = String.format(WEATHER_URL, key, country, city);
        try {
                URL url = new URL(weatherURL);
                URLConnection con = url.openConnection();
                InputStream in = con.getInputStream();
                if(in == null) throw new Exception("Input stream not available.");
                byte buf[] = new byte[in.available()];
                if (in.read(buf) != buf.length) throw new Exception("Input stream broken.");
                WeatherSet w = new WeatherSet();
                w.City = city;
                w.Time = System.currentTimeMillis();
                w.Contents = new String(buf, "ISO-8859-1");
                parse(w);
                w.loadIcon();
                return w;
        } catch (Exception e) {
            //System.out.println("--- Parse --- " + city + ": " + e.getMessage());
            //e.printStackTrace();
            return null;
        }
        
    }

    void parse(WeatherSet w) throws Exception {
        if (w.Contents.indexOf("\"temp_c\":") >= 0)
        {
            String temp_c = w.Contents.substring(w.Contents.indexOf("\"temp_c\":") + 9,
                    w.Contents.indexOf(",", w.Contents.indexOf("\"temp_c\":") + 9));
            w.Celsius = Integer.parseInt(temp_c);
        } else throw new Exception("temp_c not found.");
        if (w.Contents.indexOf("\"relative_humidity\":") >= 0)
        {
            String relative_humidity = w.Contents.substring(w.Contents.indexOf("\"relative_humidity\":") + 20,
                    w.Contents.indexOf(",", w.Contents.indexOf("\"relative_humidity\":") + 20))
                    .replaceAll("\"", "").replaceAll("%", "");
            w.Humidity = Integer.parseInt(relative_humidity);
        }
        if (w.Contents.indexOf("\"weather\":") >= 0)
        {
            String weather = w.Contents.substring(w.Contents.indexOf("\"weather\":") + 10,
                    w.Contents.indexOf(",", w.Contents.indexOf("\"weather\":") + 10))
                    .replaceAll("\"", "");
            w.forecast = weather + " " + w.Celsius + "°C " + w.Humidity + "%RH";
        }
        if (w.Contents.indexOf("\"icon_url\":") >= 0)
        {
            w.IconURL = w.Contents.substring(w.Contents.indexOf("\"icon_url\":") + 11,
                    w.Contents.indexOf(",", w.Contents.indexOf("\"icon_url\":") + 11))
                    .replaceAll("\"", "");
        }
    }
    
    
    /*
    public static void main(String[] args) throws Exception {
        String myKey = "...";
        Wunderground wunderground = new Wunderground(myKey);
//        String[] cities = {"PARIS", "ORLEANS", "MILAN", "MADRID"};
        String[] cities = {"PARIS"};
        for(int i = 0; i < cities.length; i++) {
            String city = cities[i];
            WeatherSet cc = wunderground.parse(city);
            if (cc != null) {
                System.out.println(city + ": " + cc.getForecast());
            } else System.out.println(city + ": no data available.");
        }
    }
    */
}