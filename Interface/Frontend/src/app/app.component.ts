import { Component } from '@angular/core';
import { KafkaService } from './services/kafka.service';
import { StockEvent } from './types/event.type';
import { Chart, Data } from './types/chart.type';
import { getDummyEvents } from './types/fake-data';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
})
export class AppComponent {
  value: string = '';
  currentChartData: any[] = [];
  charts: Map<string, Chart> = new Map();

  constructor(private kafkaService: KafkaService) {}

  ngOnInit(): void {
    this.kafkaService.receiveMessages().subscribe((msg) => {
      this.updateChart(msg);
    });

    this.currentChartData = this.initialTestChart();
  }

  subscribeToTopic() {
    const topic = this.value;
    this.kafkaService.subscribeToTopic(topic);
    this.charts.set(topic, { name: topic, chartData: this.initializeChart() });
  }

  private updateChart(msg: any): void {
    const event: StockEvent = JSON.parse(msg);
    const chartData: Data[] = this.charts.get(event.id)!.chartData;
  }

  private initialTestChart(): any[] {
    const dummyEvents = getDummyEvents();

    const prices = dummyEvents.map((event) => {
      return { name: event.timeStamp, value: event.price };
    });
    const ema38 = dummyEvents.map((event) => {
      return { name: event.timeStamp, value: event.EMA38 };
    });
    const ema100 = dummyEvents.map((event) => {
      return { name: event.timeStamp, value: event.EMA100 };
    });
    const sma2 = dummyEvents.map((event) => {
      return { name: event.timeStamp, value: event.SMA2 };
    });

    console.log(prices);
    console.log(ema38);
    console.log(ema100);
    console.log(sma2);

    return [
      {
        name: 'Price',
        series: [...prices],
      },
      {
        name: 'EMA38',
        series: [...ema38],
      },
      {
        name: 'EMA100',
        series: [...ema100],
      },
      {
        name: 'SMA2',
        series: [...sma2],
      },
    ];
  }

  private initializeChart(): any[] {
    return [
      {
        name: 'Price',
        series: [],
      },
      {
        name: 'EMA38',
        series: [],
      },
      {
        name: 'EMA100',
        series: [],
      },
      {
        name: 'SMA2',
        series: [],
      },
    ];
  }
}
