#include <avr/io.h> 
#include <avr/interrupt.h>
#include <util/delay.h>
#include "libs/usart.h"
#include "libs/sensors.h"
#include "libs/serwo.h"
#include "libs/globals.h"
#include <avr/eeprom.h>


volatile unsigned char step = 0, cnter = 0;
int main()
{
	
	sei();	
	uart_init();
	serwo_init();
	sensors_init();
	
	set_serwo(ODLACZ, 85);
	switch_serwo(ODLACZ, 1);	
	set_serwo(NACIAG, 50);
	switch_serwo(NACIAG, 1);
	set_serwo(KAT, 0);
	switch_serwo(KAT, 1);	
	set_serwo(MOTR, 50);
	switch_serwo(MOTR, 1);
	set_serwo(MOTL, 50);
	switch_serwo(MOTL, 1);
	
	while(1)
	{		
		if(POSITION > 0)
		{
			int diff = sensor[4] - sensor[5];
			if(diff < 0 && diff < -15)
			{
				set_serwo(MOTR, 60);				
			}
			else if(diff > 0 && diff > 15)
			{
				set_serwo(MOTL, 60);				
			}
			else
			{
				set_serwo(MOTR, 50);	
				set_serwo(MOTL, 50);
				POSITION = 0;
			}
		}
		
		if(JUMP > 0)
		{
			switch(step)
			{
			case 0:
				set_serwo(ODLACZ, 95);
				set_serwo(NACIAG, 70);
				step++;
			break;
			case 1:
				if(sensor[0] > 800)
				{
					step++;
				}
			break;
			case 2:
				if(JUMP == 10)
					set_serwo(KAT, 60);
				else if(JUMP == 15)
					set_serwo(KAT, 75);
				else if(JUMP == 25)
					set_serwo(KAT, 90);
					
				_delay_ms(10);
				step++;
			break;			
			case 3:
			set_serwo(ODLACZ, 85);
			set_serwo(NACIAG, 50);
			set_serwo(KAT, 0);
			step = 0;
			JUMP = 0;
			break;
			}
		}
		PORTD ^= (1 << PD2);
		_delay_ms(500);
	}
}