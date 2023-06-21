import { Injectable } from '@angular/core';
import { webSocket, WebSocketSubject } from 'rxjs/webSocket';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class KafkaService {
  private socket$: WebSocketSubject<any>;

  constructor() {
    this.socket$ = webSocket('ws://localhost:8040/ws'); // Replace with your WebSocket server URL
  }

  connect(): void {
    this.socket$.next('connect');
  }

  subscribeToTopic(topic: string): void {
    this.socket$.next(topic);
  }

  receiveMessages(): Observable<any> {
    return this.socket$.asObservable();
  }
}
