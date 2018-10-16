//https://bitbucket.org/teckel12/arduino-new-ping/wiki/Home#!15-sensors-sketch

#include <NewPing.h>
#include <SoftwareSerial.h>

const int RX_PIN = 0;
const int TX_PIN = 1;
const int BLUETOOTH_BAUD_RATE = 9600;

int val = 0;
char trip1[] = "a";
char trip2[] = "b";

#define SONAR_NUM     2  // Number of sensors.
#define MAX_DISTANCE 5 // Maximum distance (in cm) to ping.
#define PING_INTERVAL 33 // Milliseconds between sensor pings (29ms is about the min to avoid cross-sensor echo).

unsigned long pingTimer[SONAR_NUM]; // Holds the times when the next ping should happen for each sensor.
unsigned int cm[SONAR_NUM];         // Where the ping distances are stored.
uint8_t currentSensor = 0;          // Keeps track of which sensor is active.

NewPing sonar[SONAR_NUM] = {     // Sensor object array.
  NewPing(9,10, MAX_DISTANCE), // Each sensor's trigger pin, echo pin, and max distance to ping.
  NewPing(3,5, MAX_DISTANCE)
};

SoftwareSerial bluetooth(RX_PIN, TX_PIN);

const int ledPin = 6;
const int ledPin2 = 11;

void setup() {
  // put your setup code here, to run once:
  Serial.begin(BLUETOOTH_BAUD_RATE);
  bluetooth.begin(BLUETOOTH_BAUD_RATE);
  pingTimer[0] = millis() + 75;           // First ping starts at 75ms, gives time for the Arduino to chill before starting.
  for (uint8_t i = 1; i < SONAR_NUM; i++) // Set the starting time for each sensor.
    pingTimer[i] = pingTimer[i - 1] + PING_INTERVAL;

  pinMode(ledPin, OUTPUT); // Sets the trigPin as an Output
  pinMode(ledPin2, OUTPUT); // Sets the trigPin as an Output
//  digitalWrite(ledPin2, LOW);
}

void loop() {
  // put your main code here, to run repeatedly:
  for (uint8_t i = 0; i < SONAR_NUM; i++) { // Loop through all the sensors.
    if (millis() >= pingTimer[i]) {         // Is it this sensor's time to ping?
      pingTimer[i] += PING_INTERVAL * SONAR_NUM;  // Set next time this sensor will be pinged.
      if (i == 0 && currentSensor == SONAR_NUM - 1) oneSensorCycle(); // Sensor ping cycle complete, do something with the results.
      sonar[currentSensor].timer_stop();          // Make sure previous timer is canceled before starting a new ping (insurance).
      currentSensor = i;                          // Sensor being accessed.
      cm[currentSensor] = 0;                      // Make distance zero in case there's no ping echo for this sensor.
      sonar[currentSensor].ping_timer(echoCheck); // Do the ping (processing continues, interrupt will call echoCheck to look for echo).
    }
  }
  // Other code that *DOESN'T* analyze ping results can go here.
}

void echoCheck() { // If ping received, set the sensor distance to array.
  if (sonar[currentSensor].check_timer())
    cm[currentSensor] = sonar[currentSensor].ping_result / US_ROUNDTRIP_CM;
}

void oneSensorCycle() { // Sensor ping cycle complete, do something with the results.
  
  // The following code would be replaced with your code that does something with the ping results.
  if(cm[0] != 0)
  {
    digitalWrite(ledPin, HIGH);
    bluetooth.write(trip1);
    Serial.write(bluetooth.read());
  }
  else
  {
    digitalWrite(ledPin, LOW);
  }
    
  if(cm[1] != 0)
  {
    digitalWrite(ledPin2, HIGH);
    bluetooth.write(trip2);
    Serial.write(bluetooth.read());
  }
  else
    digitalWrite(ledPin2, LOW);
  
//  for (uint8_t i = 0; i < SONAR_NUM; i++) {
//    Serial.print(i);
//    Serial.print("=");
//    Serial.print(cm[i]);
//    Serial.print("cm ");
//  }
//  Serial.println();
}
