#include "globals.h"
#include "usart.h"

volatile unsigned char ADDRESS;

void wait_ms(int ms) {
  int i;
  for (i=0; i<ms; i++) {
    _delay_ms(1);
  }
}

void dec2hascii(uint32_t liczba, uint8_t length){
	
	uint32_t buffer;
	
	//sztuczne dopełnienie do > 16 bitów	
	if(length > 4)
		for(int i = 0; i < length -4; i++)
			uart_put('0');
			
	for (volatile int i=length;i > 0;i--)
	{
		buffer = (liczba % (1<< (i*4)) / (1<< (i*4)-4));
		if (buffer>=0 && buffer<=9)
			uart_put((uint8_t)buffer+48);
		else if (buffer>=10 && buffer<=15)
			uart_put((uint8_t)buffer+55);
		
	}
	
}

int32_t hascii2dec(volatile unsigned char* p, volatile int8_t len){

	volatile int32_t t = 0;
	volatile int minus = 0; //w 8 hascii gdy 4 najstarsze bity mają wartość F, oznacza to liczbę ujemną, 
	//konieczne, gdyż mega nie obsługuje więcej niż 16 bitowych zmiennych
	
	for(volatile int i = len -1; i >= 0; i--)
	{
		if(*p>='0' && *p<='9'){
		t+=(int32_t)((*p-48)*(1<< (i*4)));
		}else if(*p>='A' && *p<='F'){
			t+=(int32_t)((*p-55)*(1<< (i*4)));
		}
		if(*p == 'F' && i > 3)
			minus++;
		
		
		++p;	
	}

	if(minus == 4)
		return -t;
	else
		return t;

}