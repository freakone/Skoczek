#include <avr/io.h> 
#include <avr/interrupt.h>
#include <util/delay.h>
#include "usart.h"
#define SENS 6

ISR(ADC_vect);
extern volatile int sensor[SENS];
void sensors_init();
