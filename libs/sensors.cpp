#include "sensors.h"

volatile unsigned int sensor[SENS];
void sensors_init() // przestawic
{	
	DDRC = 0;
	ADCSRA = (1 << ADEN);
	ADMUX = (1 << REFS0); // AVcc with external cap at ARef
}

void sensors_update()
{
	for(int i = 0; i < SENS; i++)
	{
		ADMUX &= 0b11100000; // czyscimy MUX0-3
		ADMUX |= i;		
		for(int ii = 0; ii < 3; ii++)
		{	
			ADCSRA |= (1 << ADSC);	
			while(ADCSRA & (1<<ADSC));	
			sensor[i] = (ADCL | (ADCH << 8));
		}		
	}
}
