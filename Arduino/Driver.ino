#include <IRremote.h>
#include <LiquidCrystal_I2C.h>
#include <Wire.h>
#include <RTClib.h>
#include <dht.h>

dht DHT;

#define RECV_PIN  2
#define data_pin 12
#define store_clk 8
#define shift_clk 7
#define tone_pin 10
#define tempPin A0
#define bits 16
#define DHT11_PIN 4
#define MAX_DIS_MODE 2
#define led_r 5
#define led_g 6
#define led_b 9
#define disk1 0x51
#define TRANSITION_TIME 10


#define MAX_FAN_SPEED 4
#define MAX_TIME_LCD 1000
#define MAX_TIME_SHIFT_REG 20

boolean lcd_flag=true,flag_play=true;;

int fanSpeed=0,setRgbLedMillis=0,r_prv=0,g_prv=0,b_prv=0;
long lcdDisMillis=0,rgbLedMillis=0,lcdDisMillis2=0;
boolean remoteKeys[21],ranLed1=false,ranLed2=false;
int disMode=1;
boolean alarm=false;
String prev="";

int aldd=0,almm=0,alyy=0,alhh=0,almn=0,alsec=0;
int tempToActive=1000;

int rf=0,gf=0,bf=0;


IRrecv irrecv(RECV_PIN);
decode_results results;

LiquidCrystal_I2C lcd(0x20,16,2);

RTC_DS3231 rtc;


char daysOfTheWeek[7][12] = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};

void setup() {

  pinMode(led_r,OUTPUT);
  pinMode(led_g,OUTPUT);
  pinMode(led_b,OUTPUT);
  
  /*analogWrite(led_r,255);
  analogWrite(led_g,255);
  analogWrite(led_b,255);*/

  analogWrite(led_r,0);
  analogWrite(led_g,0);
  analogWrite(led_b,0);

  
  //rtc.adjust(DateTime(F(__DATE__), F(__TIME__)));
  lcd.init(); 
  lcd.setBacklight(LOW);  
  
  rtc.begin();
  // Print a message to the LCD.  
  
  for(int i=0;i<21;i++){
    remoteKeys[i]=false;
  }
  
  pinMode(data_pin,OUTPUT);
  pinMode(store_clk,OUTPUT);
  pinMode(shift_clk,OUTPUT);
  pinMode(tone_pin,OUTPUT);
  
  
  digitalWrite(data_pin,LOW);
  digitalWrite(store_clk,LOW);
  digitalWrite(shift_clk,LOW);

  irrecv.enableIRIn(); 
  Serial.begin(9600); 
  
  reset();
  showTemp();

  
  aldd=readEEPROM(disk1, 0);
  if(aldd!=0){
      almm=readEEPROM(disk1, 1);
      alyy=readEEPROM(disk1, 2);
      alhh=readEEPROM(disk1, 3);
      almn=readEEPROM(disk1, 4);
      alsec=readEEPROM(disk1, 5);
      //Serial.print("al set ");
  }
  
  //Serial.println("Alarm 1: "+String(aldd)+" "+String(almm)+" "+String(alyy)+" : "+String(alhh)+" "+String(almn)+" "+String(alsec));  
  
  
}
void loop() {

  if(Serial.available()){
    String s=Serial.readString();
    /* 
    //Serial.print(s+"  \n ");       
    //s=s.substring(0,(s.indexOf(';',0)));    
    */
    if(s.charAt(0)!='!'){
      s=s.substring(0,(s.indexOf(';',0)));
      Serial.print(s+";");             
      if(s.equalsIgnoreCase("1FE7887")){
        alarmOff();
      }
      else if(remoteKeys[11]==false || s.equalsIgnoreCase("1FE48B7")){
        basicIns(s);    
      }
    }
    else{
      advanceIns(s);
    }
  }
  
  
  if (irrecv.decode(&results)) {    
    String x=String(results.value,HEX);    
    if(x.equalsIgnoreCase("1FE7887")){
      alarmOff();
    }
    else if(remoteKeys[11]==false || x.equalsIgnoreCase("1FE48B7")){
      if(x.equalsIgnoreCase("FFFFFFFF")){  
        if(prev.equalsIgnoreCase("1FEA05F") || prev.equalsIgnoreCase("1FE609F")){
              x=prev;   
              basicIns(x);              
           }      
      }
      else{      
        basicIns(x);
      }
    }

    prev=x;
    irrecv.resume(); 
  }  
  if(alarm==true){
    digitalWrite(tone_pin,HIGH);
  }
  if(millis()-lcdDisMillis>MAX_TIME_LCD){
    /*if(alarm==true){
      digitalWrite(tone_pin,LOW);
    }*/
    showTemp();  
    lcdDisMillis=millis();
    //Serial.println("Update");
  }


  if(millis()-lcdDisMillis2>MAX_TIME_SHIFT_REG){    
    reset();
    updateShiftReg();  
    lcdDisMillis2=millis();    
  }


  if(ranLed1==true){
    if(millis()-rgbLedMillis>setRgbLedMillis){
      randomizeRgbLed1();  
      rgbLedMillis=millis();
      flag_play=true;
    }    
  }
  else if(ranLed2==true){
    if(millis()-rgbLedMillis>setRgbLedMillis){
      randomizeRgbLed2();  
      rgbLedMillis=millis();
      flag_play=true;
    }    
  }
  
}

