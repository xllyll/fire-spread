package com.xllyll.fire.model;

public enum WeatherCondition {
    SUNNY("sunny","晴朗"),
    CLOUDY("cloudy","阴天"),
    RAIN("rain","小雨"),
    HEAVYRAIN("heavyRain","大雨"),
    SNOW("snow","小雪"),
    HEAVYSNOW("heavySnow","大雪");
    private final String code;
    private final String desc;
    WeatherCondition(String code,String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }
}
