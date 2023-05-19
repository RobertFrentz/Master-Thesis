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
  charts: Map<string, Data[]> = new Map();
  lines: any[] = [];
  currentChartData: Data[] = [];
  breakoutPatterns: any[] = [];

  constructor(private kafkaService: KafkaService) {}

  ngOnInit(): void {
    this.currentChartData = this.initializeChart();

    this.kafkaService.receiveMessages().subscribe((msg) => {
      this.updateChart(msg);
    });
  }

  select(topic: string): void {
    this.currentChartData = this.charts.get(topic)!;
  }

  getBreakoutPattern(topic: string): void {
    return this.breakoutPatterns.filter((pattern) => pattern.name === topic)[0]
      .value;
  }

  subscribeToTopic() {
    const topic = this.value;
    this.kafkaService.subscribeToTopic(topic);
    this.charts.set(topic, this.initializeChart());
  }

  private updateChart(msg: any): void {
    const event: StockEvent = JSON.parse(msg);

    console.log(event);

    if (this.charts.has(event.id)) {
      let chartData: Data[] = this.charts.get(event.id)!;
      chartData = this.updateValues(chartData, event);

      this.charts.set(event.id, chartData);
    } else {
      let chartData = this.initializeChart();
      chartData = this.updateValues(chartData, event);

      this.charts.set(event.id, chartData);
    }
  }

  private updateValues(chartData: Data[], event: StockEvent): Data[] {
    chartData[0].series.push({
      name: event.timeStamp,
      value: `${event.price}`,
    });

    chartData[1].series.push({
      name: event.timeStamp,
      value: `${event.EMA38}`,
    });

    chartData[2].series.push({
      name: event.timeStamp,
      value: `${event.EMA100}`,
    });

    chartData[3].series.push({
      name: event.timeStamp,
      value: `${event.SMA2}`,
    });

    return chartData;
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

    this.breakoutPatterns = dummyEvents.map((event) => {
      return { name: event.timeStamp, value: event.breakoutPattern };
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

  private initializeChart(): Data[] {
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
