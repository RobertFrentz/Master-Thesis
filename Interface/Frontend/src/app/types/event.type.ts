export interface StockEvent {
  id: string;
  price: number;
  ema38: number;
  ema100: number;
  sma2: number;
  breakoutPattern: string;
  timeStamp: string;
}
