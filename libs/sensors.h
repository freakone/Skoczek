#include <avr/io.h> 
#include <avr/interrupt.h>
#include <util/delay.h>
#include "usart.h"
#define SENS 4


extern volatile unsigned int sensor[SENS];
void sensors_init();
void sensors_update();