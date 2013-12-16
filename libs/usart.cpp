#include "usart.h"
#include "sensors.h"
#include "globals.h"
#include <avr/eeprom.h>

volatile static unsigned char buffer[30];
volatile static unsigned int pos = 0;

void uart_init()
{	
	UBRR0H = (unsigned char)(BAUD_PRESCALE>>8);
	UBRR0L = (unsigned char)BAUD_PRESCALE;
	UCSR0B = (1<<RXEN0)|(1<<TXEN0)|(1<<RXCIE0);
	UCSR0C = (3<<UCSZ00);	
	pos = 0;
}

unsigned char uart_receive( void )
{
	while ( !(UCSR0A & (1<<RXC0)) ) 	
		;			                

	return UDR0;
}

ISR(USART_RX_vect)
{

	
}

void uart_put( unsigned char data )
{
	while(!( UCSR0A & (1<<UDRE0)));
	UDR0 = data;		        
}

void uart_puts(const char *s )
{
    while (*s)
      uart_put(*s++);
}

void uart_puts(unsigned char *s )
{
    while (*s)
      uart_put(*s++);
}
