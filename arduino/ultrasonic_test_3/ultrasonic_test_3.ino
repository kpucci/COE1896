//https://bitbucket.org/teckel12/arduino-new-ping/wiki/Home#!15-sensors-sketch

#include <NewPing.h>
#include <SoftwareSerial.h>


// ======================================
// ===            SONAR STUFF         ===
// ======================================
#define SONAR_NUM     2  // Number of sensors.
#define MAX_DISTANCE 10 // Maximum distance (in cm) to ping.
#define PING_INTERVAL 100 // Milliseconds between sensor pings (29ms is about the min to avoid cross-sensor echo).

unsigned long pingTimer[SONAR_NUM]; // Holds the times when the next ping should happen for each sensor.
unsigned int cm[SONAR_NUM];         // Where the ping distances are stored.
uint8_t currentSensor = 0;          // Keeps track of which sensor is active.

NewPing sonar[SONAR_NUM] = {     // Sensor object array.
  NewPing(3,5, MAX_DISTANCE),
  NewPing(6,7, MAX_DISTANCE) // Each sensor's trigger pin, echo pin, and max distance to ping.
  
};

// ======================================
// ===         BLUETOOTH STUFF        ===
// ======================================
const int RX_PIN = 10;
const int TX_PIN = 9;
const int BLUETOOTH_BAUD_RATE = 9600;
bool run_flag = false;
const int buff_size = sizeof(int)*2 + sizeof(char)*1;

SoftwareSerial bluetooth(RX_PIN, TX_PIN);

// ========== SETUP ==========
void setup() {
  // ----- Bluetooth Setup -----
  bluetooth.begin(BLUETOOTH_BAUD_RATE);
}

// ========== LOOP ==========
void loop() {
  // If there's a new value available on bluetooth input stream
  if (bluetooth.available())
  {
//    Serial.println("bluetooth available");
    // Read in the character
    char bt_in = bluetooth.read();

    // If character is 's', set start flag to true
    if (bt_in == 's' || bt_in == 'S')
    {
      run_flag = true;
      pingTimer[0] = millis() + 75;
      for (uint8_t i = 1; i < SONAR_NUM; i++) 
        pingTimer[i] = pingTimer[i - 1] + PING_INTERVAL;
    }
    else if(bt_in == 'e' || bt_in == 'E')
    {
      run_flag = false;
    }
  }

  while(run_flag)
  {
    // ---------- Sonar Stuff ----------
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
  
    if (bluetooth.available())
    {
      // Read in the character
      char bt_in = bluetooth.read();
  
      if(bt_in == 'e' || bt_in == 'E')
      {
        run_flag = false;
      }
    }
  }
}

void send_packet()
{
//  Serial.println(sensor_num);
//  Serial.println(distance);
  
  // Print to bluetooth module
  char* packet = malloc(buff_size);
  snprintf(packet, buff_size, "%u %u", cm[0], cm[1]);
  bluetooth.write(packet);
  free(packet);     
}

// If ping received, set the sensor distance to array
void echoCheck() {
  if (sonar[currentSensor].check_timer())
    cm[currentSensor] = sonar[currentSensor].ping_result / US_ROUNDTRIP_CM;
}

// Sensor ping cycle complete, do something with the results
void oneSensorCycle() {
  if(cm[0] != 0 || cm[1] != 0)
    send_packet();
}
