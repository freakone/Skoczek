#include <avr/io.h> 
#include <avr/interrupt.h>
#include <util/delay.h>

extern volatile unsigned char ADDRESS;
void dec2hascii(uint32_t liczba, uint8_t length);
int32_t hascii2dec(volatile unsigned char* p, volatile int8_t len);
void wait_ms(int ms);