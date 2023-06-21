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
  startvalue = 0;
  endvalue = 100;
  charts: Map<string, Data[]> = new Map();
  tradingDays: Map<string, string[]> = new Map();
  lines: any[] = [];
  currentChartData: Data[] = [];
  currentTopic: string = '';
  currentTradingDay: string = '';
  breakoutPatterns: any[] = [];
  currentBreakoutPattern = 'No Pattern Detected';

  dummy: any[] = [
    {
      id: 'IEBBB.FR',
      price: 57.69,
      breakoutPattern: 'No Pattern Detected',
      timeStamp: '07:04:45.389',
      ema38: 57.69,
      ema100: 57.69,
      sma2: 57.69,
    },
    {
      id: 'IEBBB.FR',
      price: 66.69,
      breakoutPattern: 'No Pattern Detected',
      timeStamp: '07:09:45.461',
      ema38: 58.15153846153846,
      ema100: 57.868217821782174,
      sma2: 62.19,
    },
    {
      id: 'IEBBB.FR',
      price: 68.69,
      breakoutPattern: 'No Pattern Detected',
      timeStamp: '07:14:45.405',
      ema38: 58.691972386587764,
      ema100: 58.082510538182525,
      sma2: 67.69,
    },
    {
      id: 'IEBBB.FR',
      price: 60.69,
      breakoutPattern: 'No Pattern Detected',
      timeStamp: '07:19:45.098',
      ema38: 58.794435341121726,
      ema100: 58.13414399287198,
      sma2: 64.69,
    },
    {
      id: 'IEBBB.FR',
      price: 56.69,
      breakoutPattern: 'No Pattern Detected',
      timeStamp: '07:24:45.317',
      ema38: 58.68651558003856,
      ema100: 58.10554708212203,
      sma2: 58.69,
    },
    {
      id: 'IEBBB.FR',
      price: 52.69,
      breakoutPattern: 'No Pattern Detected',
      timeStamp: '07:29:45.055',
      ema38: 58.379001960549395,
      ema100: 57.998308526040404,
      sma2: 54.69,
    },
    {
      id: 'IEBBB.FR',
      price: 48.69,
      breakoutPattern: 'No Pattern Detected',
      timeStamp: '07:34:45.290',
      ema38: 57.8821300651366,
      ema100: 57.81398558493069,
      sma2: 50.69,
    },
    {
      id: 'IEBBB.FR',
      price: 42.69,
      breakoutPattern: 'Bullish',
      timeStamp: '07:39:45.513',
      ema38: 57.10304647205267,
      ema100: 57.51450072186275,
      sma2: 45.69,
    },
    {
      id: 'IEBBB.FR',
      price: 47.69,
      breakoutPattern: 'No Pattern Detected',
      timeStamp: '07:44:45.241',
      ema38: 56.62032614015253,
      ema100: 57.31995615311299,
      sma2: 45.19,
    },
    {
      id: 'IEBBB.FR',
      price: 52.69,
      breakoutPattern: 'No Pattern Detected',
      timeStamp: '07:49:45.426',
      ema38: 56.41877095347803,
      ema100: 57.22827385305134,
      sma2: 50.19,
    },
    {
      id: 'IEBBB.FR',
      price: 59.69,
      breakoutPattern: 'No Pattern Detected',
      timeStamp: '07:54:45.170',
      ema38: 56.58652628919711,
      ema100: 57.277020905466166,
      sma2: 56.19,
    },
    {
      id: 'IEBBB.FR',
      price: 65.69,
      breakoutPattern: 'No Pattern Detected',
      timeStamp: '07:59:45.452',
      ema38: 57.05337109487931,
      ema100: 57.44361455090248,
      sma2: 62.69,
    },
    {
      id: 'IEBBB.FR',
      price: 72.69,
      breakoutPattern: 'Bearish',
      timeStamp: '08:04:45.286',
      ema38: 57.85524950027011,
      ema100: 57.745523173656885,
      sma2: 69.19,
    },
    {
      id: 'IEBBB.FR',
      price: 81.69,
      breakoutPattern: 'No Pattern Detected',
      timeStamp: '08:09:45.122',
      ema38: 59.07754439769215,
      ema100: 58.219671229624076,
      sma2: 77.19,
    },
  ];

  getValue(): void{
    console.log(this.endvalue);
    console.log(this.startvalue);
  }

  constructor(private kafkaService: KafkaService) {}

  ngOnInit(): void {
    this.currentChartData = this.initializeChart();
    this.kafkaService.receiveMessages().subscribe((msg) => {
      this.updateChart(msg);
    });
  }

  select(topic: string): void {
    this.currentTopic = topic;
    this.currentChartData = [...this.charts.get(topic)!];
  }

  selectDate(tradingDay: string): void{
    if(tradingDay === this.currentTradingDay){
      //this.currentChartData = [...this.charts.get(this.currentTopic)!];
      return;
    }

    this.currentTradingDay = tradingDay;
    this.filterCharData();
  }

  getBreakoutPattern(topic: string): void {
    console.log(topic);
    console.log(this.breakoutPatterns);
    return this.breakoutPatterns.filter((pattern) => pattern.name.getTime() === new Date(Date.parse(topic)).getTime())[0]
      .value;
  }

  subscribeToTopic() {
    const topic = this.value;
    if(!topic){
      return;
    }
    this.currentTopic = topic;
    this.kafkaService.subscribeToTopic(topic);
    this.charts.set(topic, this.initializeChart());
  }

  private updateChart(msg: any): void {
    let event: StockEvent = {
      id: msg.id,
      price: msg.price,
      ema38: msg.ema38,
      ema100: msg.ema100,
      sma2: msg.sma2,
      breakoutPattern: msg.breakoutPattern,
      time: msg.time,
      date: msg.date
    };

    console.log(event);

    if (event.breakoutPattern !== 'No Pattern Detected') {
      this.currentBreakoutPattern = event.breakoutPattern;
    }

    this.tradingDays.forEach((value, key) => {
        console.log("Trading Days key: " + key); 
        console.log("Trading Days value: " + value);
    });

    if (this.charts.has(event.id)) {
      let chartData: Data[] = this.charts.get(event.id)!;
      chartData = this.updateValues(chartData, event);

      this.charts.set(event.id, chartData);

      this.breakoutPatterns.push({
        name: this.getDate(event.date, event.time),
        value: this.currentBreakoutPattern,
      });


      if(this.tradingDays.has(event.id)){
        const tradingDaysForKey = this.tradingDays.get(event.id)!;
        if(!tradingDaysForKey.includes(event.date)){
          console.log(event.date);
          this.tradingDays.set(event.id, [...tradingDaysForKey, event.date]);
        }

      } else {
        this.tradingDays.set(event.id, [event.date]);
      }
      
    } else {
      console.log(event.date);
      let chartData = this.initializeChart();
      chartData = this.updateValues(chartData, event);

      this.charts.set(event.id, chartData);
      this.breakoutPatterns.push({
        name: this.getDate(event.date, event.time),
        value: this.currentBreakoutPattern,
      });   
    }

    if (event.id === this.currentTopic) {
      this.currentChartData = [...this.charts.get(event.id)!];
    }
  }

  private filterCharData(){
    this.currentChartData[0].series = [...this.currentChartData[0].series.filter(data => {
      data.name.toString().includes(this.currentTradingDay);
    })];

    this.currentChartData[1].series = [...this.currentChartData[1].series.filter(data => {
      data.name.toString().includes(this.currentTradingDay);
    })];

    this.currentChartData[2].series = [...this.currentChartData[2].series.filter(data => {
      data.name.toString().includes(this.currentTradingDay);
    })];

    this.currentChartData[3].series = [...this.currentChartData[3].series.filter(data => {
      data.name.toString().includes(this.currentTradingDay);
    })];
  }

  private updateValues(chartData: Data[], event: StockEvent): Data[] {
    console.log('Get date: ' + this.getDate(event.date, event.time));
    chartData[0].series.push({
      name: this.getDate(event.date, event.time),
      value: `${event.price}`,
    });

    chartData[1].series.push({
      name: this.getDate(event.date, event.time),
      value: `${event.ema38}`,
    });

    chartData[2].series.push({
      name: this.getDate(event.date, event.time),
      value: `${event.ema100}`,
    });

    chartData[3].series.push({
      name: this.getDate(event.date, event.time),
      value: `${event.sma2}`,
    });

    return chartData;
  }

  private getDate(date: string, time: string): Date {
    return new Date(Date.parse(date + 'T' + time));
  }

  private initialTestChart(): any[] {
    const dummyEvents = getDummyEvents();

    const prices = dummyEvents.map((event) => {
      return { name: event.time, value: event.price };
    });
    const ema38 = dummyEvents.map((event) => {
      return { name: event.time, value: event.ema38 };
    });
    const ema100 = dummyEvents.map((event) => {
      return { name: event.time, value: event.ema100 };
    });
    const sma2 = dummyEvents.map((event) => {
      return { name: event.time, value: event.sma2 };
    });

    this.breakoutPatterns = dummyEvents.map((event) => {
      return { name: event.time, value: event.breakoutPattern };
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