void advanceIns(String x){
  int in=x.lastIndexOf(':');
  /*
  for(in=x.length();in>=0;in--){
    if(x.charAt(in)=='!'){
      Serial.println(x.charAt(in));
      break;
    }
  }*/
  int in2=0;
  for(in2=in;in2>=0;in2--){
      if(x.charAt(in2)=='!'){
        break;
      }
  }  
  //Serial.println("\n"+String(in)+"  :   "+String(in2));
  String temp=x.substring(in2+1,in);
  in++;
  x=x.substring(in,x.length());
  //Serial.println("\n   "+String(temp)+"      "+String(x));
  Serial.print("!"+temp+":"+x);
  if(temp=="m0"){
    int dd=x.substring(0,x.indexOf('/')).toInt();
        
    x=x.substring(x.indexOf('/')+1,x.length());
    int mm=x.substring(0,x.indexOf('/')).toInt();
    
    x=x.substring(x.indexOf('/')+1,x.length());
    int yy=x.substring(0,x.indexOf('/')).toInt();
    
    x=x.substring(x.indexOf('/')+1,x.length());
    int hh=x.substring(0,x.indexOf('/')).toInt();
    
    x=x.substring(x.indexOf('/')+1,x.length());
    int mn=x.substring(0,x.indexOf('/')).toInt();

    
    x=x.substring(x.indexOf('/')+1,x.length());
    int sec=x.substring(0,x.indexOf('/')).toInt();

    rtc.adjust(DateTime(yy, mm+1, dd, hh, mn, sec));
    //Serial.println(String(dd)+" "+String(mm)+" "+String(yy)+" : "+String(hh)+" "+String(mn)+" "+String(sec));
    
  }
  else if(temp=="m1"){
    rf=x.substring(0,x.indexOf('/')).toInt();
    
    x=x.substring(x.indexOf('/')+1,x.length());    
    gf=x.substring(0,x.indexOf('/')).toInt();
    
    x=x.substring(x.indexOf('/')+1,x.length());    
    bf=x.substring(0,x.indexOf('/')).toInt();

    //Serial.println("\n"+String(r)+" "+String(g)+" "+String(b));   
    
    /*
    b=265-b;
    if(b>255){
      b=255;
    }   
    
    analogWrite(led_r,255-r);
    analogWrite(led_g,255-g);
    analogWrite(led_b,b);
    */

    /*b=b-10;
    if(b<0){
      b=0;
    } */      
    analogWrite(led_r,rf);
    analogWrite(led_g,gf);
    analogWrite(led_b,bf);
    flag_play=true;
    ranLed1=ranLed2=false;     
  }
 else if(temp=="m2"){
    aldd=x.substring(0,x.indexOf('/')).toInt();
        
    x=x.substring(x.indexOf('/')+1,x.length());
    almm=x.substring(0,x.indexOf('/')).toInt();
    almm++;
    
    x=x.substring(x.indexOf('/')+1,x.length());
    alyy=x.substring(2,x.indexOf('/')).toInt();
    
    x=x.substring(x.indexOf('/')+1,x.length());
    alhh=x.substring(0,x.indexOf('/')).toInt();
    
    x=x.substring(x.indexOf('/')+1,x.length());
    almn=x.substring(0,x.indexOf('/')).toInt();
    
    x=x.substring(x.indexOf('/')+1,x.length());
    alsec=x.substring(0,x.indexOf('/')).toInt();
    
    //Serial.println("Setting al: "+String(aldd)+" "+String(almm)+" "+String(alyy)+" : "+String(alhh)+" "+String(almn)+" "+String(alsec));    

    writeEEPROM(disk1, 0, aldd);
    delay(10);
    writeEEPROM(disk1, 1, almm);
    delay(10);
    writeEEPROM(disk1, 2, alyy);
    delay(10);
    writeEEPROM(disk1, 3, alhh);
    delay(10);
    writeEEPROM(disk1, 4, almn);
    delay(10);
    writeEEPROM(disk1, 5, alsec); 
    delay(10);
    //Serial.println("Alarm: "+String(aldd)+" "+String(almm)+" "+String(alyy)+" : "+String(alhh)+" "+String(almn)+" "+String(alsec));  
  }
  else if(temp=="m3"){
    tempToActive=x.substring(0,x.indexOf(';')).toInt();   
    //Serial.println("Temp on"+String(tempToActive));
  }  
  else if(temp=="m4"){
    setRgbLedMillis=x.substring(0,x.indexOf(';')).toInt();   
    ranLed1=true;
    ranLed2=false;    
    randomizeRgbLed1();  
  }  
  else if(temp=="m5"){
    setRgbLedMillis=x.substring(0,x.indexOf(';')).toInt();   
    ranLed2=true;
    ranLed1=false;
    randomizeRgbLed2();  
  }
}

