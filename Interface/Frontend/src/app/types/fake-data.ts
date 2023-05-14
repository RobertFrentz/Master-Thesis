import { StockEvent } from './event.type';

export function getDummyEvents(): StockEvent[] {
  const dummyData: StockEvent[] = [];

  for (let i = 0; i < 100; i++) {
    const stockEvent: StockEvent = {
      id: `Event`,
      price: Math.random() * 100, // Random price between 0 and 100
      EMA38: Math.random() * 100, // Random EMA38 value between 0 and 100
      EMA100: Math.random() * 100, // Random EMA100 value between 0 and 100
      SMA2: Math.random() * 100, // Random SMA2 value between 0 and 100
      breakoutPattern: `Pattern-${i + 1}`,
      timeStamp: new Date(
        new Date().getDate() + Math.random() * 100
      ).toISOString(),
    };

    dummyData.push(stockEvent);
  }
  return dummyData;
}
