//https://bitbucket.org/teckel12/arduino-new-ping/wiki/Home#!15-sensors-sketch

#include <NewPing.h>
#include <SoftwareSerial.h>

#define SONAR_NUM     2  // Number of sensors.
#define MAX_DISTANCE 15 // Maximum distance (in cm) to ping.
#define PING_INTERVAL 33 // Milliseconds between sensor pings (29ms is about the min to avoid cross-sensor echo).


unsigned long pingTimer[SONAR_NUM]; // Holds the times when the next ping should happen for each sensor.
unsigned int cm[SONAR_NUM];         // Where the ping distances are stored.
int currentSensor = 0;          // Keeps track of which sensor is active.
int increment = 2;

const int RX_PIN = 10;
const int TX_PIN = 9;
const int BLUETOOTH_BAUD_RATE = 9600;

SoftwareSerial bluetooth(RX_PIN, TX_PIN);

bool run_flag = false;
bool send_flag = false;
const int buff_size = 24;

NewPing sonar[SONAR_NUM] = {     // Sensor object array.
//  NewPing(0,1, MAX_DISTANCE),   // Each sensor's trigger pin, echo pin, and max distance to ping.
  NewPing(4,3, MAX_DISTANCE),
//  NewPing(17,18, MAX_DISTANCE),
  NewPing(4,5, MAX_DISTANCE)
//  NewPing(8,9, MAX_DISTANCE)
//  NewPing(17,18, MAX_DISTANCE),
//  NewPing(19,20, MAX_DISTANCE),
//  NewPing(21,22, MAX_DISTANCE)

 };

//const int ledPin = 29;
//const int ledPin2 = 26;
//const int ledPin3 = 27;
//const int ledPin4 = 28;

void setup() {

  bluetooth.begin(BLUETOOTH_BAUD_RATE);
//  Serial.begin(9600);
//
//  pinMode(ledPin, OUTPUT);
//  pinMode(ledPin2, OUTPUT);
//  pinMode(ledPin3, OUTPUT);
//  pinMode(ledPin4, OUTPUT);
  // put your setup code here, to run once:

  pingTimer[0] = millis() + 75;           // First ping starts at 75ms, gives time for the Arduino to chill before starting.
  for (uint8_t i = 1; i < SONAR_NUM; i++) // Set the starting time for each sensor.
    pingTimer[i] = pingTimer[i - 1] + PING_INTERVAL;

  
}

void loop() {
  // If there's a new value available on bluetooth input stream
//  if (bluetooth.available())
//  {
////    Serial.println("bluetooth available");
//    // Read in the character
//    char bt_in = bluetooth.read();
//
//    // If character is 's', set start flag to true
//    if (bt_in == 's' || bt_in == 'S')
//    {
//      run_flag = true;
//      pingTimer[0] = millis() + 75;
//      for (uint8_t i = 1; i < SONAR_NUM; i++) 
//        pingTimer[i] = pingTimer[i - 1] + PING_INTERVAL;
//    }
//    else if(bt_in == 'e' || bt_in == 'E')
//    {
//      run_flag = false;
//    }
//  }

  run_flag=true;
  send_flag=false;

  while(run_flag)
  {
    // put your main code here, to run repeatedly:
    for (uint8_t i = 0; i < SONAR_NUM; i++) {
      if (millis() >= pingTimer[i]) {         // Is it this sensor's time to ping?
        pingTimer[i] += PING_INTERVAL * SONAR_NUM;  // Set next time this sensor will be pinged.
        if (i == 0 && currentSensor == SONAR_NUM - 1)
        {
          if(send_flag)
          {
            char* packet = malloc(buff_size);
            snprintf(packet, buff_size, "%u %u\n", cm[0], cm[1]);
            bluetooth.write(packet);
            free(packet);
          }
          
          send_flag = false;
        }
        
        sonar[currentSensor].timer_stop();          // Make sure previous timer is canceled before starting a new ping (insurance).
        currentSensor = i;                          // Sensor being accessed.
        cm[currentSensor] = 0;                      // Make distance zero in case there's no ping echo for this sensor.
        sonar[currentSensor].ping_timer(echoCheck); // Do the ping (processing continues, interrupt will call echoCheck to look for echo).
      }
    }
  }
}

void echoCheck() { // If ping received, set the sensor distance to array.
  if (sonar[currentSensor].check_timer())
  {
    send_flag = true;
    cm[currentSensor] = sonar[currentSensor].ping_result / US_ROUNDTRIP_CM;
  }
//  {
//    char* packet = malloc(buff_size);
//    snprintf(packet, buff_size, "%d %u\n", currentSensor, sonar[currentSensor].ping_result / US_ROUNDTRIP_CM);
//    bluetooth.write(packet);
//    free(packet);
//  }
    
}
