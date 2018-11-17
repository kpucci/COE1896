#include "I2Cdev.h"
#include "MPU6050_6Axis_MotionApps20.h"
#include <SoftwareSerial.h>

// Arduino Wire library is required if I2Cdev I2CDEV_ARDUINO_WIRE implementation
// is used in I2Cdev.h
#if I2CDEV_IMPLEMENTATION == I2CDEV_ARDUINO_WIRE
    #include "Wire.h"
#endif


// ========== MPU6050 STUFF ==========
MPU6050 mpu;

#define OUTPUT_READABLE_YAWPITCHROLL

// MPU control/status vars
bool dmpReady = false;  // set true if DMP init was successful
uint8_t mpuIntStatus;   // holds actual interrupt status byte from MPU
uint8_t devStatus;      // return status after each device operation (0 = success, !0 = error)
uint16_t packetSize;    // expected DMP packet size (default is 42 bytes)
uint16_t fifoCount;     // count of all bytes currently in FIFO
uint8_t fifoBuffer[64]; // FIFO storage buffer

// orientation/motion vars
Quaternion q;           // [w, x, y, z]         quaternion container
VectorInt16 aa;         // [x, y, z]            accel sensor measurements
VectorInt16 aaReal;     // [x, y, z]            gravity-free accel sensor measurements
VectorInt16 aaWorld;    // [x, y, z]            world-frame accel sensor measurements
VectorFloat gravity;    // [x, y, z]            gravity vector
float euler[3];         // [psi, theta, phi]    Euler angle container
float ypr[3];           // [yaw, pitch, roll]   yaw/pitch/roll container and gravity vector

// ================================================================
// ===               INTERRUPT DETECTION ROUTINE                ===
// ================================================================

volatile bool mpuInterrupt = false;     // indicates whether MPU interrupt pin has gone high
void dmpDataReady() {
    mpuInterrupt = true;
}

// ======================================
// ===         BLUETOOTH STUFF        ===
// ======================================
const int RX_PIN = 10;
const int TX_PIN = 9;
const int BLUETOOTH_BAUD_RATE = 9600;
bool run_flag = false;
const int buff_size = 24;

SoftwareSerial bluetooth(RX_PIN, TX_PIN);

// ======================================
// ===            FSR STUFF           ===
// ======================================

int fsrPin = 0;
int fsrReading;
int fsrVoltage;
unsigned long fsrResistance;
unsigned long fsrConductance; 
long fsrForce;

long previousMillis = 0;
long interval = 1000; 

// ========== CODE SETUP ==========
void setup() {
  // ----- MPU6050 Setup -----
  #if I2CDEV_IMPLEMENTATION == I2CDEV_ARDUINO_WIRE
      Wire.begin();
      TWBR = 24; // 400kHz I2C clock (200kHz if CPU is 8MHz)
  #elif I2CDEV_IMPLEMENTATION == I2CDEV_BUILTIN_FASTWIRE
      Fastwire::setup(400, true);
  #endif

  mpu.initialize();
  devStatus = mpu.dmpInitialize();

  // supply your own gyro offsets here, scaled for min sensitivity
  mpu.setXGyroOffset(220);
  mpu.setYGyroOffset(76);
  mpu.setZGyroOffset(-85);
  mpu.setZAccelOffset(1788); // 1688 factory default for my test chip

  // make sure it worked (returns 0 if so)
  if (devStatus == 0) {
      mpu.setDMPEnabled(true);

      attachInterrupt(0, dmpDataReady, RISING);
      mpuIntStatus = mpu.getIntStatus();

      dmpReady = true;

        // get expected DMP packet size for later comparison
        packetSize = mpu.dmpGetFIFOPacketSize();
    } else {
        // Deal with error
    }

  // ----- Bluetooth Setup -----
  bluetooth.begin(BLUETOOTH_BAUD_RATE);
}


// ========== CODE LOOP ==========
void loop() {
  if (!dmpReady) return;
  
  // If there's a new value available on bluetooth input stream
  if (bluetooth.available())
  {
    // Read in the character
    char bt_in = bluetooth.read();

    // If character is 's', set start flag to true
    if (bt_in == 's' || bt_in == 'S')
    {
      run_flag = true;
    }
    else if(bt_in == 'e' || bt_in == 'E')
    {
      run_flag = false;
    }
  }

  while(run_flag)
  {
    if (bluetooth.available())
    {
      // Read in the character
      char bt_in = bluetooth.read();
  
      if(bt_in == 'e' || bt_in == 'E')
      {
        run_flag = false;
      }
    }
    // ---------- MPU6050 Stuff ----------

    unsigned long currentMillis = millis();
  
    // wait for MPU interrupt or extra packet(s) available
    while (!mpuInterrupt && fifoCount < packetSize){}
  
    // reset interrupt flag and get INT_STATUS byte
    mpuInterrupt = false;
    mpuIntStatus = mpu.getIntStatus();
  
    // get current FIFO count
    fifoCount = mpu.getFIFOCount();

    if ((mpuIntStatus & 0x10) || fifoCount == 1024) {
      mpu.resetFIFO();
    } else if (mpuIntStatus & 0x02) {
      while (fifoCount < packetSize) fifoCount = mpu.getFIFOCount();
      mpu.getFIFOBytes(fifoBuffer, packetSize);
      fifoCount -= packetSize;
  
      mpu.dmpGetQuaternion(&q, fifoBuffer);
      mpu.dmpGetGravity(&gravity, &q);
      mpu.dmpGetYawPitchRoll(ypr, &q, &gravity);

      if(currentMillis - previousMillis > interval) {
        previousMillis = currentMillis;
        
        fsrReading = analogRead(fsrPin);
        fsrVoltage = map(fsrReading, 0, 1023, 0, 5000);
        if(fsrVoltage != 0)
        {
          fsrResistance = 5000 - fsrVoltage;
          fsrResistance *= 10000;
          fsrResistance /= fsrVoltage;

          fsrConductance = 1000000;
          fsrConductance /= fsrResistance;

          if (fsrConductance <= 1000) {
            fsrForce = fsrConductance / 80;
          } else {
            fsrForce = fsrConductance - 1000;
            fsrForce /= 30;
          }
        } else {
          fsrForce = 0;
        }
  
        // Print to bluetooth module
        char* packet = malloc(buff_size);
        
        char yaw_buff[8];
        dtostrf(ypr[0] * 180/M_PI, 4, 2, yaw_buff);

        char pitch_buff[8];
        dtostrf(ypr[1] * 180/M_PI, 4, 2, pitch_buff);

        char roll_buff[8];
        dtostrf(ypr[2] * 180/M_PI, 4, 2, roll_buff);
        
        snprintf(packet, buff_size, "%s %s %s %ld", yaw_buff, pitch_buff, roll_buff, fsrForce);
        
        bluetooth.write(packet);
        free(packet);
      } 
    }
  }
}