void basicIns(String x){
      if(x.equalsIgnoreCase("1FE20DF")){
        ranLed1=false;
        ranLed2=true;
        if(setRgbLedMillis==0){
          setRgbLedMillis=4000;
        }        
      }
      else if(x.equalsIgnoreCase("1FE807F")){             
        flag_play=!flag_play;
        ranLed1=ranLed2=false;
        if(flag_play==true){
          analogWrite(led_r,rf);
          analogWrite(led_g,gf);
          analogWrite(led_b,bf);
        }
        else{
          analogWrite(led_r,0);
          analogWrite(led_g,0);
          analogWrite(led_b,0);
        }
      }
      else if(x.equalsIgnoreCase("1FE40BF")){   
        analogWrite(led_r,r_prv);
        analogWrite(led_g,g_prv);
        analogWrite(led_b,b_prv); 
      }
      else if(x.equalsIgnoreCase("1FEC03F")){   
        r_prv=rf;
        g_prv=gf;
        b_prv=bf;
        rf=random(256);
        gf=random(256);
        bf=random(256);        
        analogWrite(led_r,rf);
        analogWrite(led_g,gf);
        analogWrite(led_b,bf);
         
      }      
      else if(x.equalsIgnoreCase("1FE609F")){               
        if(fanSpeed<=MAX_FAN_SPEED){
          fanSpeed++;
        }
        //Serial.println("FAN : "+String(fanSpeed));
      }
      else if(x.equalsIgnoreCase("1FEA05F")){
        if(fanSpeed>0){
          fanSpeed--;
        }
        //Serial.println("FAN : "+String(fanSpeed));
      }
      else if(x.equalsIgnoreCase("1FE48B7")){        
        remoteKeys[11]=!remoteKeys[11];            
        static boolean ranLed1_b=ranLed1,ranLed2_b=ranLed2;
        if(remoteKeys[11]==false){
          if(!lcd_flag){
            lcd.setBacklight(HIGH);          
          }
          else{
            lcd.setBacklight(LOW);                    
          }
          if(ranLed1_b==true ){
              ranLed1=true;              
          }
          else if(ranLed2_b==true){                
            ranLed2=true;
          }
          else{
            if(flag_play==true){
              analogWrite(led_r,rf);
              analogWrite(led_g,gf);
              analogWrite(led_b,bf);
            }
          }
        }
        else{
          lcd.setBacklight(LOW);            
          ranLed1=ranLed2=false;
          analogWrite(led_r,0);
          analogWrite(led_g,0);
          analogWrite(led_b,0);
        }  
        //Serial.println("POWER");        
      }
      else if(x.equalsIgnoreCase("1FEE01F")){
        //Serial.println("0"); 
        remoteKeys[0]=!remoteKeys[0];      
      }
      else if(x.equalsIgnoreCase("1FE50AF")){
        //Serial.println("1"); 
        remoteKeys[1]=!remoteKeys[1];      
      }
  
      else if(x.equalsIgnoreCase("1FED827")){
        //Serial.println("2"); 
        remoteKeys[2]=!remoteKeys[2];  
      }
  
      else if(x.equalsIgnoreCase("1FEF807")){
        //Serial.println("3"); 
        remoteKeys[3]=!remoteKeys[3];  
      }
  
      else if(x.equalsIgnoreCase("1FE30CF")){
        //Serial.println("4"); 
        remoteKeys[4]=!remoteKeys[4];  
      }
  
      else if(x.equalsIgnoreCase("1FEB04F")){
        //Serial.println("5"); 
        remoteKeys[5]=!remoteKeys[5];  
      }
  
      else if(x.equalsIgnoreCase("1FE708F")){
        //Serial.println("6"); 
        remoteKeys[6]=!remoteKeys[6];  
      }
  
      else if(x.equalsIgnoreCase("1FE00FF")){
        //Serial.println("7"); 
        remoteKeys[7]=!remoteKeys[7];  
      }
  
      else if(x.equalsIgnoreCase("1FEF00F")){
        //Serial.println("8"); 
        remoteKeys[8]=!remoteKeys[8];  
      }
  
      else if(x.equalsIgnoreCase("1FE9867")){
        //Serial.println("9"); 
        remoteKeys[9]=!remoteKeys[9];  
      }

      else if(x.equalsIgnoreCase("1FE10EF")){
        if(tempToActive==1000){
          remoteKeys[10]=!remoteKeys[10];          
        }
      }

      else if(x.equalsIgnoreCase("1FE906F")){        
        if(lcd_flag){
          lcd.setBacklight(HIGH);
          lcd_flag=false;
        }
        else{
          lcd.setBacklight(LOW);          
          lcd_flag=true;
        }
      }
      else if(x.equalsIgnoreCase("1FE58A7")){
        disMode++;
        if(disMode>MAX_DIS_MODE){
          disMode=1;
          showTemp();
        }        
        else if(disMode==2){  
          lcd.clear();  
          lcd.setCursor(0,0);    
          lcd.printStr("Developed by: ");        
          lcd.setCursor(0,1);
          lcd.printStr("            Sid");    
        }
      }
      else if(x.equalsIgnoreCase("1FE7887")){
         alarmOff();
      }   
      
      reset();
      updateShiftReg();      
}
void alarmOff(){
  if (alarm==true){
          aldd=0;
          almm=0;
          alyy=0;
          alhh=0;
          almn=0;
          alsec=0;
          writeEEPROM(disk1, 0, aldd);
          delay(10);
          writeEEPROM(disk1, 1, almm);
          delay(10);
          writeEEPROM(disk1, 2, alyy);
          delay(10);
          writeEEPROM(disk1, 3, alhh);
          delay(10);
          writeEEPROM(disk1, 4, almn);
          delay(10);
          writeEEPROM(disk1, 5, alsec); 
          delay(10);        
          alarm=false;
          digitalWrite(tone_pin,LOW);
  }  
}
void updateShiftReg(){
  for(int i=15;i>11;i--){
        //Serial.println(String(fanSpeed)+"      "+String(i-10));
        if(remoteKeys[11]==true){
          digitalWrite(data_pin,LOW); 
          storePulse();
          continue;  
        }        
        if(i-10!=fanSpeed){        
          digitalWrite(data_pin,LOW); 
          storePulse();
        }
        else{        
          digitalWrite(data_pin,HIGH);     
          storePulse();
        }
      }
      
      for(int i=10;i>=0;i--){
        if(remoteKeys[11]==true){
          digitalWrite(data_pin,LOW); 
          storePulse();
          continue;  
        }        
        if(remoteKeys[i]==false){        
          digitalWrite(data_pin,LOW); 
          storePulse();
        }
        else{        
          digitalWrite(data_pin,HIGH);     
          storePulse();
        }
      }      
      shiftPulse();  
}

