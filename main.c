#include <avr/io.h> 
#include <avr/interrupt.h>
#include <util/delay.h>
#include "libs/usart.h"
#include "libs/sensors.h"
#include "libs/serwo.h"
#include "libs/globals.h"
#include <avr/eeprom.h>



int main()
{
	/*ADDRESS = eeprom_read_word(( uint16_t *)1);	
	if(ADDRESS == 0xff) // temporary
		ADDRESS = 0x0C;
		*/
	sei();	
	uart_init();
	serwo_init();
	sensors_init();
	
	set_serwo(ODLACZ, 95);
	switch_serwo(ODLACZ, 1);	
	set_serwo(NACIAG, 70);
	switch_serwo(NACIAG, 1);
	set_serwo(KAT, 0);
	switch_serwo(KAT, 1);
	
	set_serwo(MOTR, 50);
	switch_serwo(MOTR, 1);
	set_serwo(MOTL, 50);
	switch_serwo(MOTL, 1);
	
	while(1)
	{		
		switch_serwo(NACIAG, 1);
		set_serwo(ODLACZ, 95);
		set_serwo(KAT, 30);		
		set_serwo(MOTL, 55);
		_delay_ms(400);
		set_serwo(MOTL, 50);
		_delay_ms(4000);
		set_serwo(ODLACZ, 88);
		_delay_ms(500);
		switch_serwo(NACIAG, 0);
		set_serwo(KAT, 0);
		_delay_ms(2000);
	}
}