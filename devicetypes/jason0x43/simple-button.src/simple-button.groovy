/**
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  Based on Simulated Button, Copyright 2015 SmartThings
 */
metadata {
    definition (name: "Simple Button", namespace: "jason0x43", author: "Jason Cheatham") {
        capability "Actuator"
        capability "Button"
        capability "Sensor"
        command "push"
    }

    tiles {
        standardTile("button", "device.button", width: 2, height: 2, canChangeIcon: true) {
            state "default", label: "Push", backgroundColor: "#ffffff", action: "push"
        }
        main "button"
        details(["button"])
    }
}

def parse(String description) {
}

def push() {
    sendEvent(name: "button", value: "pushed", data: [buttonNumber: "1"], descriptionText: "$device.displayName button 1 was pushed", isStateChange: true)
}