void showTemp(){  
    DateTime now = rtc.now();    
    String date="";
    int dd=now.day();
    date+=String(dd, DEC);  
    date+=String('/');
    int mm=now.month();
    date+=String(mm, DEC);
    date+=String('/');
    int yy=now.year()-2000;
    date+=String(yy, DEC);  
    date+=String("(");
    date+=String(daysOfTheWeek[now.dayOfTheWeek()]);
    date+=String(") ");
    
    String t1; 
    int h=now.hour();
    int hh=h;
    boolean pm=false;
    
    if(h==0){
       h=12;  
    }
    else if(h==12){
      pm=true;
    }
    else if(h>12) {
      h-=12;
      pm=true;
    }
    t1+=String(h, DEC);
    t1+=String(':');
    int mn=now.minute();
    t1+=String(mn, DEC);
    t1+=String(':');
    int sec=now.second();
    t1+=String(sec, DEC);
    if(pm){
      t1+=" PM";
    }
    else{
      t1+=" AM";
    }    

    if(aldd!=0){      
      if(yy>=alyy && mm >=almm && dd>=aldd && hh>=alhh && mn>=almn && sec>=alsec ){
        alarm=true; 
      }           
    }

    int chk = DHT.read11(DHT11_PIN);  
    if(tempToActive!=1000){
      if(chk==DHTLIB_OK){
        if((int)DHT.temperature>=tempToActive){
          remoteKeys[10]=true;
        }
        else{
          remoteKeys[10]=false;
        }
      }
      updateShiftReg();  
    }
    
    
    if(disMode==1){      
      
      lcd.clear();
      lcd.setCursor(0,0);
      static boolean flag=true;
      if(flag){
        lcd.printStr(t1);
        flag=false;
      }
      else{
        lcd.printStr(date);
        flag=true;
      }
    
        
      //DHT.read11(DHT11_PIN);          
      lcd.setCursor(0,1);
      if(chk==DHTLIB_OK){
        lcd.printStr("T:"+(String(DHT.temperature).substring(0,String(DHT.temperature).length()-1))+((char)223)+"C H:"+String(DHT.humidity).substring(0,String(DHT.humidity).length()-1)+"%");
      }
      else{
        lcd.printStr("DHT11 error");
      }
    }
}

