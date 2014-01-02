#include <avr/io.h> 
#include <avr/interrupt.h>
#include <util/delay.h>
#include "libs/usart.h"
#include "libs/sensors.h"
#include "libs/serwo.h"
#include "libs/globals.h"
#include <avr/eeprom.h>


volatile unsigned char step = 0, cnter = 0;
volatile int sens_rmb = 0, diffr = 0, delay_cnt = 0, diff = 0;
int main()
{
	
	sei();	
	uart_init();
	serwo_init();
	sensors_init();
	
	
	while(1)
	{		
		if(POSITION > 0)
		{
			diff = sensor[4] - sensor[5];
			if(diff < 0 && diff < -2)
			{
				set_serwo(MOTR, 55);
				set_serwo(MOTL, 50);		
			}
			else if(diff > 0 && diff > 2)
			{
				set_serwo(MOTL, 55);
				set_serwo(MOTR, 50);
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
				sens_rmb = sensor[0];
				step++;
			break;
			case 1:
			diffr = sensor[0] - sens_rmb;
			if(diffr < 0) diffr *= -1;
				if(diffr >50) //700 && sensor[0] < 1000)
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
				set_serwo(NACIAG, 50);
				step++;
				_delay_ms(700);
			break;		
			
					
			case 3:
			set_serwo(ODLACZ, 85);
			_delay_ms(100);
			set_serwo(KAT, 0);
			delay_cnt = 0;
			step = 0;
			JUMP = 0;
			break;
			}
		}		
	}
}