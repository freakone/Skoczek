#include "usart.h"
#include "sensors.h"
#include "globals.h"
#include "serwo.h"
#include <avr/eeprom.h>

volatile static unsigned char buffer[30];
volatile static unsigned char hascii2[2];
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

void rewrite_tab()
{                              
	hascii2[0] = buffer[2];
	hascii2[1] = buffer[3];
}

ISR(USART_RX_vect)
{
	if((pos > 0 && buffer[0] != 0xff )|| pos > 25) //antycrap
         pos = 0;
	
	buffer[pos] = UDR0;        
    pos++;
	
	if(buffer[pos-1] == 0x0A)
	{
		switch(buffer[1])
		{
			case 0x11:
				rewrite_tab();
				set_serwo(KAT, hascii2dec(hascii2, 2));
				break;				
			case 0x12:
				rewrite_tab();
				set_serwo(MOTL, hascii2dec(hascii2, 2));
				break;
			case 0x13:
				rewrite_tab();
				set_serwo(MOTR, hascii2dec(hascii2, 2));
				break;
			case 0x14:
				rewrite_tab();
				set_serwo(NACIAG, hascii2dec(hascii2, 2));
				break;
			case 0x15:
				rewrite_tab();
				set_serwo(ODLACZ, hascii2dec(hascii2, 2));
				break;
			case 0x17: //wyrownanie
				POSITION = 1;
				break;
			case 0x18: //skok
				rewrite_tab();
				JUMP = hascii2dec(hascii2, 2);
				break;
			case 0x19: //stan
				uart_put(0xff);
				uart_put(0x19);
				for(int i = 0; i < SENS; i++)
					dec2hascii(sensor[i], 3);
				uart_put(0x0A);
				break;
			
		}
		
		 pos = 0;		 
	}
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