void reset(){
  digitalWrite(data_pin,LOW);    
  for(int i=0;i<bits;i++){
    digitalWrite(data_pin,LOW); 
    storePulse();
  }  
  shiftPulse();
}

void storePulse(){  
  digitalWrite(store_clk,HIGH);     
  digitalWrite(store_clk,LOW);
}

void shiftPulse(){
  digitalWrite(shift_clk,HIGH);     
  digitalWrite(shift_clk,LOW);
}

void writeEEPROM(int deviceaddress, unsigned int eeaddress, byte data ) 
{
  Wire.beginTransmission(deviceaddress);
  Wire.write((int)(eeaddress >> 8));   // MSB
  Wire.write((int)(eeaddress & 0xFF)); // LSB
  Wire.write(data);
  Wire.endTransmission();
}
 
byte readEEPROM(int deviceaddress, unsigned int eeaddress ) 
{
  byte rdata = 0xFF;
 
  Wire.beginTransmission(deviceaddress);
  Wire.write((int)(eeaddress >> 8));   // MSB
  Wire.write((int)(eeaddress & 0xFF)); // LSB
  Wire.endTransmission();
 
  Wire.requestFrom(deviceaddress,1);
 
  if (Wire.available()) rdata = Wire.read();
 
  return rdata;
}

void randomizeRgbLed1(){

    int r=random(256);
    int g=random(256);
    int b=random(256);
    static int r1=0,g1=0,b1=0;

    if(r>r1){
      for(int i=r1;r1<r;r1++){
        //analogWrite(led_r,255-i);
        analogWrite(led_r,i);
        delay(TRANSITION_TIME);
      }
    }
    else{
      for(int i=r1;r1>r;r1--){
        //analogWrite(led_r,255-i);
        analogWrite(led_r,i);
        delay(TRANSITION_TIME);
      }
    }

    
    if(g>g1){
      for(int i=g1;g1<g;g1++){
        //analogWrite(led_g,255-i);
        analogWrite(led_g,i);
        delay(TRANSITION_TIME);
      }
    }
    else{
      for(int i=g1;g1>g;g1--){
        //analogWrite(led_g,255-i);
        analogWrite(led_g,i);
        delay(TRANSITION_TIME);
      }
    }
    
    if(b>b1){
      for(int i=b1;b1<b;b1++){
        //analogWrite(led_b,255-i);
        analogWrite(led_b,i);
        delay(TRANSITION_TIME);
      }
    }
    else{
      for(int i=b1;b1>b;b1--){
        //analogWrite(led_b,255-i);
        analogWrite(led_b,i);
        delay(TRANSITION_TIME);
      }
    }
    
    r1=r;
    g1=g;
    b1=b;
}

void randomizeRgbLed2(){
  int r=random(256);
  int g=random(256);
  int b=random(256);
  
  /*analogWrite(led_r,255-r);
  analogWrite(led_g,255-g);
  analogWrite(led_b,255-b);*/
  analogWrite(led_r,r);
  analogWrite(led_g,g);
  analogWrite(led_b,b);
  
}

