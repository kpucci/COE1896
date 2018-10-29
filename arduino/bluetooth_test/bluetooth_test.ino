#include <SparkFun_ADXL345.h>
#include <SoftwareSerial.h>

#define member_size(type, member) sizeof(((type *)0)->member)

const int RX_PIN = 10;
const int TX_PIN = 9;
const int BLUETOOTH_BAUD_RATE = 9600;
const int buff_len = 4;
const int packet_len = buff_len * 4;

bool run_flag = false;

SoftwareSerial bluetooth(RX_PIN, TX_PIN);

// Assign a unique ID to this sensor at the same time
ADXL345 adxl = ADXL345();

typedef struct Packet {
  char x_accel[buff_len];
  char y_accel[buff_len];
  char z_accel[buff_len];
  char timestamp[buff_len];
} Packet;

Packet* create_packet()
{
  // Get current sensor vals
  int x,y,z;  
  adxl.readAccel(&x, &y, &z);

  Serial.println(x); // Debug
//  Serial.println(y);
//  Serial.println(z);

  Packet* packet = malloc(sizeof(Packet));
  
  snprintf(packet->x_accel, member_size(Packet, x_accel), "%d", x);
  snprintf(packet->y_accel, member_size(Packet, y_accel), "%d", y);
  snprintf(packet->z_accel, member_size(Packet, z_accel), "%d", z);

  // TODO: get current time from real-time clock
  snprintf(packet->timestamp, member_size(Packet, timestamp), "%d", millis());

  return packet;
}

void send_packet(Packet* packet)
{
  bluetooth.write(packet->x_accel);
  free(packet);
}

void setup()
{
  // Start serial
  Serial.begin(BLUETOOTH_BAUD_RATE);

  // Start bluetooth
  bluetooth.begin(BLUETOOTH_BAUD_RATE);

  adxl.powerOn();
  adxl.setRangeSetting(16);

  Serial.println(sizeof(Packet));
}

void loop()
{
  // If there's a new value available on bluetooth input stream
  if (bluetooth.available())
  {
    // Read in the character
    char bt_in = bluetooth.read();

    // If character is 's', set start flag to true
    if (bt_in == 's' || bt_in == 'S')
    {
      run_flag = true;
      Serial.println("Start sampling");  // Debug
    }
    else if(bt_in == 'e' || bt_in == 'E')
    {
      run_flag = false;
      Serial.println("Stop sampling");
    }
  }

  if(run_flag)
  {
    
    Packet* packet = create_packet();
    send_packet(packet);
  }

  // Wait a second
  delay(1000);
}


