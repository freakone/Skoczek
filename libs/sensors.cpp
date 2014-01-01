#include "sensors.h"

volatile int sensor[SENS];
void sensors_init() // przestawic
{	
	DDRC = 0;
	ADCSRA = (1 << ADEN) | (1 << ADIE) | (1 << ADPS2) | (1 << ADPS1) | (1 << ADPS0);
	ADMUX = (1 << REFS0); // AVcc with external cap at ARef
	ADCSRA |= (1 << ADSC);
}

unsigned volatile char i = 0;
ISR(ADC_vect)
{
	
	sensor[i] = ADC;	
	i++;
	
	if(i > SENS - 1)
		i = 0;
		
	ADMUX &= 0b11100000; // czyscimy MUX0-3
	ADMUX |= i;		
		
	ADCSRA |= (1 << ADSC);	
}
