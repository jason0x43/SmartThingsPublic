/**
 *  Weather Station
 *
 *  Author: j.cheatham@gmail.com
 *  Date: 2017-09-10
 *  Code: https://github.com/jason0x43/SmartThingsPublic/devicetypes/jason0x43/weather-station.src
 *
 * Copyright (C) 2017 Jason Cheatham <j.cheatham@gmail.com>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy
 *  of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License for the specific language governing permissions and limitations
 *  under the License.
 */

metadata {
    definition(
        name: 'Weather Station',
        author: 'Jason Cheatham',
        namespace: 'jason0x43'
    ) {
        capability('Polling')
        capability('Refresh')
        capability('Relative Humidity Measurement')
        capability('Temperature Measurement')
        capability('Thermostat Mode')
    }

    tiles {
        valueTile(
            'temperature',
            'device.temperature',
        ) {
            state(
                'default',
                label: '${currentValue}°F',
                unit: 'dF',
                
                backgroundColors: [
                    [value: 31, color: '#153591'],
                    [value: 44, color: '#1e9cbb'],
                    [value: 59, color: '#90d2a7'],
                    [value: 74, color: '#44b621'],
                    [value: 84, color: '#f1d801'],
                    [value: 95, color: '#d04e00'],
                    [value: 96, color: '#bc2323']
                ]
            )
        }

        valueTile(
            'humidity',
            'device.humidity'
        ) {
            state('default',  label:'Humidity\n${currentValue}%', unit: '%')
        }

        standardTile(
            'direction',
            'device.thermostatMode'
        ) {
            state(
            	'cool',
                label: 'Cool',
                icon: 'st.Weather.weather2',
                backgroundColor: '#9999ff'
            )
            state(
            	'heat',
                label: 'Heat',
                icon: 'st.Weather.weather2',
                backgroundColor: '#ff9999'
            )
        	state(
            	'off',
                icon: 'st.Weather.weather2',
                label: 'Steady'
            )
        }

        standardTile(
            'forecast',
            'device.forecast'
        ) {
            state(
                'default',
                label: 'updating...',
                icon: 'st.unknown.unknown.unknown'
            )
            state(
                'chanceflurries',
                label: 'Chance of Flurries',
                icon: 'st.Weather.weather6'
            )
            state(
                'chancerain',
                label: 'Chance of Rain',
                icon: 'st.Weather.weather9'
            )
            state(
                'chancesleet',
                label: 'Chance of Sleet',
                icon: 'st.Weather.weather6'
            )
            state(
                'chancesnow',
                label: 'Chance of Snow',
                icon: 'st.Weather.weather6'
            )
            state(
                'chancetstorms',
                label: 'Chance of TStorms',
                icon: 'st.Weather.weather9'
            )
            state(
                'clear',
                label: 'Clear',
                icon: 'st.Weather.weather14'
            )
            state(
                'cloudy',
                label: 'Cloudy',
                icon: 'st.Weather.weather15'
            )
            state(
                'flurries',
                label: 'Flurries',
                icon: 'st.Weather.weather6'
            )
            state(
                'fog',
                label: 'Fog',
                icon: 'st.Weather.weather13'
            )
            state(
                'hazy',
                label: 'Hazy',
                icon: 'st.Weather.weather13'
            )
            state(
                'mostlycloudy',
                label: 'Mostly Cloudy',
                icon: 'st.Weather.weather15'
            )
            state(
                'mostlysunny',
                label: 'Mostly Sunny',
                icon: 'st.Weather.weather11'
            )
            state(
                'partlycloudy',
                label: 'Partly Cloudy',
                icon: 'st.Weather.weather11'
            )
            state(
                'partlysunny',
                label: 'Partly Sunny',
                icon: 'st.Weather.weather11'
            )
            state(
                'sleet',
                label: 'Sleet',
                icon: 'st.Weather.weather10'
            )
            state(
                'rain',
                label: 'Rain',
                icon: 'st.Weather.weather10'
            )
            state(
                'snow',
                label: 'Snow',
                icon: 'st.Weather.weather7'
            )
            state(
                'sunny',
                label: 'Sunny',
                icon: 'st.Weather.weather14'
            )
            state(
                'tstorms',
                label: 'Thunder Storms',
                icon: 'st.Weather.weather10'
            )
        }

        standardTile(
        	'water',
            'device.water'
        ) {
            state(
            	'default',
                label: 'updating...',
                icon: 'st.unknown.unknown.unknown'
            )
            state(
            	'true',
                icon: 'st.alarm.water.wet',
                backgroundColor: '#ff9999'
            )
            state(
            	'false',
                icon: 'st.alarm.water.dry',
                backgroundColor: '#99ff99'
            )
        }
        
        standardTile(
            'refresh',
            'device.refresh',
            inactiveLabel: false,
            decoration: 'flat'
        ) {
            state 'default', action:'polling.poll', icon:'st.secondary.refresh'
        }

        main('temperature')

        details([
            'temperature',
            'humidity',
            'forecast',
            'direction',
            'water',
            'refresh'
        ])
    }
}

def poll() {
    def weather = getWeatherFeature('conditions')
    def forecast = getWeatherFeature('hourly')

    if (!weather) {
        log.debug('No data found')
        return false
    }
    
    def icon = weather.current_observation.icon
    log.debug("Forecast: ${icon}")
    sendEvent(name: 'forecast', value: icon)

    def temp = weather.current_observation.temp_f.toFloat()
    log.debug("Current Temperature: ${temp}ºF")
    sendEvent(name: 'temperature', value: temp)

    def humidity = weather.current_observation.relative_humidity.replace('%', '').toInteger()
    log.debug("Relative Humidity: ${humidity}%")
    sendEvent(name: 'humidity', value: humidity)
    
    def hour1 = forecast.hourly_forecast[0].temp.english.toInteger()
    def hour2 = forecast.hourly_forecast[1].temp.english.toInteger()
    def mode = 'off';
    if (hour1 > hour2) {
        mode = 'cool';
    } else if (hour1 < hour2) {
        mode = 'heat';
    }
    log.debug("Direction: ${mode}")
   	sendEvent(name: 'thermostatMode', value: mode)

    def precip = weather.current_observation.precip_1hr_in.toFloat()
    if (precip > 0) {
        log.debug("Precipitation: ${precip}")
        sendEvent(name: 'water', value: 'true')
    } else {
        log.debug('Precipitation: None')
        sendEvent(name: 'water', value: 'false')
    }
}