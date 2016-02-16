#include <SoftwareSerial.h>
#include <MD5.h>

// Process
// Client connect to bluetooth arduino (door)
// Door generate token and send it
// Client append token validator to token, hash it in md5 and send it
// Door check if hash is valid and unlock it if successful

#define         BLUETOOTH_NAME        "LordOfTheLock"
#define         BLUETOOTH_PIN         "XXXX"
#define         PAYLOAD_SIZE          32
#define         TOKEN_VALIDATOR_SIZE  16
#define         DOOR_MOTOR_PIN        13

SoftwareSerial  bluetooth(10, 9); // RX, TX

String          tokenRequestor =  "XXXXXXXXXXXXXXXXXXXXXXXXXXXXX";
char            *tokenValidator = "XXXXXXXXXXXXXXXX";
String          response; // Stores response of bluetooth device  which simply allows \n between each response.
char            generatedToken[PAYLOAD_SIZE]; // Token used to communicate with other bluetooth device
char            tokensAppended[PAYLOAD_SIZE + TOKEN_VALIDATOR_SIZE + 1];
char            *tokensAppendedHashed = NULL; // Result from MD5 lib
bool            pendingRequest =  false;

void setup() 
{
  pinMode(DOOR_MOTOR_PIN, OUTPUT);
  
  randomSeed(analogRead(0));
  
  // SoftwareSerial "com port" data rate. JY-MCU v1.03 defaults to 9600.
  bluetooth.begin(9600);
  delay(2000);
  
  // Set device properties
  bluetooth.write("AT+NAME" BLUETOOTH_NAME);
  delay(1000);
  bluetooth.write("AT+PIN" BLUETOOTH_PIN);

  generateToken();
}

void generateToken()
{
  // Fill each byte of our token to a random number
  for (int i = 0; i < PAYLOAD_SIZE; ++i)
    generatedToken[i] = random(1, 255); // Start at one to avoid having a premature '\0'
  
  // Generate hash
  memcpy(tokensAppended, generatedToken, PAYLOAD_SIZE);
  memcpy(&tokensAppended[PAYLOAD_SIZE], tokenValidator, TOKEN_VALIDATOR_SIZE);
  tokensAppended[PAYLOAD_SIZE + TOKEN_VALIDATOR_SIZE] = '\0';
  unsigned char* hash = MD5::make_hash(tokensAppended);
  
  // Free ressources if possible
  if (tokensAppendedHashed != NULL)
    free(tokensAppendedHashed);
    
  tokensAppendedHashed = MD5::make_digest(hash, 16);
  
  free(hash);
}

void loop()
{  
   response = ""; // No repeats
   
   // Read bluetooth device output if available.
   if (!bluetooth.available())
     return;
  
   delay(42); // Accumulate data
   while(bluetooth.available()) { // While there is more to be read, keep reading.
     response += (char) bluetooth.read();
   }
   
   // Check first if our command is a token requestor - even if we have already a request pending (to avoid blocking)
   if (response.startsWith(tokenRequestor))
   {
     manageNewRequest();
     return;
   }
   
   // Check if we have a pending request
   if (!pendingRequest)
     return;
  
   managePendingRequest();
}

// Basically just send the pre-generated token
void manageNewRequest()
{
  bluetooth.write(generatedToken, PAYLOAD_SIZE);
  pendingRequest = true;
}

// Check if our response equal to our hash
void managePendingRequest()
{
  if (response.length() == PAYLOAD_SIZE)
  {
    if (response == tokensAppendedHashed)
    {
      digitalWrite(DOOR_MOTOR_PIN, HIGH);
      delay(100);
      digitalWrite(DOOR_MOTOR_PIN, LOW);
    }
  }
  
  generateToken();
  pendingRequest = false;
}
