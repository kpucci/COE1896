/*
* Ultrasonic Sensor HC-SR04 and Arduino Tutorial
*
* by Dejan Nedelkovski,
* www.HowToMechatronics.com
*
*/
// defines pins numbers
const int trigPin = 9;
const int echoPin = 10;

const int trigPin2 = 5;
const int echoPin2 = 11;

const int ledPin = 6;
const int ledPin2 = 7;
// defines variables

long duration;
long duration2;
int distance;
int distance2;

void setup()
{
  pinMode(trigPin, OUTPUT); // Sets the trigPin as an Output
  pinMode(echoPin, INPUT); // Sets the echoPin as an Input

  pinMode(trigPin2, OUTPUT); // Sets the trigPin as an Output
  pinMode(echoPin2, INPUT); // Sets the echoPin as an Input

  pinMode(ledPin, OUTPUT); // Sets the trigPin as an Output
  pinMode(ledPin2, OUTPUT); // Sets the trigPin as an Output
  Serial.begin(9600); // Starts the serial communication
}
void loop()
{
  // Clears the trigPin
  digitalWrite(trigPin, LOW);
  digitalWrite(trigPin2, LOW);
//  digitalWrite(ledPin, LOW);
//  digitalWrite(ledPin2, LOW);
  delayMicroseconds(2);
  // Sets the trigPin on HIGH state for 10 micro seconds
  digitalWrite(trigPin, HIGH);
  delayMicroseconds(10);
  digitalWrite(trigPin, LOW);
  
  // Reads the echoPin, returns the sound wave travel time in microseconds
  duration = pulseIn(echoPin, HIGH);
  
  // Calculating the distance
  distance= duration*0.034/2;
  
  
  // Prints the distance on the Serial Monitor
//  Serial.print("Distance #1: ");
//  Serial.print(distance);
//  Serial.print("\t\tDistance #2: ");
//  Serial.println(distance2);

  if(distance <= 8)
    digitalWrite(ledPin, HIGH);
 
  digitalWrite(trigPin2, HIGH);
  delayMicroseconds(10);
  digitalWrite(trigPin2, LOW);
  duration2 = pulseIn(echoPin2, HIGH);
  distance2= duration2*0.034/2;
  if(distance2 <= 8)
    digitalWrite(ledPin2, HIGH);
  
}
