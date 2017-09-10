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

preferences {
    input(
        'zipcode',
        'text',
        title: 'ZipCode',
        description: 'ZIP code for forecast (autodetected by default)'
    )
}

metadata {
    definition(
        name: 'Weather Station',
        author: 'Jason Cheatham',
        namespace: 'jason0x43'
    ) {
        capability 'Polling'
        capability 'Relative Humidity Measurement'
        capability 'Temperature Measurement'
    }

    tiles {
        // First Row
        valueTile(
            'temperature',
            'device.temperature',
            width: 1,
            height: 1,
            canChangeIcon: true
        ) {
            state(
                'temperature',
                label: 'Outside\n${currentValue}°F',
                unit:'F',
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
            'device.humidity',
            inactiveLabel: false,
            decoration: 'flat'
        ) {
            state 'default',  label:'${currentValue}%', unit: 'Humidity'
        }

        valueTile(
            'feels_like',
            'device.feels_like',
            inactiveLabel: false,
            decoration: 'flat'
        ) {
            state 'feels_like', label: '${currentValue}ºF', unit: 'Feels Like'
        }

        // Second Row
        standardTile(
            'forecast',
            'device.forecast',
            inactiveLabel: false,
            decoration: 'flat'
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

        valueTile(
            'wind_speed',
            'device.wind_speed',
            inactiveLabel: false,
            decoration: 'flat'
        ) {
            state(
                'wind_speed',
                label: '${currentValue}',
                unit: 'mph',
                backgroundColors: [
                    // Values and colors based on the Beaufort Scale
                    // http://en.wikipedia.org/wiki/Beaufort_scale#Modern_scale
                    [value: 0,  color: '#ffffff'],
                    [value: 1,  color: '#ccffff'],
                    [value: 4,  color: '#99ffcc'],
                    [value: 8,  color: '#99ff99'],
                    [value: 13, color: '#99ff66'],
                    [value: 18, color: '#99ff00'],
                    [value: 25, color: '#ccff00'],
                    [value: 31, color: '#ffff00'],
                    [value: 39, color: '#ffcc00'],
                    [value: 47, color: '#ff9900'],
                    [value: 55, color: '#ff6600'],
                    [value: 64, color: '#ff3300'],
                    [value: 74, color: '#ff0000']
                ]
            )
        }

        // Third Row
        valueTile(
            'location',
            'device.location',
            inactiveLabel: false,
            decoration: 'flat'
        ) {
            state 'location', label: '${currentValue}', unit: 'ZipCode'
        }

        // Fourth Row
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
            'feels_like',
            'forecast',
            'wind_speed',
            'wind_direction',
            'uv_index',
            'water',
            'location',
            'refresh'
        ])
    }
}

def poll() {
    def weather

    if (settings.zipcode) {
        weather = getWeatherFeature('conditions', settings.zipcode)
    } else {
        // No ZIP code specified; ST will use hub location
        weather = getWeatherFeature('conditions')
    }

    if (!weather) {
        log.debug('No data found')
        return false
    }

    log.debug('Forecast: ${weather.current_observation.icon}')
    sendEvent(name: 'forecast', value: weather.current_observation.icon)

    log.debug('Wind Speed: ${weather.current_observation.wind_mph} mph')
    sendEvent(name: 'wind_speed', value: weather.current_observation.wind_mph)
    log.debug('Wind Direction: ${weather.current_observation.wind_dir}')
    sendEvent(
        name: 'wind_direction',
        value: weather.current_observation.wind_dir
    )

    log.debug('Current Temperature: ${weather.current_observation.temp_f}ºF')
    sendEvent(name: 'temperature', value: weather.current_observation.temp_f)

    log.debug('Relative Humidity: ${weather.current_observation.relative_humidity}')
    sendEvent(
        name: 'humidity',
        value: weather.current_observation.relative_humidity
    )

    log.debug('Feels Like: ${weather.current_observation.feelslike_f}')
    sendEvent(
        name: 'feels_like',
        value: weather.current_observation.feelslike_f
    )

    log.debug('Location: ${weather.current_observation.display_location.zip.toString()}')
    sendEvent(
        name: 'location',
        value: weather.current_observation.display_location.zip.toString()
    )

    if (weather.current_observation.precip_1hr_in.toFloat() > 0) {
        log.debug('Precipitation: ${weather.current_observation.precip_1hr_in}')
        sendEvent(name: 'water', value: 'true')
    } else {
        log.debug('Precipitation: None')
        sendEvent(name: 'water', value: 'false')
    }
}
