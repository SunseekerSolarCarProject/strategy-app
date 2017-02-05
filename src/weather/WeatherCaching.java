package weather;

import net.aksingh.owmjapis.CurrentWeather;
import net.aksingh.owmjapis.HourlyForecast;
import net.aksingh.owmjapis.OpenWeatherMap;
import utilities.Position;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

/**
 * PROJECT: seniordesign
 * AUTHOR: aaron  2/4/2017.
 * DATE: 2/4/2017
 *
 * DESCRIPTION:
 *
 *
 * INPUTS:
 *
 *
 * OUTPUTS:
 */
public class WeatherCaching {

    private static Position[] positions;
    private static ArrayList<WeatherForecast> forecasts;

    /**
     * @param args
     */
    public static void main(String[] args) {

        //get current weather conditions


        positions = Position.loadPositions("leg-1-10_items");
        if (positions == null) {
            System.out.println("Error loading positions");
        } else {
            saveCurrent("weather-10_locations");
        }

        loadCurrent("weather-10_locations");

        saveForecast("weather-forecast-10_locations");

        forecasts = loadForecast("weather-forecast-10_locations");

        if (forecasts != null) {
            for (int i = 0; i < forecasts.size(); i++) {
                forecasts.get(i).printOut();
            }
        } else {
            System.out.println("Error loading forecasts");
        }

    }

    /**
     * @param outFileName
     * @return
     */
    public static boolean saveCurrent(String outFileName) {

        outFileName += ".csv";
        OpenWeatherMap owm = new OpenWeatherMap(ApiKey.getApiKey());
        CurrentWeather cw;


        //read coordinates from csv file
        try {

            FileWriter fw = new FileWriter(outFileName);
            BufferedWriter bufferedWriter = new BufferedWriter(fw);

            float latitude;
            float longitude;
            float cloudsPercentage;
            float windSpeed;
            float windDirection;
            String sunrise;
            String sunset;

            bufferedWriter.write(String.format("%d\n", positions.length));
            for (int i = 0; i < positions.length; i++) {

                latitude = positions[i].getLatitude();
                longitude = positions[i].getLongitude();
                System.out.println(latitude + " " + longitude);

                cw = owm.currentWeatherByCoordinates(latitude, longitude);
                cloudsPercentage = cw.getCloudsInstance().getPercentageOfClouds();
                windSpeed = cw.getWindInstance().getWindSpeed();
                windDirection = cw.getWindInstance().getWindDegree();
                sunrise = cw.getSysInstance().getSunriseTime().toString();
                sunset = cw.getSysInstance().getSunsetTime().toString();

                bufferedWriter.write(String.format("%f,%f,%f,%f,%f,%s,%s\n", latitude, longitude, cloudsPercentage, windSpeed, windDirection,
                        sunrise, sunset));

                System.out.println(cw.getCityName().toString());
            }

            bufferedWriter.close();
            fw.close();
        } catch (Exception e) {

            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * @param fileName
     * @return
     */
    public static WeatherCurrent[] loadCurrent(String fileName) {
        fileName = fileName + ".csv";
        WeatherCurrent[] weatherCurrents;

        try {


            FileReader fileReader = new FileReader(fileName);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            String read = bufferedReader.readLine();

            int length = Integer.valueOf(read);

            weatherCurrents = new WeatherCurrent[length];

            for (int i = 0; i < length; i++) {
                String line = bufferedReader.readLine();

                String[] items = line.split(",");
                weatherCurrents[i] = new WeatherCurrent(
                        Float.valueOf(items[0]),
                        Float.valueOf(items[1]),
                        Float.valueOf(items[2]),
                        Float.valueOf(items[3]),
                        Float.valueOf(items[4]),
                        items[5],
                        items[6]);

            }

            bufferedReader.close();
            fileReader.close();

            return weatherCurrents;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean saveForecast(String outFileName) {

        int count = 0;
        outFileName += ".csv";
        OpenWeatherMap owm = new OpenWeatherMap(ApiKey.getApiKey());
        HourlyForecast hf;


        //read coordinates from csv file
        try {

            FileWriter fw = new FileWriter(outFileName);
            BufferedWriter bufferedWriter = new BufferedWriter(fw);

            float latitude;
            float longitude;
            float cloudsPercentage;
            float windSpeed;
            float windDirection;
            String forecastTime;

            for (int i = 0; i < positions.length; i++) {

                latitude = positions[i].getLatitude();
                longitude = positions[i].getLongitude();
                System.out.println(latitude + " " + longitude);

                hf = owm.hourlyForecastByCoordinates(latitude, longitude);

                for (int j = 0; j < hf.getForecastCount(); j++) {
                    cloudsPercentage = hf.getForecastInstance(j).getCloudsInstance().getPercentageOfClouds();
                    windSpeed = hf.getForecastInstance(j).getWindInstance().getWindSpeed();
                    windDirection = hf.getForecastInstance(j).getWindInstance().getWindDegree();
                    forecastTime = hf.getForecastInstance(j).getDateTimeText();

                    bufferedWriter.write(String.format("%f,%f,%f,%f,%f,%s\n", latitude, longitude, cloudsPercentage,
                            windSpeed, windDirection, forecastTime));

                    count++;
                }


                System.out.println(hf.getCityInstance().getCityName());
            }

            bufferedWriter.close();
            fw.close();
        } catch (Exception e) {

            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static ArrayList<WeatherForecast> loadForecast(String fileName) {
        fileName = fileName + ".csv";
        ArrayList<WeatherForecast> weatherForecasts = new ArrayList<WeatherForecast>();

        ArrayList<Float> cloudPercentages;
        ArrayList<Float> windSpeeds;
        ArrayList<Float> windDegrees;
        ArrayList<String> time;

        String line;
        String[] items;
        float currentLat;

        try {

            FileReader fileReader = new FileReader(fileName);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            int i = 0;
            while ((line = bufferedReader.readLine()) != null) {
                System.out.println(i + " " + line);
                i++;
                items = line.split(",");

                currentLat = Float.valueOf(items[0]);
                WeatherForecast wf = new WeatherForecast(Float.valueOf(items[0]), Float.valueOf(items[1]));

                cloudPercentages = new ArrayList<Float>();
                windSpeeds = new ArrayList<Float>();
                windDegrees = new ArrayList<Float>();
                time = new ArrayList<String>();

                while (Float.valueOf(items[0]) == currentLat) {
                    cloudPercentages.add(Float.valueOf(items[2]));
                    windSpeeds.add(Float.valueOf(items[3]));
                    windDegrees.add(Float.valueOf(items[4]));
                    time.add(items[5]);

                    //get next line with this same lat
                    line = bufferedReader.readLine();

                    if (line != null) {
                        items = line.split(",");
                        currentLat = Float.valueOf(items[0]);
                    }

                }

                wf.setCloudPercentages(cloudPercentages);
                wf.setWindSpeeds(windSpeeds);
                wf.setWindDegrees(windDegrees);
                wf.setTime(time);

                weatherForecasts.add(wf);
            }


            bufferedReader.close();
            fileReader.close();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return weatherForecasts;
    }


}



