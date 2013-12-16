#include <avr/io.h> 
#include <avr/interrupt.h>
#include <util/delay.h>

#define F_OSC 8000000
#define USART_BAUDRATE 9600
#define BAUD_PRESCALE (((F_OSC / (USART_BAUDRATE * 16UL))) - 1) 

void uart_init();
void uart_put(unsigned char data);
void uart_puts(unsigned char *s);
void uart_puts(const char *s);
void wait_ms(int ms);
ISR(USART_RX_vect);
