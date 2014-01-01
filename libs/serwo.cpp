#include "serwo.h"

void switch_serwo(char num, char state)
{
	switch(num)
	{
	case 1:
		if(state > 0) TIMSK0 |= (1 << OCIE0A); else TIMSK0 &= ~(1 << OCIE0A);
		break;
	case 2:
		if(state > 0) TIMSK0 |= (1 << OCIE0B); else TIMSK0 &= ~(1 << OCIE0B);
		break;
	case 3:
		if(state > 0) TIMSK1 |= (1 << OCIE1B); else TIMSK1 &= ~(1 << OCIE1B);
		break;
	case 4:
		if(state > 0) TIMSK2 |= (1 << OCIE2A); else TIMSK2 &= ~(1 << OCIE2A);
		break;
	case 5:
		if(state > 0) TIMSK2 |= (1 << OCIE2B); else TIMSK2 &= ~(1 << OCIE2B);
		break;
	}

}

void set_serwo(char num, char perc)
{
	if(perc > 100)
		perc = 100;

	switch(num)
	{
	case 1:
		if(perc == 60) perc++;
		OCR0A = 136 + perc;
		break;
	case 2:
		perc = 100 - perc;
		OCR0B = 137 + perc;
		break;
	case 3:
		OCR1B = 119 + perc;
		break;
	case 4:
		OCR2A =  60 + perc;
		break;
	case 5:
		OCR2B = perc;
		break;
	}
}

void serwo_init()
{
	DDRD = 0xFE;
	
	TCCR0B |= (1 << CS01) | (1 << CS00);
//	TIMSK0 |= (1 << OCIE0A) | (1 << OCIE0B);
	OCR0A = 197;
	OCR0B = 130;
	
	TCCR1B |= (1 << CS11) | (1 << CS10) |  (1 << WGM12);
	TIMSK1 |= (1 << OCIE1A);// | (1 << OCIE1B);
	OCR1A = 2500;
	OCR1B = 255;
	
	TCCR2B |= (1 << CS22) | (1 << CS20);
//	TIMSK2 |= (1 << OCIE2A) | (1 << OCIE2B);
	OCR2A = 130;
	OCR2B = 130;
}

ISR(TIMER0_COMPA_vect)
{	
	PORTD &= ~(1 << PD3);
}


ISR(TIMER0_COMPB_vect)
{	
	PORTD &= ~(1 << PD7);
}


ISR(TIMER1_COMPA_vect)
{	
	PORTD |= (1 << PD3) | (1 << PD4) | (1 << PD5) | (1 << PD6) | (1 << PD7);
	TCNT0 = 0;
	TCNT1 = 0;
	TCNT2 = 0;
}

ISR(TIMER1_COMPB_vect)
{		
	PORTD &= ~(1 << PD4);
}

ISR(TIMER2_COMPA_vect)
{	
	PORTD &= ~(1 << PD6);
}

ISR(TIMER2_COMPB_vect)
{	
	PORTD &= ~(1 << PD5);
}
