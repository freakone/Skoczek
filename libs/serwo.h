#include <avr/io.h> 
#include <util/delay.h>
#include <avr/interrupt.h>

#define MOTL 1
#define MOTR 2
#define KAT 4
#define NACIAG 3
#define ODLACZ 5

void serwo_init();
ISR(TIMER0_COMPA_vect);
ISR(TIMER0_COMPB_vect);
ISR(TIMER1_COMPA_vect);
ISR(TIMER1_COMPB_vect);
ISR(TIMER2_COMPA_vect);
ISR(TIMER2_COMPB_vect);
void switch_serwo(char num, char state);
void set_serwo(char num, char perc);