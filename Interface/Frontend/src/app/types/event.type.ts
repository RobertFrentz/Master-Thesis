export interface StockEvent {
  id: string;
  price: number;
  EMA38: number;
  EMA100: number;
  SMA2: number;
  breakoutPattern: string;
  timeStamp: string;
}
